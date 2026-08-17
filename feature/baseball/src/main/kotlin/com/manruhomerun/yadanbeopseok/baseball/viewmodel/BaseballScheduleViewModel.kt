package com.manruhomerun.yadanbeopseok.baseball.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.error.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.error.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.common.error.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.BaseballRepository
import com.manruhomerun.yadanbeopseok.model.KboTeam
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * A·05 경기 일정 화면에서 발생하는 일회성 이동 이벤트입니다.
 */
sealed interface BaseballScheduleNavigationEvent {
    /** 인증 정보가 만료되어 로그인 화면으로 이동해야 합니다. */
    data object NavigateToLogin : BaseballScheduleNavigationEvent
}

/**
 * A·05의 구단 선택과 구단별 경기 일정을 관리합니다.
 */
@HiltViewModel
class BaseballScheduleViewModel @Inject constructor(
    private val baseballRepository: BaseballRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BaseballScheduleUiState())
    val uiState: StateFlow<BaseballScheduleUiState> = _uiState.asStateFlow()

    private val _navigationEvents = Channel<BaseballScheduleNavigationEvent>(
        capacity = Channel.BUFFERED,
    )
    val navigationEvents: Flow<BaseballScheduleNavigationEvent> = _navigationEvents.receiveAsFlow()

    private var scheduleLoadJob: Job? = null

    init {
        loadTeamSchedule(_uiState.value.selectedTeam)
    }

    /**
     * 선택한 구단으로 필터를 변경하고 해당 구단의 경기 일정을 조회합니다.
     */
    fun selectTeam(team: KboTeam) {
        if (_uiState.value.selectedTeam == team) return

        loadTeamSchedule(team)
    }

    /**
     * 현재 선택된 구단의 경기 일정 조회를 다시 시도합니다.
     */
    fun retry() {
        if (_uiState.value.isLoading) return

        loadTeamSchedule(_uiState.value.selectedTeam)
    }

    /**
     * 선택한 구단의 요청일 기준 최대 2주 경기 일정을 조회합니다.
     */
    private fun loadTeamSchedule(team: KboTeam) {
        scheduleLoadJob?.cancel()

        _uiState.value = BaseballScheduleUiState(
            selectedTeam = team,
            isLoading = true,
        )

        scheduleLoadJob = viewModelScope.launch {
            try {
                val games = baseballRepository.getTeamGameSchedule(team)
                val sortedGames = games.sortedBy { it.gameDateTime }

                if (_uiState.value.selectedTeam == team) {
                    _uiState.update {
                        it.copy(
                            games = sortedGames,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: SessionExpiredException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                    )
                }

                _navigationEvents.send(
                    BaseballScheduleNavigationEvent.NavigateToLogin,
                )
            } catch (exception: Exception) {
                if (_uiState.value.selectedTeam == team) {
                    val errorMessage = exception.toBaseballScheduleErrorMessage()

                    _uiState.update {
                        it.copy(
                            games = emptyList(),
                            isLoading = false,
                            errorMessage = errorMessage,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 경기 일정 조회 예외를 사용자에게 표시할 안전한 문구로 변환합니다.
 */
private fun Throwable.toBaseballScheduleErrorMessage(): String =
    when (this) {
        is NetworkConnectionException ->
            "인터넷 연결을 확인한 후 다시 시도해주세요."

        is NetworkTimeoutException ->
            "서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요."

        else ->
            "경기 일정을 불러오지 못했습니다. 잠시 후 다시 시도해주세요."
    }
