package com.manruhomerun.yadanbeopseok.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.ApiException
import com.manruhomerun.yadanbeopseok.common.InvalidResponseException
import com.manruhomerun.yadanbeopseok.common.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.common.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.TravelRepository
import com.manruhomerun.yadanbeopseok.data.repository.TravelSpotRepository
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSpotFilterCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * 홈 화면의 여행, 인기 관광지, 필터와 찜 상태를 관리합니다.
 *
 * 화면은 상태를 표시하고 사용자 입력만 전달하며,
 * 데이터 조회와 변경은 이 ViewModel에서 Repository를 통해 처리합니다.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val travelRepository: TravelRepository,
    private val travelSpotRepository: TravelSpotRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var hasCompletedInitialLoad = false
    private var homeLoadJob: Job? = null
    private var popularSpotLoadJob: Job? = null

    init {
        loadHome(isInitialLoad = true)
    }

    /**
     * 여행 목록과 현재 지역의 인기 관광지를 다시 조회합니다.
     */
    fun refresh() {
        val currentState = _uiState.value

        if (currentState.isLoading || currentState.isRefreshing) {
            return
        }

        loadHome(isInitialLoad = !hasCompletedInitialLoad)
    }

    /**
     * 인기 관광지를 조회할 지역을 변경합니다.
     *
     * 여행 목록은 지역 필터와 관계없으므로 인기 관광지만 다시 조회합니다.
     */
    fun selectRegion(region: Region) {
        val currentState = _uiState.value

        if (currentState.isLoading || currentState.selectedRegion == region) {
            return
        }

        loadPopularTravelSpots(
            region = region,
            category = currentState.selectedCategory,
        )
    }

    /**
     * 인기 관광지의 카테고리를 변경합니다.
     *
     * 서버 필수 요청 값이므로 선택한 카테고리로 인기 관광지를 다시 조회합니다.
     */
    fun selectCategory(category: TravelSpotFilterCategory) {
        val currentState = _uiState.value

        if (currentState.isLoading || currentState.selectedCategory == category) {
            return
        }

        loadPopularTravelSpots(
            region = currentState.selectedRegion,
            category = category,
        )
    }

    /**
     * 관광지의 현재 찜 상태에 따라 찜 또는 찜 취소를 요청합니다.
     */
    fun toggleDibs(spotId: String) {
        val currentState = _uiState.value
        val isUnavailable = currentState.isLoading ||
            currentState.isRefreshing ||
            spotId in currentState.updatingDibsSpotIds

        if (isUnavailable) {
            return
        }

        val targetSpot = currentState.popularTravelSpots.firstOrNull { spot ->
            spot.id == spotId
        } ?: return
        val requestedRegion = currentState.selectedRegion
        val requestedCategory = currentState.selectedCategory

        _uiState.update {
            it.copy(
                updatingDibsSpotIds = it.updatingDibsSpotIds + spotId,
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

                val updatedSpot = targetSpot.copy(dibs = !targetSpot.dibs)

                /*
                 * 찜 요청 중 지역 또는 카테고리가 변경됐다면 이전 필터의 응답을
                 * 현재 인기 관광지 목록에 반영하지 않습니다.
                 */
                if (_uiState.value.matchesPopularSpotFilter(requestedRegion, requestedCategory)) {
                    _uiState.update { state ->
                        state.copy(
                            popularTravelSpots = state.popularTravelSpots.map { spot ->
                                if (spot.id == spotId) updatedSpot else spot
                            },
                            errorMessage = null,
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                /*
                 * 세션 만료 화면 이동은 앱의 공통 세션 관찰이 처리합니다.
                 * updatingDibsSpotIds는 finally에서 정리합니다.
                 */
            } catch (exception: Exception) {
                if (_uiState.value.matchesPopularSpotFilter(requestedRegion, requestedCategory)) {
                    _uiState.update {
                        it.copy(
                            errorMessage = exception.toHomeErrorMessage(
                                fallbackMessage = "찜 상태를 변경하지 못했습니다.",
                            ),
                        )
                    }
                }
            } finally {
                _uiState.update {
                    it.copy(
                        updatingDibsSpotIds = it.updatingDibsSpotIds - spotId,
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

    /** 선택한 지역과 카테고리의 인기 관광지만 다시 조회합니다. */
    private fun loadPopularTravelSpots(
        region: Region,
        category: TravelSpotFilterCategory,
    ) {
        homeLoadJob?.cancel()
        popularSpotLoadJob?.cancel()

        _uiState.update {
            it.copy(
                selectedRegion = region,
                selectedCategory = category,
                popularTravelSpots = emptyList(),
                isRefreshing = true,
                travelSpotErrorMessage = null,
                errorMessage = null,
            )
        }

        popularSpotLoadJob = viewModelScope.launch {
            try {
                val travelSpots = travelSpotRepository.getPopularTravelSpots(
                    region = region,
                    category = category,
                )

                if (_uiState.value.matchesPopularSpotFilter(region, category)) {
                    _uiState.update {
                        it.copy(
                            popularTravelSpots = travelSpots,
                            isRefreshing = false,
                            travelSpotErrorMessage = null,
                            errorMessage = null,
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                if (_uiState.value.matchesPopularSpotFilter(region, category)) {
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            travelSpotErrorMessage = null,
                            errorMessage = null,
                        )
                    }
                }
            } catch (exception: Exception) {
                if (_uiState.value.matchesPopularSpotFilter(region, category)) {
                    val errorMessage = exception.toHomeErrorMessage(
                        fallbackMessage = "인기 관광지를 불러오지 못했습니다.",
                    )

                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            travelSpotErrorMessage = errorMessage,
                            errorMessage = errorMessage,
                        )
                    }
                }
            }
        }
    }

    /**
     * 여행 목록과 인기 관광지를 함께 조회합니다.
     *
     * 두 요청은 병렬로 실행하지만 결과는 독립적으로 처리합니다.
     * 하나의 요청이 실패해도 다른 요청의 성공 결과는 화면에 반영합니다.
     */
    private fun loadHome(isInitialLoad: Boolean) {
        homeLoadJob?.cancel()
        popularSpotLoadJob?.cancel()

        val requestedRegion = _uiState.value.selectedRegion
        val requestedCategory = _uiState.value.selectedCategory

        _uiState.update {
            it.copy(
                isLoading = isInitialLoad,
                isRefreshing = !isInitialLoad,
                travelSpotErrorMessage = null,
                errorMessage = null,
            )
        }

        homeLoadJob = viewModelScope.launch {
            try {
                /*
                 * supervisorScope와 개별 Result를 사용하여
                 * 한 요청의 실패가 다른 요청을 취소하지 않게 합니다.
                 */
                val (travelsResult, travelSpotsResult) = supervisorScope {
                    val travelsDeferred = async {
                        runHomeRequest {
                            travelRepository.getPlannedTravels()
                        }
                    }

                    val travelSpotsDeferred = async {
                        runHomeRequest {
                            travelSpotRepository.getPopularTravelSpots(
                                region = requestedRegion,
                                category = requestedCategory,
                            )
                        }
                    }

                    travelsDeferred.await() to travelSpotsDeferred.await()
                }

                val travelFailure = travelsResult.exceptionOrNull()
                val travelSpotFailure = travelSpotsResult.exceptionOrNull()

                /*
                 * 어느 요청에서든 세션 만료가 확인되면
                 * 일부 데이터를 표시하지 않고 로그인으로 이동합니다.
                 */
                val sessionExpiredException = listOfNotNull(travelFailure, travelSpotFailure)
                    .filterIsInstance<SessionExpiredException>()
                    .firstOrNull()

                if (sessionExpiredException != null) {
                    throw sessionExpiredException
                }

                val loadedTravels = travelsResult.getOrNull()
                val loadedTravelSpots = travelSpotsResult.getOrNull()
                val travelSpotErrorMessage = travelSpotFailure?.toHomeErrorMessage(
                    fallbackMessage = "인기 관광지를 불러오지 못했습니다.",
                )

                val visibleTravelSpotErrorMessage = travelSpotErrorMessage.takeIf {
                    _uiState.value.popularTravelSpots.isEmpty()
                }

                hasCompletedInitialLoad = true

                _uiState.update { current ->
                    val canApplyTravelSpots = current.matchesPopularSpotFilter(
                        region = requestedRegion,
                        category = requestedCategory,
                    )

                    current.copy(
                        /*
                         * 실패한 영역은 기존 데이터를 유지하고
                         * 성공한 영역만 새로운 응답으로 교체합니다.
                         */
                        travels = loadedTravels ?: current.travels,
                        popularTravelSpots = if (canApplyTravelSpots) {
                            loadedTravelSpots ?: current.popularTravelSpots
                        } else {
                            current.popularTravelSpots
                        },
                        isLoading = false,
                        isRefreshing = false,
                        travelSpotErrorMessage = visibleTravelSpotErrorMessage,
                        errorMessage = homeLoadErrorMessage(
                            travelFailure = travelFailure,
                            travelSpotFailure = travelSpotFailure,
                        ),
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                /*
                 * ApiCallExecutor 또는 인증 정보 변경이 세션 상태를 갱신하고,
                 * 앱의 공통 세션 관찰이 로그인 화면 이동을 처리합니다.
                 */
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        travelSpotErrorMessage = null,
                        errorMessage = null,
                    )
                }
            } catch (exception: Exception) {
                val errorMessage = exception.toHomeErrorMessage(
                    fallbackMessage = "홈 정보를 불러오지 못했습니다.",
                )
                val visibleTravelSpotErrorMessage = errorMessage.takeIf {
                    _uiState.value.popularTravelSpots.isEmpty()
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        travelSpotErrorMessage = visibleTravelSpotErrorMessage,
                        errorMessage = errorMessage,
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
        Result.success(request())
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
    val failures = listOfNotNull(travelFailure, travelSpotFailure)

    if (failures.isEmpty()) {
        return null
    }

    val fallbackMessage =
        when {
            travelFailure != null && travelSpotFailure != null -> "홈 정보를 불러오지 못했습니다."

            travelFailure != null -> "여행 목록을 불러오지 못했습니다."

            else -> "인기 관광지를 불러오지 못했습니다."
        }

    /*
     * 연결 또는 시간 초과 오류가 포함돼 있다면
     * 사용자가 원인을 이해할 수 있도록 해당 오류를 우선 안내합니다.
     */
    val representativeFailure = failures.firstOrNull { failure ->
        failure is NetworkConnectionException
    } ?: failures.firstOrNull { failure ->
        failure is NetworkTimeoutException
    } ?: failures.first()

    return representativeFailure.toHomeErrorMessage(
        fallbackMessage = fallbackMessage,
    )
}

/** 현재 인기 관광지 응답이 속한 필터 조합인지 확인합니다. */
private fun HomeUiState.matchesPopularSpotFilter(
    region: Region,
    category: TravelSpotFilterCategory,
): Boolean = selectedRegion == region && selectedCategory == category

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
