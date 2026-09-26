package com.manruhomerun.yadanbeopseok.notification.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.common.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.NotificationRepository
import com.manruhomerun.yadanbeopseok.notifications.NotificationRefreshNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** E·01 알림 목록을 조회하고 화면 상태로 변환합니다. */
@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    notificationRefreshNotifier: NotificationRefreshNotifier,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()
    private var loadNotificationsJob: Job? = null
    private var loadRequestId = 0L

    init {
        loadNotifications()

        viewModelScope.launch {
            notificationRefreshNotifier.events.collect {
                loadNotifications()
            }
        }
    }

    /** 알림 목록 조회를 다시 시도합니다. */
    fun retry() {
        refresh()
    }

    /** 화면 재개 또는 사용자 재시도 시 최신 알림 목록을 조회합니다. */
    fun refresh() {
        if (loadNotificationsJob?.isActive == true) return

        loadNotifications()
    }

    /** Snackbar로 오류를 표시한 뒤 저장된 오류 문구를 제거합니다. */
    fun clearErrorMessage() {
        _uiState.update { currentState ->
            currentState.copy(errorMessage = null)
        }
    }

    private fun loadNotifications() {
        loadNotificationsJob?.cancel()
        val requestId = ++loadRequestId

        _uiState.update { currentState ->
            currentState.copy(
                isLoading = currentState.notifications.isEmpty(),
                errorMessage = null,
            )
        }

        loadNotificationsJob = viewModelScope.launch {
            try {
                val notifications = notificationRepository
                    .getNotifications()
                    .sortedByDescending { notification -> notification.createdAt }
                    .map { notification -> notification.toNotificationListItem() }

                if (requestId != loadRequestId) return@launch

                _uiState.value = NotificationUiState(
                    notifications = notifications,
                    isLoading = false,
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                if (requestId != loadRequestId) return@launch

                _uiState.update { currentState ->
                    currentState.copy(isLoading = false)
                }
            } catch (exception: Exception) {
                if (requestId != loadRequestId) return@launch

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = exception.toNotificationErrorMessage(
                            defaultMessage = "알림을 불러오지 못했습니다.",
                        ),
                    )
                }
            }
        }
    }
}

/** 사용자에게 내부 예외 정보를 노출하지 않는 알림 기능 오류 문구를 만듭니다. */
internal fun Throwable.toNotificationErrorMessage(defaultMessage: String): String = when (this) {
    is NetworkConnectionException -> "인터넷 연결을 확인한 후 다시 시도해주세요."
    is NetworkTimeoutException -> "서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요."
    else -> defaultMessage
}
