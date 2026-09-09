package com.manruhomerun.yadanbeopseok.record.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.ApiException
import com.manruhomerun.yadanbeopseok.common.InvalidResponseException
import com.manruhomerun.yadanbeopseok.common.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.common.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.TravelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * D01의 완료 여행 목록과 시즌 선택 상태를 관리합니다.
 */
@HiltViewModel
class TravelRecordViewModel @Inject constructor(
    private val travelRepository: TravelRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TravelRecordUiState())
    val uiState: StateFlow<TravelRecordUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadCompletedTravels()
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
            it.copy(selectedSeason = season)
        }
    }

    /**
     * 완료 여행 목록 조회를 다시 시도합니다.
     */
    fun retry() {
        if (_uiState.value.isLoading) return

        loadCompletedTravels()
    }

    /**
     * 완료 여행 목록을 최신 상태로 다시 조회합니다.
     */
    fun refresh() {
        if (_uiState.value.isLoading) return

        loadCompletedTravels()
    }

    /**
     * 완료 여행 목록을 조회하고 최신 여행이 먼저 보이도록 정렬합니다.
     */
    private fun loadCompletedTravels() {
        loadJob?.cancel()

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
            )
        }

        loadJob = viewModelScope.launch {
            try {
                val page = travelRepository.getCompletedTravels()
                val completedTravels = page.travels.sortedByDescending { travel ->
                    travel.endDate
                }

                val availableSeasons = completedTravels
                    .map { travel -> travel.startDate.year }
                    .distinct()
                    .sortedDescending()

                val currentSeason = _uiState.value.selectedSeason
                val selectedSeason = currentSeason
                    ?.takeIf { season -> season in availableSeasons }
                    ?: availableSeasons.firstOrNull()

                _uiState.update {
                    it.copy(
                        completedTravels = completedTravels,
                        selectedSeason = selectedSeason,
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                _uiState.update { it.copy(isLoading = false, errorMessage = null) }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        completedTravels = emptyList(),
                        selectedSeason = null,
                        isLoading = false,
                        errorMessage = exception.toTravelRecordErrorMessage(),
                    )
                }
            }
        }
    }
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
