package com.manruhomerun.yadanbeopseok.record.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.ApiException
import com.manruhomerun.yadanbeopseok.common.InvalidResponseException
import com.manruhomerun.yadanbeopseok.common.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.common.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.TravelRepository
import com.manruhomerun.yadanbeopseok.model.TravelListPage
import com.manruhomerun.yadanbeopseok.model.TravelStatus
import com.manruhomerun.yadanbeopseok.model.TravelSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * D01의 전체 완료 여행 조회, 시즌 통계와 목록의 추가 표시를 관리합니다.
 */
@HiltViewModel
class TravelRecordViewModel @Inject constructor(
    private val travelRepository: TravelRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TravelRecordUiState())
    val uiState: StateFlow<TravelRecordUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var loadGeneration = 0L
    private var nextPageNumber = FIRST_PAGE_NUMBER
    private val pendingTravels = linkedMapOf<String, TravelSummary>()
    private var keepPreviousSnapshot = false

    init {
        loadCompletedTravels(restart = true)
    }

    /**
     * 화면에 표시할 시즌을 변경합니다.
     *
     * 현재 조회된 완료 여행에 존재하지 않는 시즌은 선택하지 않습니다.
     */
    fun selectSeason(season: Int) {
        val currentState = _uiState.value

        if (season !in currentState.availableSeasons) return
        if (season == currentState.selectedSeason) return

        _uiState.update {
            it.copy(selectedSeason = season, displayedTravelCount = TRAVEL_RECORD_PAGE_SIZE)
        }
    }

    /**
     * 성공한 페이지는 유지하고 실패한 서버 페이지부터 전체 조회를 재개합니다.
     */
    fun retry() {
        if (_uiState.value.isLoading || loadJob?.isActive == true) return
        if (_uiState.value.errorMessage == null) return

        loadCompletedTravels(restart = false)
    }

    /**
     * 완료 여행 목록을 최신 상태로 다시 조회합니다.
     */
    fun refresh() {
        if (_uiState.value.isLoading || loadJob?.isActive == true) return

        loadCompletedTravels(restart = true)
    }

    /** 서버를 다시 호출하지 않고 선택 시즌의 보관된 여행을 10개 더 표시합니다. */
    fun loadNextPage() {
        _uiState.update {
            if (!it.hasNextPage) return@update it

            it.copy(
                displayedTravelCount = it.displayedTravelCount +
                    minOf(TRAVEL_RECORD_PAGE_SIZE, it.seasonTravels.size - it.displayedTravelCount),
            )
        }
    }

    /** 전체 페이지를 순차 조회하며 새로고침 결과는 모두 성공한 뒤에 교체합니다. */
    private fun loadCompletedTravels(restart: Boolean) {
        loadJob?.cancel()
        val generation = ++loadGeneration

        if (restart) {
            pendingTravels.clear()
            nextPageNumber = FIRST_PAGE_NUMBER
            keepPreviousSnapshot = _uiState.value.hasCompleteStatistics ||
                _uiState.value.completedTravels.isNotEmpty()
        }

        _uiState.update {
            it.copy(isLoading = true, errorMessage = null)
        }

        loadJob = viewModelScope.launch {
            try {
                while (true) {
                    val requestedPage = nextPageNumber
                    val page = travelRepository.getTravels(
                        status = TravelStatus.COMPLETED,
                        pageNumber = requestedPage,
                        pageSize = TRAVEL_RECORD_PAGE_SIZE,
                    )

                    ensureActive()
                    if (generation != loadGeneration) return@launch
                    page.validatePage(requestedPage)

                    page.travels.forEach { travel -> pendingTravels[travel.id] = travel }
                    val isComplete = page.pageNumber >= page.totalPages

                    if (isComplete || !keepPreviousSnapshot) {
                        publishTravels(isComplete)
                    }

                    if (isComplete) {
                        pendingTravels.clear()
                        return@launch
                    }

                    nextPageNumber = page.pageNumber + 1
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                ensureActive()
                if (generation != loadGeneration) return@launch

                pendingTravels.clear()
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = null)
                }
            } catch (exception: Exception) {
                ensureActive()
                if (generation != loadGeneration) return@launch

                _uiState.update {
                    it.copy(isLoading = false, errorMessage = exception.toTravelRecordErrorMessage())
                }
            }
        }
    }

    /** 최신 선택 시즌을 존중하고 전체 조회 성공 시에만 통계를 확정합니다. */
    private fun publishTravels(isComplete: Boolean) {
        val travels = pendingTravels.values.sortedByDescending { travel -> travel.endDate }
        val seasons = travels.map { travel -> travel.startDate.year }.distinct().sortedDescending()

        _uiState.update {
            val selectedSeason = it.selectedSeason
                ?.takeIf { season -> !isComplete || season in seasons }
                ?: seasons.firstOrNull()

            it.copy(
                completedTravels = travels,
                selectedSeason = selectedSeason,
                displayedTravelCount = if (selectedSeason == it.selectedSeason) {
                    it.displayedTravelCount
                } else {
                    TRAVEL_RECORD_PAGE_SIZE
                },
                hasCompleteStatistics = isComplete,
                isLoading = !isComplete,
                errorMessage = null,
            )
        }
    }
}

/** 정상적인 빈 첫 페이지는 허용하고, 페이지 번호 불일치나 진행되지 않는 응답은 거부합니다. */
private fun TravelListPage.validatePage(requestedPage: Int) {
    val isValidEmptyPage = pageNumber == FIRST_PAGE_NUMBER && travels.isEmpty() &&
        totalElements == 0L && totalPages in 0..1
    val isInvalid = pageNumber != requestedPage || pageSize <= 0 || totalPages < 0 || totalElements < 0 ||
        (!isValidEmptyPage && (pageNumber > totalPages || travels.isEmpty()))

    if (isInvalid) throw InvalidResponseException("완료 여행 목록의 페이지 정보가 유효하지 않습니다.")
}

/**
 * 완료 여행 조회 예외를 사용자에게 노출해도 되는 안내 문구로 변환합니다.
 */
private fun Exception.toTravelRecordErrorMessage(): String =
    when (this) {
        is NetworkConnectionException ->
            "인터넷 연결을 확인한 후 다시 시도해주세요."

        is NetworkTimeoutException ->
            "서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요."

        is ApiException,
        is InvalidResponseException,
            -> "여행 기록을 불러오지 못했습니다. 잠시 후 다시 시도해주세요."

        else ->
            "여행 기록을 불러오지 못했습니다. 잠시 후 다시 시도해주세요."
    }

private const val FIRST_PAGE_NUMBER = 1
