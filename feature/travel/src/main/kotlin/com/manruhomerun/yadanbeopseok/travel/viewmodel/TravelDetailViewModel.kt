package com.manruhomerun.yadanbeopseok.travel.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.error.ApiException
import com.manruhomerun.yadanbeopseok.common.error.InvalidResponseException
import com.manruhomerun.yadanbeopseok.common.error.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.error.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.common.error.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.TravelRepository
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
 * 여행 상세 화면에서 발생하는 일회성 이동 이벤트입니다.
 */
sealed interface TravelDetailNavigationEvent {
    /** 인증 정보가 만료되어 로그인 화면으로 이동해야 합니다. */
    data object NavigateToLogin : TravelDetailNavigationEvent
}

/**
 * 여행 상세 정보와 현재 선택한 일차를 관리합니다.
 */
@HiltViewModel
class TravelDetailViewModel @Inject constructor(
    private val travelRepository: TravelRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TravelDetailUiState())
    val uiState: StateFlow<TravelDetailUiState> = _uiState.asStateFlow()

    private val _navigationEvents = Channel<TravelDetailNavigationEvent>(
        capacity = Channel.BUFFERED,
    )
    val navigationEvents: Flow<TravelDetailNavigationEvent> =
        _navigationEvents.receiveAsFlow()

    private var currentTravelId: String? = null
    private var loadJob: Job? = null

    /**
     * 전달받은 여행 ID에 해당하는 상세 정보를 조회합니다.
     *
     * 동일한 여행을 이미 불러왔다면 불필요한 재요청을 하지 않습니다.
     */
    fun loadTravel(travelId: String) {
        val normalizedTravelId = travelId.trim()

        if (normalizedTravelId.isEmpty()) {
            _uiState.update {
                TravelDetailUiState(
                    isLoading = false,
                    errorMessage = "여행 정보를 확인할 수 없습니다.",
                )
            }
            return
        }

        val currentState = _uiState.value
        val isAlreadyLoaded =
            currentTravelId == normalizedTravelId &&
                currentState.travel != null

        if (isAlreadyLoaded) {
            return
        }

        currentTravelId = normalizedTravelId
        requestTravel(normalizedTravelId)
    }

    /**
     * 현재 여행 상세 정보 조회를 다시 시도합니다.
     */
    fun retry() {
        if (_uiState.value.isLoading) {
            return
        }

        val travelId = currentTravelId ?: return
        requestTravel(travelId)
    }

    /**
     * 여행 상세 화면에 표시할 일차를 변경합니다.
     */
    fun selectDay(day: Int) {
        val currentState = _uiState.value

        if (day !in currentState.dayNumbers) {
            return
        }

        _uiState.update {
            it.copy(selectedDay = day)
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
     * Repository를 통해 여행 상세 정보를 조회합니다.
     */
    private fun requestTravel(travelId: String) {
        loadJob?.cancel()

        _uiState.update {
            it.copy(
                travel = null,
                selectedDay = null,
                isLoading = true,
                errorMessage = null,
            )
        }

        loadJob = viewModelScope.launch {
            try {
                val travel = travelRepository.getTravel(travelId)
                val firstDay = travel.days.firstOrNull()?.day

                _uiState.update {
                    it.copy(
                        travel = travel,
                        selectedDay = firstDay,
                        isLoading = false,
                        errorMessage = null,
                    )
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
                    TravelDetailNavigationEvent.NavigateToLogin,
                )
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.toTravelDetailErrorMessage(),
                    )
                }
            }
        }
    }
}

/**
 * 여행 상세 조회 예외를 사용자에게 표시할 문구로 변환합니다.
 */
private fun Exception.toTravelDetailErrorMessage(): String =
    when (this) {
        is NetworkConnectionException ->
            "인터넷 연결을 확인한 후 다시 시도해주세요."

        is NetworkTimeoutException ->
            "서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요."

        is ApiException,
        is InvalidResponseException,
            -> "여행 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요."

        else ->
            "여행 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요."
    }
