package com.manruhomerun.yadanbeopseok.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.error.ApiException
import com.manruhomerun.yadanbeopseok.common.error.InvalidResponseException
import com.manruhomerun.yadanbeopseok.common.error.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.error.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.common.error.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.AuthRepository
import com.manruhomerun.yadanbeopseok.data.repository.TravelRepository
import com.manruhomerun.yadanbeopseok.data.repository.TravelSpotRepository
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * 홈 화면에서 발생하는 일회성 이동 이벤트입니다.
 */
sealed interface HomeNavigationEvent {
    /**
     * 인증 정보가 만료되어 로그인 화면으로 이동해야 합니다.
     */
    data object NavigateToLogin : HomeNavigationEvent
}

/**
 * 홈 화면의 여행, 인기 관광지, 필터와 찜 상태를 관리합니다.
 *
 * 화면은 상태를 표시하고 사용자 입력만 전달하며,
 * 데이터 조회와 변경은 이 ViewModel에서 Repository를 통해 처리합니다.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val travelRepository: TravelRepository,
    private val travelSpotRepository: TravelSpotRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _navigationEvents =
        Channel<HomeNavigationEvent>(
            capacity = Channel.BUFFERED,
        )
    val navigationEvents: Flow<HomeNavigationEvent> =
        _navigationEvents.receiveAsFlow()

    /*
     * 인기 관광지 API는 지역을 기준으로 전체 카테고리를 반환하므로,
     * 카테고리를 변경할 때 다시 요청하지 않도록 원본 목록을 보관합니다.
     */
    private var allPopularTravelSpots: List<TravelSpot> =
        emptyList()

    private var homeLoadJob: Job? = null
    private var regionLoadJob: Job? = null

    init {
        loadHome(
            isInitialLoad = true,
        )
    }

    /**
     * 여행 목록과 현재 지역의 인기 관광지를 다시 조회합니다.
     */
    fun refresh() {
        val currentState = _uiState.value

        if (currentState.isLoading || currentState.isRefreshing) {
            return
        }

        loadHome(
            isInitialLoad =
                currentState.currentUserId == null,
        )
    }

    /**
     * 인기 관광지를 조회할 지역을 변경합니다.
     *
     * 여행 목록은 지역 필터와 관계없으므로 인기 관광지만 다시 조회합니다.
     */
    fun selectRegion(region: Region) {
        val currentState = _uiState.value

        if (
            currentState.isLoading ||
            currentState.isRefreshing ||
            currentState.selectedRegion == region
        ) {
            return
        }

        val previousRegion = currentState.selectedRegion

        _uiState.update {
            it.copy(
                selectedRegion = region,
                isRefreshing = true,
                errorMessage = null,
            )
        }

        regionLoadJob?.cancel()
        regionLoadJob =
            viewModelScope.launch {
                try {
                    val travelSpots =
                        travelSpotRepository
                            .getPopularTravelSpots(region)

                    /*
                     * 요청 중 다른 지역으로 변경되지 않은 경우에만
                     * 이번 응답을 현재 화면 상태에 반영합니다.
                     */
                    if (_uiState.value.selectedRegion == region) {
                        allPopularTravelSpots = travelSpots

                        _uiState.update { current ->
                            current.copy(
                                popularTravelSpots =
                                    travelSpots.filterBy(
                                        category =
                                            current.selectedCategory,
                                    ),
                                isRefreshing = false,
                                errorMessage = null,
                            )
                        }
                    }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: SessionExpiredException) {
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            errorMessage = null,
                        )
                    }

                    _navigationEvents.send(
                        HomeNavigationEvent.NavigateToLogin,
                    )
                } catch (exception: Exception) {
                    /*
                     * 지역 조회가 실패하면 이전 지역과 기존 목록으로 되돌립니다.
                     */
                    _uiState.update { current ->
                        current.copy(
                            selectedRegion = previousRegion,
                            popularTravelSpots =
                                allPopularTravelSpots.filterBy(
                                    category =
                                        current.selectedCategory,
                                ),
                            isRefreshing = false,
                            errorMessage =
                                exception.toHomeErrorMessage(
                                    fallbackMessage =
                                        "인기 여행지를 불러오지 못했습니다.",
                                ),
                        )
                    }
                }
            }
    }

    /**
     * 인기 관광지의 카테고리를 변경합니다.
     *
     * 이미 조회한 지역별 목록을 로컬에서 필터링하므로
     * 카테고리를 변경할 때 추가 네트워크 요청은 발생하지 않습니다.
     */
    fun selectCategory(
        category: TravelSpotCategory,
    ) {
        val currentState = _uiState.value

        if (
            currentState.isLoading ||
            currentState.isRefreshing ||
            currentState.selectedCategory == category
        ) {
            return
        }

        _uiState.update {
            it.copy(
                selectedCategory = category,
                popularTravelSpots =
                    allPopularTravelSpots.filterBy(
                        category = category,
                    ),
            )
        }
    }

    /**
     * 관광지의 현재 찜 상태에 따라 찜 또는 찜 취소를 요청합니다.
     */
    /**
     * 관광지의 현재 찜 상태에 따라 찜 또는 찜 취소를 요청합니다.
     */
    fun toggleDibs(spotId: String) {
        val currentState = _uiState.value

        if (
            currentState.isLoading ||
            currentState.isRefreshing ||
            spotId in currentState.updatingDibsSpotIds
        ) {
            return
        }

        val targetSpot =
            allPopularTravelSpots.firstOrNull { spot ->
                spot.id == spotId
            } ?: return

        val requestedRegion = currentState.selectedRegion

        _uiState.update {
            it.copy(
                updatingDibsSpotIds =
                    it.updatingDibsSpotIds + spotId,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            try {
                /*
                 * 찜 등록과 취소 API는 응답 데이터를 반환하지 않으므로
                 * 요청 성공 후 기존 관광지의 찜 상태를 로컬에서 반전합니다.
                 */
                if (targetSpot.dibs) {
                    travelSpotRepository.deleteTravelSpotDibs(spotId)
                } else {
                    travelSpotRepository.addTravelSpotDibs(spotId)
                }

                val updatedSpot =
                    targetSpot.copy(
                        dibs = !targetSpot.dibs,
                    )

                /*
                 * 찜 요청 중 지역이 변경됐다면 이전 지역의 응답을
                 * 현재 관광지 목록에 반영하지 않습니다.
                 */
                if (_uiState.value.selectedRegion == requestedRegion) {
                    allPopularTravelSpots =
                        allPopularTravelSpots.map { spot ->
                            if (spot.id == spotId) {
                                updatedSpot
                            } else {
                                spot
                            }
                        }

                    _uiState.update { current ->
                        current.copy(
                            popularTravelSpots =
                                allPopularTravelSpots.filterBy(
                                    category = current.selectedCategory,
                                ),
                            errorMessage = null,
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: SessionExpiredException) {
                _navigationEvents.send(
                    HomeNavigationEvent.NavigateToLogin,
                )
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage =
                            exception.toHomeErrorMessage(
                                fallbackMessage =
                                    "찜 상태를 변경하지 못했습니다.",
                            ),
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(
                        updatingDibsSpotIds =
                            it.updatingDibsSpotIds - spotId,
                    )
                }
            }
        }
    }

    /**
     * Snackbar로 오류를 표시한 뒤 저장된 오류 문구를 제거합니다.
     */
    fun clearErrorMessage() {
        _uiState.update {
            it.copy(
                errorMessage = null,
            )
        }
    }

    /**
     * 현재 사용자, 여행 목록과 인기 관광지를 함께 조회합니다.
     *
     * 두 요청은 병렬로 실행하지만 결과는 독립적으로 처리합니다.
     * 하나의 요청이 실패해도 다른 요청의 성공 결과는 화면에 반영합니다.
     */
    private fun loadHome(
        isInitialLoad: Boolean,
    ) {
        homeLoadJob?.cancel()
        regionLoadJob?.cancel()

        val requestedRegion =
            _uiState.value.selectedRegion

        _uiState.update {
            it.copy(
                isLoading = isInitialLoad,
                isRefreshing = !isInitialLoad,
                errorMessage = null,
            )
        }

        homeLoadJob =
            viewModelScope.launch {
                try {
                    val currentUserId =
                        authRepository.getCurrentUserId()
                            ?: throw SessionExpiredException()

                    /*
                     * supervisorScope와 개별 Result를 사용하여
                     * 한 요청의 실패가 다른 요청을 취소하지 않게 합니다.
                     */
                    val (travelsResult, travelSpotsResult) =
                        supervisorScope {
                            val travelsDeferred =
                                async {
                                    runHomeRequest {
                                        travelRepository.getPlannedTravels().travels
                                    }
                                }

                            val travelSpotsDeferred =
                                async {
                                    runHomeRequest {
                                        travelSpotRepository
                                            .getPopularTravelSpots(
                                                region = requestedRegion,
                                            )
                                    }
                                }

                            travelsDeferred.await() to
                                travelSpotsDeferred.await()
                        }

                    val travelFailure =
                        travelsResult.exceptionOrNull()
                    val travelSpotFailure =
                        travelSpotsResult.exceptionOrNull()

                    /*
                     * 어느 요청에서든 세션 만료가 확인되면
                     * 일부 데이터를 표시하지 않고 로그인으로 이동합니다.
                     */
                    val sessionExpiredException =
                        listOfNotNull(
                            travelFailure,
                            travelSpotFailure,
                        ).filterIsInstance<SessionExpiredException>()
                            .firstOrNull()

                    if (sessionExpiredException != null) {
                        throw sessionExpiredException
                    }

                    val loadedTravels =
                        travelsResult.getOrNull()
                    val loadedTravelSpots =
                        travelSpotsResult.getOrNull()

                    /*
                     * 인기 관광지 요청이 성공한 경우에만
                     * 현재 지역의 원본 캐시를 교체합니다.
                     */
                    if (loadedTravelSpots != null) {
                        allPopularTravelSpots =
                            loadedTravelSpots
                    }

                    _uiState.update { current ->
                        current.copy(
                            currentUserId = currentUserId,

                            /*
                             * 실패한 영역은 기존 데이터를 유지하고
                             * 성공한 영역만 새로운 응답으로 교체합니다.
                             */
                            travels =
                                loadedTravels
                                    ?: current.travels,
                            popularTravelSpots =
                                loadedTravelSpots
                                    ?.filterBy(
                                        category =
                                            current.selectedCategory,
                                    )
                                    ?: current.popularTravelSpots,
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage =
                                homeLoadErrorMessage(
                                    travelFailure =
                                        travelFailure,
                                    travelSpotFailure =
                                        travelSpotFailure,
                                ),
                        )
                    }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: SessionExpiredException) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null,
                        )
                    }

                    _navigationEvents.send(
                        HomeNavigationEvent.NavigateToLogin,
                    )
                } catch (exception: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage =
                                exception.toHomeErrorMessage(
                                    fallbackMessage =
                                        "홈 정보를 불러오지 못했습니다.",
                                ),
                        )
                    }
                }
            }
    }
}

/**
 * 네트워크 요청의 성공 또는 실패를 Result로 반환합니다.
 *
 * Coroutine 취소는 일반 실패로 변환하지 않고 상위 Coroutine에 전달합니다.
 */
private suspend fun <T> runHomeRequest(
    request: suspend () -> T,
): Result<T> =
    try {
        Result.success(
            request(),
        )
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }

/**
 * 여행과 인기 관광지 요청 결과를 하나의 사용자 안내 문구로 변환합니다.
 */
private fun homeLoadErrorMessage(
    travelFailure: Throwable?,
    travelSpotFailure: Throwable?,
): String? {
    val failures =
        listOfNotNull(
            travelFailure,
            travelSpotFailure,
        )

    if (failures.isEmpty()) {
        return null
    }

    val fallbackMessage =
        when {
            travelFailure != null &&
                travelSpotFailure != null ->
                "홈 정보를 불러오지 못했습니다."

            travelFailure != null ->
                "여행 목록을 불러오지 못했습니다."

            else ->
                "인기 여행지를 불러오지 못했습니다."
        }

    /*
     * 연결 또는 시간 초과 오류가 포함돼 있다면
     * 사용자가 원인을 이해할 수 있도록 해당 오류를 우선 안내합니다.
     */
    val representativeFailure =
        failures.firstOrNull { failure ->
            failure is NetworkConnectionException
        } ?: failures.firstOrNull { failure ->
            failure is NetworkTimeoutException
        } ?: failures.first()

    return representativeFailure.toHomeErrorMessage(
        fallbackMessage = fallbackMessage,
    )
}

/**
 * 인기 관광지 전체 목록에서 선택한 카테고리만 반환합니다.
 */
private fun List<TravelSpot>.filterBy(
    category: TravelSpotCategory,
): List<TravelSpot> =
    filter { spot ->
        spot.category == category
    }

/**
 * 내부 예외 정보를 노출하지 않고 사용자용 안내 문구로 변환합니다.
 */
private fun Throwable.toHomeErrorMessage(
    fallbackMessage: String,
): String =
    when (this) {
        is NetworkConnectionException ->
            "인터넷 연결을 확인한 후 다시 시도해주세요."

        is NetworkTimeoutException ->
            "서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요."

        is ApiException,
        is InvalidResponseException,
            -> "$fallbackMessage 잠시 후 다시 시도해주세요."

        else ->
            "$fallbackMessage 잠시 후 다시 시도해주세요."
    }
