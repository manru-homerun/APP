package com.manruhomerun.yadanbeopseok.travel.spot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.error.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.error.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.common.error.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.TravelSpotRepository
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

/** 관광지 상세 화면에서 발생하는 일회성 이동 이벤트입니다. */
sealed interface TravelSpotDetailNavigationEvent {
    /** 인증 정보가 만료되어 로그인 화면으로 이동해야 합니다. */
    data object NavigateToLogin : TravelSpotDetailNavigationEvent
}

/** 관광지 상세 정보와 찜 상태를 관리합니다. */
@HiltViewModel
class TravelSpotDetailViewModel @Inject constructor(
    private val travelSpotRepository: TravelSpotRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TravelSpotDetailUiState())
    val uiState: StateFlow<TravelSpotDetailUiState> = _uiState.asStateFlow()

    private val _navigationEvents = Channel<TravelSpotDetailNavigationEvent>(Channel.BUFFERED)
    val navigationEvents: Flow<TravelSpotDetailNavigationEvent> = _navigationEvents.receiveAsFlow()

    private var currentSpotId: String? = null
    private var loadJob: Job? = null
    private var dibsJob: Job? = null

    /** 전달받은 관광지 ID에 해당하는 상세 정보를 조회합니다. */
    fun loadTravelSpot(spotId: String) {
        val normalizedSpotId = spotId.trim()

        if (normalizedSpotId.isEmpty()) {
            currentSpotId = null
            loadJob?.cancel()
            dibsJob?.cancel()

            _uiState.value = TravelSpotDetailUiState(
                isLoading = false,
                errorMessage = "관광지 정보를 확인할 수 없습니다.",
            )
            return
        }

        val isAlreadyLoaded = currentSpotId == normalizedSpotId && _uiState.value.detail != null

        if (isAlreadyLoaded) return

        currentSpotId = normalizedSpotId
        requestTravelSpot(normalizedSpotId)
    }

    /** 현재 관광지 상세 조회를 다시 시도합니다. */
    fun retry() {
        if (_uiState.value.isLoading) return
        requestTravelSpot(currentSpotId ?: return)
    }

    /** 현재 찜 상태에 따라 찜 등록 또는 취소를 요청합니다. */
    fun toggleDibs() {
        val detail = _uiState.value.detail ?: return
        if (_uiState.value.isUpdatingDibs) return

        val spotId = detail.spot.id
        val wasDibs = detail.spot.dibs

        _uiState.update {
            it.copy(isUpdatingDibs = true, errorMessage = null)
        }

        dibsJob = viewModelScope.launch {
            try {
                if (wasDibs) {
                    travelSpotRepository.deleteTravelSpotDibs(spotId)
                } else {
                    travelSpotRepository.addTravelSpotDibs(spotId)
                }

                if (currentSpotId == spotId) {
                    _uiState.update { state ->
                        val currentDetail = state.detail ?: return@update state
                        state.copy(
                            detail = currentDetail.copy(
                                spot = currentDetail.spot.copy(dibs = !wasDibs),
                            ),
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: SessionExpiredException) {
                _navigationEvents.send(
                    TravelSpotDetailNavigationEvent.NavigateToLogin,
                )
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = exception.toTravelSpotErrorMessage(
                            fallbackMessage = "찜 상태를 변경하지 못했습니다.",
                        ),
                    )
                }
            } finally {
                if (currentSpotId == spotId) {
                    _uiState.update { it.copy(isUpdatingDibs = false) }
                }
            }
        }
    }

    /** Snackbar에 오류를 표시한 뒤 저장된 문구를 제거합니다. */
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /** Repository를 통해 관광지 상세 정보를 조회합니다. */
    private fun requestTravelSpot(spotId: String) {
        loadJob?.cancel()
        dibsJob?.cancel()

        _uiState.value = TravelSpotDetailUiState(isLoading = true)

        loadJob = viewModelScope.launch {
            try {
                val detail = travelSpotRepository.getTravelSpotDetail(spotId)

                if (currentSpotId == spotId) {
                    _uiState.value = TravelSpotDetailUiState(
                        detail = detail,
                        isLoading = false,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: SessionExpiredException) {
                _uiState.update { it.copy(isLoading = false) }
                _navigationEvents.send(
                    TravelSpotDetailNavigationEvent.NavigateToLogin,
                )
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.toTravelSpotErrorMessage(
                            fallbackMessage = "관광지 정보를 불러오지 못했습니다.",
                        ),
                    )
                }
            }
        }
    }
}

/** 내부 예외를 사용자에게 표시할 안전한 문구로 변환합니다. */
private fun Throwable.toTravelSpotErrorMessage(fallbackMessage: String): String =
    when (this) {
        is NetworkConnectionException ->
            "인터넷 연결을 확인한 후 다시 시도해주세요."

        is NetworkTimeoutException ->
            "서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요."

        else ->
            "$fallbackMessage 잠시 후 다시 시도해주세요."
    }
