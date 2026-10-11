package com.manruhomerun.yadanbeopseok.baseball.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.common.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.BaseballRepository
import com.manruhomerun.yadanbeopseok.model.KboTeam
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
 * 경기 일정 화면의 구단 선택, 최초 조회와 기존 결과를 유지하는 갱신을 관리합니다.
 */
@HiltViewModel
class BaseballScheduleViewModel @Inject constructor(
    private val baseballRepository: BaseballRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BaseballScheduleUiState())
    val uiState: StateFlow<BaseballScheduleUiState> = _uiState.asStateFlow()

    private var scheduleLoadJob: Job? = null
    private var scheduleRequestGeneration = 0L
    private var hasInitialized = false
    private var hasExpiredSession = false

    /** 최초 진입에만 전달받은 구단을 적용하고, 이후 복귀에서는 현재 구단을 갱신합니다. */
    fun onScreenResumed(initialTeamId: Long? = null) {
        if (hasExpiredSession) return

        if (!hasInitialized) {
            hasInitialized = true
            val team = initialTeamId?.let(KboTeam::findByServerId) ?: _uiState.value.selectedTeam
            loadTeamSchedule(team)
            return
        }

        refresh()
    }

    /**
     * 다른 구단은 새로 조회하고, 이미 선택된 구단은 기존 결과를 유지하며 갱신합니다.
     */
    fun selectTeam(team: KboTeam) {
        if (hasExpiredSession) return

        if (!hasInitialized) {
            hasInitialized = true
            loadTeamSchedule(team)
            return
        }

        if (_uiState.value.selectedTeam == team) {
            refresh()
            return
        }

        loadTeamSchedule(team)
    }

    /** 현재 구단을 갱신하며 최초 조회 및 갱신 중의 중복 요청은 차단합니다. */
    fun refresh() {
        val state = _uiState.value
        if (hasExpiredSession || !hasInitialized || state.isLoading || state.isRefreshing) return

        loadTeamSchedule(state.selectedTeam)
    }

    /** 최초 조회 실패 화면에서 현재 구단을 다시 조회합니다. */
    fun retry() {
        refresh()
    }

    /** 표시를 마친 갱신 오류만 제거하고 최초 조회 실패 안내는 유지합니다. */
    fun clearErrorMessage(message: String) {
        _uiState.update { state ->
            if (state.hasLoadedSchedule && state.errorMessage == message) {
                state.copy(errorMessage = null)
            } else {
                state
            }
        }
    }

    /**
     * 선택한 구단의 요청일 기준 최대 2주 경기 일정을 조회합니다.
     */
    private fun loadTeamSchedule(team: KboTeam) {
        val state = _uiState.value
        val keepsCurrentResult = state.selectedTeam == team && state.hasLoadedSchedule
        val generation = ++scheduleRequestGeneration
        scheduleLoadJob?.cancel()

        _uiState.value = if (keepsCurrentResult) {
            state.copy(isLoading = false, isRefreshing = true, errorMessage = null)
        } else {
            BaseballScheduleUiState(selectedTeam = team, isLoading = true)
        }

        scheduleLoadJob = viewModelScope.launch {
            try {
                val games = baseballRepository.getTeamGameSchedule(team)
                ensureActive()
                if (!isCurrentScheduleRequest(generation, team)) return@launch

                _uiState.update {
                    it.copy(
                        games = games.sortedBy { game -> game.gameDateTime },
                        isLoading = false,
                        isRefreshing = false,
                        hasLoadedSchedule = true,
                        errorMessage = null,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                ensureActive()
                if (!isCurrentScheduleRequest(generation, team)) return@launch

                hasExpiredSession = true
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, errorMessage = null) }
            } catch (exception: Exception) {
                ensureActive()
                if (!isCurrentScheduleRequest(generation, team)) return@launch

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = exception.toBaseballScheduleErrorMessage(),
                    )
                }
            }
        }
    }

    /** 취소되거나 교체된 요청이 현재 구단의 결과와 오류를 덮어쓰지 않게 합니다. */
    private fun isCurrentScheduleRequest(generation: Long, team: KboTeam): Boolean =
        !hasExpiredSession && generation == scheduleRequestGeneration && _uiState.value.selectedTeam == team
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
