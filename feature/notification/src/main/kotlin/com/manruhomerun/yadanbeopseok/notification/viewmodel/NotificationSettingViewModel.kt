package com.manruhomerun.yadanbeopseok.notification.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.NotificationRepository
import com.manruhomerun.yadanbeopseok.model.NotificationSetting
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** E·02 알림 설정 조회와 수정을 처리합니다. */
@HiltViewModel
class NotificationSettingViewModel @Inject constructor(private val notificationRepository: NotificationRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow(NotificationSettingUiState())
    val uiState: StateFlow<NotificationSettingUiState> = _uiState.asStateFlow()

    init {
        loadSetting()
    }

    /** 알림 설정 조회를 다시 시도합니다. */
    fun retry() {
        loadSetting()
    }

    /** 친구 신청과 친구 수락 알림을 함께 설정합니다. */
    fun updateFriendNotification(enabled: Boolean) {
        val currentSetting = _uiState.value.setting ?: return
        updateSetting(currentSetting.copy(friendNotificationEnabled = enabled))
    }

    /** 매주 월요일 오전 9시에 발송되는 경기 일정 알림을 설정합니다. */
    fun updateWeeklyTeamScheduleNotification(enabled: Boolean) {
        val currentSetting = _uiState.value.setting ?: return
        updateSetting(
            currentSetting.copy(
                weeklyTeamScheduleNotificationEnabled = enabled,
            ),
        )
    }

    fun clearUserMessage() {
        _uiState.update { currentState ->
            currentState.copy(userMessage = null)
        }
    }

    private fun loadSetting() {
        if (_uiState.value.isLoading && _uiState.value.setting != null) return

        _uiState.update { currentState ->
            currentState.copy(
                isLoading = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            try {
                val setting = notificationRepository.getNotificationSetting()
                _uiState.value = NotificationSettingUiState(
                    setting = setting,
                    isLoading = false,
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                _uiState.update { currentState ->
                    currentState.copy(isLoading = false)
                }
            } catch (exception: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = exception.toNotificationErrorMessage(
                            defaultMessage = "알림 설정을 불러오지 못했습니다.",
                        ),
                    )
                }
            }
        }
    }

    private fun updateSetting(updatedSetting: NotificationSetting) {
        val currentState = _uiState.value
        val previousSetting = currentState.setting ?: return
        if (currentState.isUpdating || previousSetting == updatedSetting) return

        _uiState.update { state ->
            state.copy(
                setting = updatedSetting,
                isUpdating = true,
                userMessage = null,
            )
        }

        viewModelScope.launch {
            try {
                notificationRepository.updateNotificationSetting(updatedSetting)
                _uiState.update { state ->
                    state.copy(isUpdating = false)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                _uiState.update { state ->
                    state.copy(
                        setting = previousSetting,
                        isUpdating = false,
                    )
                }
            } catch (exception: Exception) {
                _uiState.update { state ->
                    state.copy(
                        setting = previousSetting,
                        isUpdating = false,
                        userMessage = exception.toNotificationErrorMessage(
                            defaultMessage = "알림 설정을 변경하지 못했습니다.",
                        ),
                    )
                }
            }
        }
    }
}
