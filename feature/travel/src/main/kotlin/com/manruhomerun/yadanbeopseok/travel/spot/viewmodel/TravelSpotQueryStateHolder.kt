package com.manruhomerun.yadanbeopseok.travel.spot.viewmodel

import com.manruhomerun.yadanbeopseok.common.error.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.TravelSpotRepository
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import com.manruhomerun.yadanbeopseok.travel.util.toTravelErrorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * B06과 C01b/C01c의 추천, 찜 목록, 검색 조회 상태를 관리합니다.
 *
 * 각 ViewModel에서 별도로 생성하고 해당 ViewModel의 viewModelScope를 전달합니다.
 * 공개 함수는 메인 스레드에서 호출하며, 화면에 표시 중인 조회만 실행합니다.
 * 선택한 관광지, 추가할 일차, 여행 생성과 저장은 각 ViewModel에서 관리합니다.
 */
internal class TravelSpotQueryStateHolder(
    private val repository: TravelSpotRepository,
    private val scope: CoroutineScope,
    private val onSessionExpired: suspend () -> Unit,
) {
    private val _uiState = MutableStateFlow(TravelSpotSelectionUiState())
    val uiState: StateFlow<TravelSpotSelectionUiState> = _uiState.asStateFlow()

    private var currentRegion: Region? = null
    private var queryJob: Job? = null
    private val loadedTabs = mutableSetOf<TravelSpotSelectionTab>()

    /** 지역이 바뀌면 이전 조회를 초기화하고 현재 탭을 불러옵니다. */
    fun initializeTravelSpotSelection(region: Region) {
        if (currentRegion != region) {
            reset()
            currentRegion = region
        }

        loadSelectedTab()
    }

    /** 추천 또는 찜 탭을 선택하고 아직 조회하지 않은 목록을 불러옵니다. */
    fun selectTravelSpotTab(tab: TravelSpotSelectionTab) {
        val state = _uiState.value
        if (state.isSearchMode) return

        if (state.selectedTab != tab) {
            cancelQuery()
            _uiState.update { it.copy(selectedTab = tab, errorMessage = null) }
        }

        loadSelectedTab()
    }

    /** 검색어가 바뀌면 이전 요청과 검색 결과를 지웁니다. 검색은 별도로 실행합니다. */
    fun updateTravelSpotSearchQuery(query: String) {
        if (_uiState.value.searchQuery == query) return

        cancelQuery()
        _uiState.update {
            it.copy(
                searchQuery = query,
                searchResults = emptyList(),
                selectedCategory = if (query.isBlank()) null else it.selectedCategory,
                errorMessage = null,
            )
        }

        if (query.isBlank()) {
            loadSelectedTab()
        }
    }

    /** 검색 버튼 또는 키보드 검색 동작으로 관광지를 조회합니다. */
    fun searchTravelSpots() {
        val state = _uiState.value
        val searchKeyword = state.searchQuery.trim()

        if (searchKeyword.isEmpty()) {
            clearTravelSpotSearch()
            return
        }

        if (state.isSearchLoading) return

        cancelQuery()
        _uiState.update {
            it.copy(
                searchQuery = searchKeyword,
                searchResults = emptyList(),
                isSearchLoading = true,
                errorMessage = null,
            )
        }

        launchSpotQuery(
            fallbackMessage = "관광지를 검색하지 못했습니다.",
            request = { repository.searchTravelSpots(searchKeyword) },
            onSuccess = { spots ->
                _uiState.update { it.copy(searchResults = spots) }
            },
        )
    }

    /** 검색을 종료하고 기존 추천 또는 찜 탭으로 돌아갑니다. */
    fun clearTravelSpotSearch() {
        updateTravelSpotSearchQuery("")
    }

    /** 검색 결과의 카테고리를 변경하며, 필터링은 기존 UiState에서 처리합니다. */
    fun selectTravelSpotCategory(category: TravelSpotCategory?) {
        val state = _uiState.value
        if (!state.isSearchMode || state.selectedCategory == category) return
        if (category == TravelSpotCategory.STADIUM || category == TravelSpotCategory.UNKNOWN) return

        _uiState.update { it.copy(selectedCategory = category) }
    }

    /**
     * 관광지 상세 화면에서 돌아온 경우 현재 목록을 다시 조회합니다.
     *
     * 검색 중이면 동일한 검색어로 검색 결과를 갱신하고,
     * 추천 또는 찜 탭이면 캐시 여부와 관계없이 현재 탭을 갱신합니다.
     */
    fun refreshTravelSpotSelection() {
        val state = _uiState.value

        if (state.isLoading) {
            return
        }

        if (state.isSearchMode) {
            searchTravelSpots()
        } else {
            loadSelectedTab(forceRefresh = true)
        }
    }

    /** 실패한 현재 검색 또는 선택한 탭을 다시 조회합니다. */
    fun retryTravelSpotSelection() {
        refreshTravelSpotSelection()
    }

    /** 조회 상태를 초기화합니다. 부모 ViewModel의 Scope는 취소하지 않습니다. */
    fun reset() {
        cancelQuery()
        currentRegion = null
        loadedTabs.clear()
        _uiState.value = TravelSpotSelectionUiState()
    }

    /** 빈 목록도 성공한 조회로 기억하여 탭 이동마다 중복 요청하지 않습니다. */
    private fun loadSelectedTab(forceRefresh: Boolean = false) {
        val region = currentRegion ?: return
        val state = _uiState.value
        val tab = state.selectedTab

        if (state.isSearchMode || queryJob?.isActive == true) return
        if (!forceRefresh && tab in loadedTabs) return

        cancelQuery()
        _uiState.update {
            it.copy(
                isSuggestedSpotsLoading = tab == TravelSpotSelectionTab.SUGGESTED,
                isDibsSpotsLoading = tab == TravelSpotSelectionTab.DIBS,
                errorMessage = null,
            )
        }

        val fallbackMessage = when (tab) {
            TravelSpotSelectionTab.SUGGESTED -> "추천 관광지를 불러오지 못했습니다."
            TravelSpotSelectionTab.DIBS -> "찜한 관광지를 불러오지 못했습니다."
        }

        launchSpotQuery(
            fallbackMessage = fallbackMessage,
            request = {
                when (tab) {
                    TravelSpotSelectionTab.SUGGESTED -> repository.getSuggestedTravelSpots(region)
                    TravelSpotSelectionTab.DIBS -> repository.getTravelSpotDibs(region)
                }
            },
            onSuccess = { spots ->
                loadedTabs.add(tab)
                _uiState.update {
                    when (tab) {
                        TravelSpotSelectionTab.SUGGESTED -> it.copy(suggestedSpots = spots)
                        TravelSpotSelectionTab.DIBS -> it.copy(dibsSpots = spots)
                    }
                }
            },
        )
    }

    /** 추천, 찜 목록, 검색의 결과 반영과 예외 처리를 공통으로 실행합니다. */
    private fun launchSpotQuery(
        fallbackMessage: String,
        request: suspend () -> List<TravelSpot>,
        onSuccess: (List<TravelSpot>) -> Unit,
    ) {
        if (!scope.isActive) {
            clearLoading()
            return
        }

        queryJob = scope.launch {
            try {
                val spots = request()
                ensureActive()
                onSuccess(spots)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: SessionExpiredException) {
                ensureActive()
                onSessionExpired()
            } catch (exception: Exception) {
                ensureActive()
                val message = exception.toTravelErrorMessage(fallbackMessage)
                _uiState.update { it.copy(errorMessage = message) }
            } finally {
                if (isActive) {
                    clearLoading()
                }
            }
        }
    }

    /** 이전 요청을 취소합니다. 늦게 도착한 결과는 ensureActive에서 차단합니다. */
    private fun cancelQuery() {
        queryJob?.cancel()
        queryJob = null
        clearLoading()
    }

    private fun clearLoading() {
        _uiState.update {
            it.copy(
                isSuggestedSpotsLoading = false,
                isDibsSpotsLoading = false,
                isSearchLoading = false,
            )
        }
    }
}
