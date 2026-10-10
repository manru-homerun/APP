package com.manruhomerun.yadanbeopseok.mypage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.common.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.TravelSpotRepository
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSpotFilterCategory
import com.manruhomerun.yadanbeopseok.model.TravelSpotListPage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * H·04 찜한 관광지 목록과 지역·카테고리 필터를 관리합니다.
 */
@HiltViewModel
class TravelSpotDibsViewModel @Inject constructor(
    private val travelSpotRepository: TravelSpotRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TravelSpotDibsUiState())
    val uiState: StateFlow<TravelSpotDibsUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var requestGeneration = 0L
    private var dibsDirty = false
    private var refreshTargetPage = FIRST_PAGE_NUMBER
    private var pendingRefresh = false

    init {
        loadDibsSpots(
            pageNumber = FIRST_PAGE_NUMBER,
            clearCurrentSpots = true,
        )
    }

    /**
     * 찜한 관광지를 조회할 지역을 변경합니다.
     */
    fun selectRegion(region: Region) {
        val currentState = _uiState.value

        if (currentState.isBusy || currentState.updatingDibsSpotIds.isNotEmpty()) return
        if (currentState.selectedRegion == region) return

        _uiState.update {
            it.copy(selectedRegion = region)
        }

        loadDibsSpots(
            pageNumber = FIRST_PAGE_NUMBER,
            clearCurrentSpots = true,
        )
    }

    /** 찜한 관광지를 조회할 카테고리를 변경하며 null이면 전체를 조회합니다. */
    fun selectCategory(category: TravelSpotFilterCategory?) {
        val currentState = _uiState.value

        if (currentState.isBusy || currentState.updatingDibsSpotIds.isNotEmpty()) return
        if (currentState.selectedCategory == category) return

        _uiState.update {
            it.copy(selectedCategory = category)
        }

        loadDibsSpots(
            pageNumber = FIRST_PAGE_NUMBER,
            clearCurrentSpots = true,
        )
    }

    /**
     * 찜한 관광지 목록 조회를 다시 시도합니다.
     */
    fun retry() {
        val currentState = _uiState.value
        if (currentState.isBusy || currentState.updatingDibsSpotIds.isNotEmpty()) return

        if (dibsDirty || currentState.refreshErrorMessage != null) {
            loadDibsSpots(FIRST_PAGE_NUMBER, clearCurrentSpots = false, refreshRange = true)
        } else if (currentState.loadMoreErrorMessage != null) {
            _uiState.update { it.copy(loadMoreErrorMessage = null) }
            loadNextPage()
        } else {
            loadDibsSpots(
                pageNumber = FIRST_PAGE_NUMBER,
                clearCurrentSpots = currentState.dibsSpots.isEmpty(),
            )
        }
    }

    /**
     * 관광지 상세 화면에서 돌아온 경우 찜 목록을 다시 조회합니다.
     */
    fun refresh() {
        val currentState = _uiState.value
        dibsDirty = true
        refreshTargetPage = maxOf(refreshTargetPage, currentState.pageNumber, FIRST_PAGE_NUMBER)
        if (currentState.updatingDibsSpotIds.isNotEmpty()) {
            pendingRefresh = true
            return
        }

        loadDibsSpots(
            pageNumber = FIRST_PAGE_NUMBER,
            clearCurrentSpots = false,
            refreshRange = true,
        )
    }

    /** 목록 하단에서 다음 찜 페이지를 조회합니다. */
    fun loadNextPage() {
        val currentState = _uiState.value
        val canLoadNextPage = !currentState.isBusy && !dibsDirty &&
            currentState.updatingDibsSpotIds.isEmpty() &&
            currentState.loadMoreErrorMessage == null && currentState.refreshErrorMessage == null &&
            currentState.hasNextPage

        if (!canLoadNextPage) return

        loadDibsSpots(
            pageNumber = currentState.pageNumber + 1,
            clearCurrentSpots = false,
        )
    }

    /**
     * 지정한 관광지의 찜을 취소합니다.
     *
     * 서버 요청에 성공한 경우 현재 목록에서 관광지를 제거합니다.
     */
    fun deleteDibs(spotId: String) {
        val currentState = _uiState.value

        if (currentState.isBusy) return
        if (spotId in currentState.updatingDibsSpotIds) return
        if (currentState.dibsSpots.none { travelSpot -> travelSpot.id == spotId }) return

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
                travelSpotRepository.deleteTravelSpotDibs(spotId)
                ensureActive()

                if (_uiState.value.matchesFilter(requestedRegion, requestedCategory)) {
                    dibsDirty = true
                    pendingRefresh = true
                    refreshTargetPage = maxOf(refreshTargetPage, _uiState.value.pageNumber, FIRST_PAGE_NUMBER)
                    _uiState.update { state ->
                        state.copy(
                            dibsSpots = state.dibsSpots.filterNot { travelSpot ->
                                travelSpot.id == spotId
                            },
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                /*
                 * 세션 만료에 따른 로그인 화면 이동은
                 * 앱의 공통 세션 관찰이 처리합니다.
                 */
            } catch (exception: Exception) {
                ensureActive()
                if (_uiState.value.matchesFilter(requestedRegion, requestedCategory)) {
                    _uiState.update {
                        it.copy(
                            errorMessage = exception.toTravelSpotDibsErrorMessage(
                                defaultMessage = "찜을 취소하지 못했습니다. 잠시 후 다시 시도해주세요.",
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
                if (pendingRefresh && _uiState.value.updatingDibsSpotIds.isEmpty() && viewModelScope.isActive) {
                    pendingRefresh = false
                    refresh()
                }
            }
        }
    }

    /**
     * Snackbar에 오류를 표시한 뒤 저장된 문구를 제거합니다.
     */
    fun clearErrorMessage() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    /**
     * 현재 선택한 지역과 카테고리의 찜한 관광지를 조회합니다.
     */
    private fun loadDibsSpots(
        pageNumber: Int,
        clearCurrentSpots: Boolean,
        refreshRange: Boolean = false,
    ) {
        if (!viewModelScope.isActive) return
        val generation = ++requestGeneration
        loadJob?.cancel()

        val requestedRegion = _uiState.value.selectedRegion
        val requestedCategory = _uiState.value.selectedCategory
        val isFirstPage = pageNumber == FIRST_PAGE_NUMBER
        if (clearCurrentSpots) {
            dibsDirty = false
            pendingRefresh = false
            refreshTargetPage = FIRST_PAGE_NUMBER
        }
        val targetPage = refreshTargetPage

        fun isCurrentRequest(): Boolean = requestGeneration == generation &&
            _uiState.value.matchesFilter(requestedRegion, requestedCategory)

        _uiState.update {
            it.copy(
                dibsSpots = if (clearCurrentSpots) emptyList() else it.dibsSpots,
                pageNumber = if (clearCurrentSpots) 0 else it.pageNumber,
                totalPages = if (clearCurrentSpots) 0 else it.totalPages,
                isLoading = isFirstPage && !refreshRange,
                isLoadingMore = !isFirstPage && !refreshRange,
                isRefreshing = refreshRange,
                errorMessage = if (refreshRange) it.errorMessage else null,
                loadMoreErrorMessage = null,
                refreshErrorMessage = null,
            )
        }

        loadJob = viewModelScope.launch {
            try {
                val page = if (refreshRange) {
                    loadDibsRange(requestedRegion, requestedCategory, targetPage)
                } else {
                    travelSpotRepository.getTravelSpotDibs(
                        region = requestedRegion,
                        category = requestedCategory,
                        pageNumber = pageNumber,
                        pageSize = DIBS_PAGE_SIZE,
                    )
                }
                ensureActive()

                if (isCurrentRequest()) {
                    if (!refreshRange && !isFirstPage && page.pageNumber > page.totalPages) {
                        refresh()
                        return@launch
                    }
                    dibsDirty = false
                    refreshTargetPage = maxOf(FIRST_PAGE_NUMBER, page.pageNumber)
                    _uiState.update { state ->
                        val travelSpots = if (isFirstPage || refreshRange) {
                            page.travelSpots
                        } else {
                            state.dibsSpots + page.travelSpots
                        }

                        state.copy(
                            dibsSpots = travelSpots.distinctBy { travelSpot -> travelSpot.id },
                            pageNumber = page.pageNumber,
                            totalPages = page.totalPages,
                            isLoading = false,
                            isLoadingMore = false,
                            isRefreshing = false,
                            errorMessage = if (refreshRange) state.errorMessage else null,
                            loadMoreErrorMessage = null,
                            refreshErrorMessage = null,
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                // 화면 이동은 공통 세션 관찰이 처리합니다.
            } catch (exception: Exception) {
                ensureActive()
                if (isCurrentRequest()) {
                    val message = exception.toTravelSpotDibsErrorMessage(
                        defaultMessage = if (refreshRange) {
                            "찜 목록을 갱신하지 못했습니다. 다시 시도해주세요."
                        } else {
                            "찜한 관광지를 불러오지 못했습니다. 다시 시도해주세요."
                        },
                    )
                    _uiState.update {
                        it.copy(
                            errorMessage = if (refreshRange) it.errorMessage else message.takeIf { isFirstPage },
                            loadMoreErrorMessage = message.takeIf { !isFirstPage && !refreshRange },
                            refreshErrorMessage = message.takeIf { refreshRange },
                        )
                    }
                }
            } finally {
                if (isCurrentRequest() && isActive) {
                    _uiState.update { it.copy(isLoading = false, isLoadingMore = false, isRefreshing = false) }
                    loadJob = null
                }
            }
        }
    }

    /** 페이지별 임시 결과를 모으며 목록 축소 시 현재 범위 밖 결과를 버립니다. */
    private suspend fun loadDibsRange(
        region: Region,
        category: TravelSpotFilterCategory?,
        targetPage: Int,
    ): TravelSpotListPage {
        val pages = linkedMapOf<Int, TravelSpotListPage>()
        var pageNumber = FIRST_PAGE_NUMBER
        while (true) {
            val page = travelSpotRepository.getTravelSpotDibs(region, category, pageNumber, DIBS_PAGE_SIZE)
            pages[pageNumber] = page
            val lastPage = minOf(targetPage, maxOf(FIRST_PAGE_NUMBER, page.totalPages))
            if (pageNumber >= lastPage) {
                return page.copy(
                    travelSpots = if (page.totalElements == 0L) emptyList() else {
                        pages.filterKeys { it <= lastPage }.values.flatMap { it.travelSpots }.distinctBy { it.id }
                    },
                    pageNumber = lastPage,
                )
            }
            pageNumber++
        }
    }
}

private val TravelSpotDibsUiState.isBusy: Boolean
    get() = isLoading || isLoadingMore || isRefreshing

/** 현재 찜 목록 응답이 속한 필터 조합인지 확인합니다. */
private fun TravelSpotDibsUiState.matchesFilter(
    region: Region,
    category: TravelSpotFilterCategory?,
): Boolean = selectedRegion == region && selectedCategory == category

/**
 * 내부 예외 정보를 노출하지 않는 사용자용 오류 문구로 변환합니다.
 */
private fun Exception.toTravelSpotDibsErrorMessage(defaultMessage: String): String =
    when (this) {
        is NetworkConnectionException ->
            "인터넷 연결을 확인한 후 다시 시도해주세요."

        is NetworkTimeoutException ->
            "서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요."

        else ->
            defaultMessage
    }

private const val FIRST_PAGE_NUMBER = 1
private const val DIBS_PAGE_SIZE = 10
