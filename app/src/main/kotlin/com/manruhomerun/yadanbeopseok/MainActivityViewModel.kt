package com.manruhomerun.yadanbeopseok

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.ApiException
import com.manruhomerun.yadanbeopseok.common.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.data.repository.AuthRepository
import com.manruhomerun.yadanbeopseok.data.repository.AuthSessionState
import com.manruhomerun.yadanbeopseok.data.repository.NotificationRepository
import com.manruhomerun.yadanbeopseok.notifications.FirebasePushRegistrationManager
import com.manruhomerun.yadanbeopseok.notifications.NotificationPayload
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * 앱의 인증 상태에 따라 표시할 최상위 화면 상태입니다.
 */
sealed interface AppStartupState {
    /** 저장된 세션을 확인하고 있습니다. */
    data object Checking : AppStartupState

    /** 유효한 세션이 없어 로그인이 필요합니다. */
    data object LoginRequired : AppStartupState

    /** 로그인은 유지되지만 온보딩을 완료해야 합니다. */
    data object OnboardingRequired : AppStartupState

    /** 로그인과 온보딩이 모두 완료된 상태입니다. */
    data object Authenticated : AppStartupState

    /** 세션 확인 중 재시도할 수 있는 오류가 발생했습니다. */
    data object Error : AppStartupState
}

/**
 * 앱 시작 시 저장된 세션을 복원하고 이후 인증 상태 변경을 관찰합니다.
 */
@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository,
    private val pushRegistrationManager: FirebasePushRegistrationManager,
) : ViewModel() {
    private val _startupState = MutableStateFlow<AppStartupState>(AppStartupState.Checking)
    val startupState: StateFlow<AppStartupState> = _startupState.asStateFlow()

    private val _pendingNotification = MutableStateFlow<NotificationPayload?>(null)
    val pendingNotification: StateFlow<NotificationPayload?> = _pendingNotification.asStateFlow()

    private var restoreSessionJob: Job? = null
    private var observeSessionJob: Job? = null
    private var pushRegistrationJob: Job? = null
    private var pushServerRegistrationJob: Job? = null
    private var pushUnregistrationJob: Job? = null
    private var hasRequestedPushRegistration = false

    init {
        observeRegisteredInstallationIds()
        restoreSession()
    }

    /**
     * 저장된 인증 정보와 온보딩 상태로 최초 화면을 결정합니다.
     *
     * access token 만료는 이후 보호된 API의 401 응답에서 처리하므로,
     * 앱 시작 시에는 별도의 토큰 재발급 요청을 보내지 않습니다.
     */
    fun restoreSession() {
        if (restoreSessionJob?.isActive == true) {
            return
        }

        observeSessionJob?.cancel()
        _startupState.value = AppStartupState.Checking

        restoreSessionJob =
            viewModelScope.launch {
                try {
                    val sessionState = authRepository.restoreSession()

                    updateSessionState(sessionState)

                    observeSessionChanges()
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Exception) {
                    _startupState.value = AppStartupState.Error
                }
            }
    }

    /**
     * 로그인, 온보딩 완료, 로그아웃과 세션 만료로 발생하는
     * DataStore 인증 정보 변경을 계속 관찰합니다.
     */
    private fun observeSessionChanges() {
        observeSessionJob?.cancel()

        observeSessionJob =
            viewModelScope.launch {
                try {
                    authRepository.observeSessionState().collect { sessionState ->
                        updateSessionState(sessionState)
                    }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Exception) {
                    _startupState.value = AppStartupState.Error
                }
            }
    }

    /** 시스템 알림 클릭으로 전달된 화면 이동 정보를 보관합니다. */
    fun setPendingNotification(payload: NotificationPayload) {
        _pendingNotification.value = payload
    }

    /** 알림 목적지 이동을 마친 뒤 동일한 이동 정보가 다시 처리되지 않게 합니다. */
    fun consumePendingNotification() {
        _pendingNotification.value = null
    }

    private fun updateSessionState(sessionState: AuthSessionState) {
        _startupState.value = sessionState.toAppStartupState()

        if (sessionState == AuthSessionState.AUTHENTICATED) {
            pushUnregistrationJob?.cancel()
            pushUnregistrationJob = null
            pushRegistrationManager.enableRegistration()
            requestPushRegistration()
        } else {
            pushRegistrationJob?.cancel()
            pushRegistrationJob = null
            pushServerRegistrationJob?.cancel()
            pushServerRegistrationJob = null
            hasRequestedPushRegistration = false

            if (pushRegistrationManager.disableRegistration()) {
                unregisterPushInstallation()
            }
        }
    }

    /** Firebase가 전달한 FID를 인증된 사용자와 연결합니다. */
    private fun observeRegisteredInstallationIds() {
        viewModelScope.launch {
            pushRegistrationManager.registeredInstallationIds.collect { installationId ->
                val isAuthenticated = _startupState.value == AppStartupState.Authenticated
                if (!isAuthenticated || installationId.isNullOrBlank()) {
                    pushServerRegistrationJob?.cancel()
                    pushServerRegistrationJob = null
                    return@collect
                }

                pushServerRegistrationJob?.cancel()
                pushServerRegistrationJob = viewModelScope.launch {
                    try {
                        retryPushOperation {
                            notificationRepository.registerPushInstallation(
                                installationId = installationId,
                                appVersion = BuildConfig.VERSION_NAME,
                            )
                        }
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (_: Exception) {
                        // 재시도할 수 없는 서버 등록 실패는 다음 FID에서 다시 처리합니다.
                    }
                }
            }
        }
    }

    /** 인증된 설치를 FCM에 등록하도록 요청합니다. */
    private fun requestPushRegistration() {
        if (hasRequestedPushRegistration || pushRegistrationJob?.isActive == true) return

        hasRequestedPushRegistration = true

        pushRegistrationJob = viewModelScope.launch {
            try {
                retryPushOperation {
                    pushRegistrationManager.register()
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                // FCM 등록 실패는 다음 인증 세션에서 다시 시도합니다.
            }
        }
    }

    /** 로그아웃 또는 세션 만료 후 로컬 FCM 등록을 해제합니다. */
    private fun unregisterPushInstallation() {
        if (pushUnregistrationJob?.isActive == true) return

        pushUnregistrationJob = viewModelScope.launch {
            try {
                pushRegistrationManager.unregister()
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                // 로컬 FCM 해제 실패가 로그인 화면 이동을 막지 않게 합니다.
            }
        }
    }

    /** 인증 세션이 유지되는 동안 일시적인 FCM·네트워크 오류를 재시도합니다. */
    private suspend fun retryPushOperation(operation: suspend () -> Unit) {
        var attempt = 0

        while (true) {
            try {
                operation()
                return
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                if (!exception.isRetryablePushFailure()) {
                    throw exception
                }

                val retryExponent = attempt.coerceAtMost(MAX_PUSH_RETRY_EXPONENT)
                val retryDelay = (
                    INITIAL_PUSH_RETRY_DELAY_MILLIS * (1L shl retryExponent)
                    ).coerceAtMost(MAX_PUSH_RETRY_DELAY_MILLIS)

                delay(retryDelay)
                attempt += 1
            }
        }
    }
}

/**
 * Repository의 인증 상태를 앱 최상위 화면 상태로 변환합니다.
 */
private fun AuthSessionState.toAppStartupState(): AppStartupState =
    when (this) {
        AuthSessionState.LOGGED_OUT ->
            AppStartupState.LoginRequired

        AuthSessionState.ONBOARDING_REQUIRED ->
            AppStartupState.OnboardingRequired

        AuthSessionState.AUTHENTICATED ->
            AppStartupState.Authenticated
    }

private fun Exception.isRetryablePushFailure(): Boolean = when (this) {
    is IOException,
    is NetworkConnectionException,
    is NetworkTimeoutException,
    -> true

    is ApiException -> statusCode in 500..599
    else -> false
}

private const val INITIAL_PUSH_RETRY_DELAY_MILLIS = 1_000L
private const val MAX_PUSH_RETRY_DELAY_MILLIS = 60_000L
private const val MAX_PUSH_RETRY_EXPONENT = 6
