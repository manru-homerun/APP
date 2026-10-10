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
import com.manruhomerun.yadanbeopseok.model.TravelStatus
import com.manruhomerun.yadanbeopseok.model.TravelSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 홈 화면의 여행, 인기 관광지, 필터와 찜 상태를 관리합니다.
 *
 * 전체 새로고침과 영역별 조회는 같은 결과 반영 처리를 사용하며,
 * 각 영역의 요청·오류·진행 상태는 서로 독립적으로 관리합니다.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val travelRepository: TravelRepository,
    private val travelSpotRepository: TravelSpotRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var hasCompletedInitialLoad = false
    private var hasExpiredSession = false
    private var activeTravels: List<TravelSummary> = emptyList()
    private var upcomingTravels: List<TravelSummary> = emptyList()
    private var homeLoadJob: Job? = null
    private var travelListLoadJob: Job? = null
    private var popularSpotLoadJob: Job? = null
    private var homeRequestGeneration = 0L
    private var travelRequestGeneration = 0L
    private var popularSpotRequestGeneration = 0L
    private val popularSpotMutex = Mutex()

    init {
        loadHome(isInitialLoad = true)
    }

    /** 최초 조회, 당겨서 새로고침과 홈 복귀 시 두 영역을 함께 갱신합니다. */
    fun refresh() {
        val state = _uiState.value
        if (hasExpiredSession || state.isLoading || state.isRefreshing) return

        loadHome(isInitialLoad = !hasCompletedInitialLoad)
    }

    /** 여행 영역의 재시도에서 진행 중·예정 여행 목록만 갱신합니다. */
    fun refreshTravels() {
        val state = _uiState.value
        if (hasExpiredSession || state.isLoading || state.isRefreshing || state.isTravelListLoading) return

        val generation = ++travelRequestGeneration
        _uiState.update { it.copy(isTravelListLoading = true) }

        travelListLoadJob = viewModelScope.launch {
            try {
                val failure = loadTravelList(generation)
                ensureActive()
                if (isCurrentTravelRequest(generation) && failure != null) {
                    _uiState.update {
                        it.copy(errorMessage = failure.toHomeErrorMessage("여행 목록을 불러오지 못했습니다."))
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                handleSessionExpired()
            }
        }
    }

    /** 지역 옆 새로고침과 관광지 재시도에서 현재 필터의 인기 관광지만 갱신합니다. */
    fun refreshPopularTravelSpots() {
        val state = _uiState.value
        if (hasExpiredSession || state.isLoading || state.isRefreshing || state.isPopularSpotLoading) return

        startPopularSpotLoad(state.selectedRegion, state.selectedCategory)
    }

    /** 여행 목록에 영향을 주지 않고 인기 관광지의 지역 필터를 변경합니다. */
    fun selectRegion(region: Region) {
        val state = _uiState.value
        if (hasExpiredSession || state.isLoading || state.isRefreshing || state.selectedRegion == region) return

        startPopularSpotLoad(region, state.selectedCategory)
    }

    /** null이면 category 쿼리를 생략하고 선택한 지역의 전체 인기 관광지를 조회합니다. */
    fun selectCategory(category: TravelSpotFilterCategory?) {
        val state = _uiState.value
        if (hasExpiredSession || state.isLoading || state.isRefreshing || state.selectedCategory == category) return

        startPopularSpotLoad(state.selectedRegion, category)
    }

    /** 조회와 찜 변경을 직렬화하고, 성공한 찜 상태만 현재 관광지에 반영합니다. */
    fun toggleDibs(spotId: String) {
        val state = _uiState.value
        val isUnavailable = hasExpiredSession || state.isLoading || state.isRefreshing ||
            state.isPopularSpotLoading || spotId in state.updatingDibsSpotIds
        if (isUnavailable) return

        val targetSpot = state.popularTravelSpots.firstOrNull { it.id == spotId } ?: return
        val requestedRegion = state.selectedRegion
        val requestedCategory = state.selectedCategory
        _uiState.update { it.copy(updatingDibsSpotIds = it.updatingDibsSpotIds + spotId) }

        viewModelScope.launch {
            try {
                // 조회와 변경 모두 API 호출부터 결과 반영까지 같은 잠금 안에서 처리합니다.
                popularSpotMutex.withLock {
                    ensureActive()
                    if (hasExpiredSession) return@withLock

                    if (targetSpot.dibs) {
                        travelSpotRepository.deleteTravelSpotDibs(spotId)
                    } else {
                        travelSpotRepository.addTravelSpotDibs(spotId)
                    }

                    ensureActive()
                    if (!hasExpiredSession && _uiState.value.matchesPopularSpotFilter(requestedRegion, requestedCategory)) {
                        _uiState.update { current ->
                            current.copy(
                                popularTravelSpots = current.popularTravelSpots.map { spot ->
                                    if (spot.id == spotId) spot.copy(dibs = !targetSpot.dibs) else spot
                                },
                            )
                        }
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                handleSessionExpired()
            } catch (exception: Exception) {
                ensureActive()
                if (!hasExpiredSession && _uiState.value.matchesPopularSpotFilter(requestedRegion, requestedCategory)) {
                    _uiState.update {
                        it.copy(errorMessage = exception.toHomeErrorMessage("찜 상태를 변경하지 못했습니다."))
                    }
                }
            } finally {
                _uiState.update { it.copy(updatingDibsSpotIds = it.updatingDibsSpotIds - spotId) }
            }
        }
    }

    /** Snackbar 안내만 제거하며 여행·관광지 영역의 지속 오류는 유지합니다. */
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /** 단독 관광지 조회를 교체하고, 필터가 변경된 경우에만 이전 목록을 비웁니다. */
    private fun startPopularSpotLoad(region: Region, category: TravelSpotFilterCategory?) {
        val filterChanged = !_uiState.value.matchesPopularSpotFilter(region, category)
        val generation = ++popularSpotRequestGeneration
        popularSpotLoadJob?.cancel()

        _uiState.update {
            it.copy(
                selectedRegion = region,
                selectedCategory = category,
                popularTravelSpots = if (filterChanged) emptyList() else it.popularTravelSpots,
                isPopularSpotLoading = true,
                travelSpotErrorMessage = if (filterChanged) null else it.travelSpotErrorMessage,
            )
        }

        popularSpotLoadJob = viewModelScope.launch {
            try {
                val failure = loadPopularTravelSpots(region, category, generation)
                ensureActive()
                if (isCurrentPopularSpotRequest(generation, region, category) && failure != null) {
                    _uiState.update {
                        it.copy(errorMessage = failure.toHomeErrorMessage("인기 관광지를 불러오지 못했습니다."))
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                handleSessionExpired()
            }
        }
    }

    /** 두 여행 상태의 전체 페이지를 조회하고 이 요청이 최신일 때만 결과를 반영합니다. */
    private suspend fun loadTravelList(generation: Long): Throwable? {
        try {
            val (activeResult, upcomingResult) = coroutineScope {
                val active = async { loadTravelPages(TravelStatus.ACTIVE) }
                val upcoming = async { loadTravelPages(TravelStatus.UPCOMING) }
                active.await() to upcoming.await()
            }

            coroutineContext.ensureActive()
            if (!isCurrentTravelRequest(generation)) return null

            val failure = activeResult.failure ?: upcomingResult.failure
            activeTravels = mergeTravelPages(activeTravels, activeResult)
            upcomingTravels = mergeTravelPages(upcomingTravels, upcomingResult)

            _uiState.update {
                it.copy(
                    travels = activeTravels + upcomingTravels,
                    hasLoadedTravelList = it.hasLoadedTravelList || failure == null,
                    travelErrorMessage = failure?.toHomeErrorMessage("여행 목록을 불러오지 못했습니다."),
                )
            }
            return failure
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: SessionExpiredException) {
            handleSessionExpired()
            throw exception
        } catch (exception: Exception) {
            coroutineContext.ensureActive()
            if (isCurrentTravelRequest(generation)) {
                _uiState.update {
                    it.copy(travelErrorMessage = exception.toHomeErrorMessage("여행 목록을 불러오지 못했습니다."))
                }
            }
            return exception
        } finally {
            if (generation == travelRequestGeneration) {
                _uiState.update { it.copy(isTravelListLoading = false) }
                travelListLoadJob = null
            }
        }
    }

    /** 인기 관광지 조회와 결과 반영을 찜 변경과 같은 잠금으로 보호합니다. */
    private suspend fun loadPopularTravelSpots(
        region: Region,
        category: TravelSpotFilterCategory?,
        generation: Long,
    ): Throwable? {
        try {
            popularSpotMutex.withLock {
                coroutineContext.ensureActive()
                if (!isCurrentPopularSpotRequest(generation, region, category)) return@withLock

                val spots = travelSpotRepository.getPopularTravelSpots(region, category)
                coroutineContext.ensureActive()
                if (isCurrentPopularSpotRequest(generation, region, category)) {
                    _uiState.update { it.copy(popularTravelSpots = spots, travelSpotErrorMessage = null) }
                }
            }
            return null
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: SessionExpiredException) {
            handleSessionExpired()
            throw exception
        } catch (exception: Exception) {
            coroutineContext.ensureActive()
            if (isCurrentPopularSpotRequest(generation, region, category)) {
                _uiState.update {
                    it.copy(travelSpotErrorMessage = exception.toHomeErrorMessage("인기 관광지를 불러오지 못했습니다."))
                }
            }
            return exception
        } finally {
            if (generation == popularSpotRequestGeneration) {
                _uiState.update { it.copy(isPopularSpotLoading = false) }
                popularSpotLoadJob = null
            }
        }
    }

    /** 전체 조회는 영역별 처리를 병렬 실행하며 이미 반영한 목록을 다시 대입하지 않습니다. */
    private fun loadHome(isInitialLoad: Boolean) {
        cancelReadRequests()
        val homeGeneration = homeRequestGeneration
        val travelGeneration = travelRequestGeneration
        val popularGeneration = popularSpotRequestGeneration
        val region = _uiState.value.selectedRegion
        val category = _uiState.value.selectedCategory

        _uiState.update {
            it.copy(
                isLoading = isInitialLoad,
                isRefreshing = !isInitialLoad,
                isTravelListLoading = true,
                isPopularSpotLoading = true,
            )
        }

        homeLoadJob = viewModelScope.launch {
            try {
                // 일반 조회 실패는 각 영역에서 반환하고, 세션 만료·취소만 형제 요청을 중단합니다.
                val (travelFailure, spotFailure) = coroutineScope {
                    val travels = async { loadTravelList(travelGeneration) }
                    val spots = async { loadPopularTravelSpots(region, category, popularGeneration) }
                    travels.await() to spots.await()
                }

                ensureActive()
                if (!hasExpiredSession && homeGeneration == homeRequestGeneration) {
                    val message = homeLoadErrorMessage(travelFailure, spotFailure)
                    if (message != null) {
                        _uiState.update { it.copy(errorMessage = message) }
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                handleSessionExpired()
            } catch (exception: Exception) {
                ensureActive()
                if (!hasExpiredSession && homeGeneration == homeRequestGeneration) {
                    _uiState.update {
                        it.copy(errorMessage = exception.toHomeErrorMessage("홈 정보를 불러오지 못했습니다."))
                    }
                }
            } finally {
                if (homeGeneration == homeRequestGeneration) {
                    hasCompletedInitialLoad = true
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
                    homeLoadJob = null
                }
            }
        }
    }

    /** 읽기 요청만 무효화합니다. 이미 시작한 찜 변경은 취소하지 않습니다. */
    private fun cancelReadRequests() {
        homeRequestGeneration++
        travelRequestGeneration++
        popularSpotRequestGeneration++
        homeLoadJob?.cancel()
        travelListLoadJob?.cancel()
        popularSpotLoadJob?.cancel()
        homeLoadJob = null
        travelListLoadJob = null
        popularSpotLoadJob = null
    }

    /** 로그인 이동은 공통 세션 관찰에 맡기고, 만료 이후 결과 반영과 추가 요청을 차단합니다. */
    private fun handleSessionExpired() {
        if (hasExpiredSession) return

        hasExpiredSession = true
        cancelReadRequests()
        _uiState.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                isTravelListLoading = false,
                isPopularSpotLoading = false,
                travelErrorMessage = null,
                travelSpotErrorMessage = null,
                errorMessage = null,
            )
        }
    }

    private fun isCurrentTravelRequest(generation: Long): Boolean =
        !hasExpiredSession && generation == travelRequestGeneration

    private fun isCurrentPopularSpotRequest(
        generation: Long,
        region: Region,
        category: TravelSpotFilterCategory?,
    ): Boolean = !hasExpiredSession && generation == popularSpotRequestGeneration &&
        _uiState.value.matchesPopularSpotFilter(region, category)

    /** 지정한 여행 상태의 페이지를 순서대로 조회하고 성공한 페이지를 보관합니다. */
    private suspend fun loadTravelPages(status: TravelStatus): TravelPageLoadResult {
        val travels = mutableListOf<TravelSummary>()
        var pageNumber = FIRST_PAGE_NUMBER

        while (true) {
            val page = try {
                travelRepository.getTravels(
                    status = status,
                    pageNumber = pageNumber,
                    pageSize = TRAVEL_PAGE_SIZE,
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: SessionExpiredException) {
                throw exception
            } catch (exception: Exception) {
                return TravelPageLoadResult(
                    travels = travels,
                    hasLoadedFirstPage = pageNumber > FIRST_PAGE_NUMBER,
                    failure = exception,
                )
            }

            travels += page.travels

            if (page.pageNumber >= page.totalPages) {
                return TravelPageLoadResult(
                    travels = travels,
                    hasLoadedFirstPage = true,
                )
            }

            pageNumber = page.pageNumber + 1
        }
    }
}

private data class TravelPageLoadResult(
    val travels: List<TravelSummary>,
    val hasLoadedFirstPage: Boolean,
    val failure: Throwable? = null,
)

/** 새로 조회한 페이지를 반영하면서 뒤쪽 페이지 실패 시 기존 항목을 유지합니다. */
private fun mergeTravelPages(
    current: List<TravelSummary>,
    result: TravelPageLoadResult,
): List<TravelSummary> {
    if (!result.hasLoadedFirstPage) return current
    if (result.failure == null) return result.travels.distinctBy { travel -> travel.id }

    val loadedIds = result.travels.mapTo(mutableSetOf()) { travel -> travel.id }

    return result.travels + current.filterNot { travel -> travel.id in loadedIds }
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
    category: TravelSpotFilterCategory?,
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

private const val FIRST_PAGE_NUMBER = 1
private const val TRAVEL_PAGE_SIZE = 10
