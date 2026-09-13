package com.manruhomerun.yadanbeopseok.mypage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.common.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.UserRepository
import com.manruhomerun.yadanbeopseok.model.ProfileRegion
import com.manruhomerun.yadanbeopseok.model.TravelStyleScore
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

/** H·03에서 발생하는 일회성 화면 이벤트입니다. */
sealed interface TravelPreferenceEditEvent {
    /** 변경한 여행 취향이 서버에 저장됐습니다. */
    data object Saved : TravelPreferenceEditEvent
}

/** H·03의 여행 취향 조회, 입력 변경과 저장을 관리합니다. */
@HiltViewModel
class TravelPreferenceEditViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TravelPreferenceEditUiState())
    val uiState: StateFlow<TravelPreferenceEditUiState> = _uiState.asStateFlow()

    private val _events = Channel<TravelPreferenceEditEvent>(Channel.BUFFERED)
    val events: Flow<TravelPreferenceEditEvent> = _events.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        loadTravelPreference()
    }

    /** 여행 취향 조회를 다시 시도합니다. */
    fun retry() {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isSaving) return

        loadTravelPreference()
    }

    /** 저장할 거주 지역을 변경합니다. */
    fun selectResidenceRegion(region: ProfileRegion) {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isSaving) return
        if (!region.isAvailableForResidence) return

        _uiState.update {
            it.copy(
                residenceRegion = region,
                errorMessage = null,
            )
        }
    }

    /** 저장할 자연·도시 여행 성향 점수를 변경합니다. */
    fun updateTravelStyleScore(score: TravelStyleScore) {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isSaving) return

        _uiState.update {
            it.copy(
                travelStyleScore = score,
                errorMessage = null,
            )
        }
    }

    /** 선호 여행 지역을 선택하거나 선택 해제합니다. */
    fun togglePreferredTravelRegion(region: ProfileRegion) {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isSaving) return
        if (!region.isAvailableForPreferredTravel) return

        val updatedRegions = if (region in currentState.preferredTravelRegions) {
            currentState.preferredTravelRegions - region
        } else {
            currentState.preferredTravelRegions + region
        }

        _uiState.update {
            it.copy(
                preferredTravelRegions = updatedRegions,
                errorMessage = null,
            )
        }
    }

    /** 현재 입력한 여행 취향을 서버에 저장합니다. */
    fun saveTravelPreference() {
        val currentState = _uiState.value
        if (!currentState.isSaveEnabled) return

        val originalPreference = currentState.originalPreference ?: return
        val residenceRegion = currentState.residenceRegion ?: return
        val updatedPreference = originalPreference.copy(
            residenceRegion = residenceRegion,
            travelStyleScore = currentState.travelStyleScore,
            preferredTravelRegions = currentState.preferredTravelRegions,
        )

        _uiState.update {
            it.copy(
                isSaving = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            try {
                val savedPreference = userRepository.updateMyTravelPreference(updatedPreference)

                _uiState.update {
                    it.copy(
                        originalPreference = savedPreference,
                        residenceRegion = savedPreference.residenceRegion,
                        travelStyleScore = savedPreference.travelStyleScore,
                        preferredTravelRegions = savedPreference.preferredTravelRegions,
                        isSaving = false,
                    )
                }

                _events.send(TravelPreferenceEditEvent.Saved)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                _uiState.update {
                    it.copy(isSaving = false)
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = exception.toTravelPreferenceEditErrorMessage(
                            defaultMessage = "여행 취향을 저장하지 못했습니다. 잠시 후 다시 시도해주세요.",
                        ),
                    )
                }
            }
        }
    }

    /** 화면에 표시된 오류 문구를 제거합니다. */
    fun clearErrorMessage() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    /** 현재 로그인한 사용자의 여행 취향을 조회합니다. */
    private fun loadTravelPreference() {
        loadJob?.cancel()

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
            )
        }

        loadJob = viewModelScope.launch {
            try {
                val preference = userRepository.getMyTravelPreference()

                _uiState.update {
                    it.copy(
                        originalPreference = preference,
                        residenceRegion = preference.residenceRegion,
                        travelStyleScore = preference.travelStyleScore,
                        preferredTravelRegions = preference.preferredTravelRegions,
                        isLoading = false,
                        isSaving = false,
                        errorMessage = null,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                _uiState.update {
                    it.copy(isLoading = false)
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.toTravelPreferenceEditErrorMessage(
                            defaultMessage = "여행 취향을 불러오지 못했습니다. 다시 시도해주세요.",
                        ),
                    )
                }
            }
        }
    }
}

/** 내부 예외 정보를 노출하지 않는 취향 수정 오류 문구로 변환합니다. */
private fun Exception.toTravelPreferenceEditErrorMessage(defaultMessage: String): String =
    when (this) {
        is NetworkConnectionException -> "인터넷 연결을 확인한 후 다시 시도해주세요."
        is NetworkTimeoutException -> "서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요."
        else -> defaultMessage
    }
