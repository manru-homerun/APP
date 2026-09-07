package com.manruhomerun.yadanbeopseok

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.data.repository.AuthRepository
import com.manruhomerun.yadanbeopseok.data.repository.AuthSessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
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
) : ViewModel() {
    private val _startupState = MutableStateFlow<AppStartupState>(AppStartupState.Checking)
    val startupState: StateFlow<AppStartupState> = _startupState.asStateFlow()

    private var restoreSessionJob: Job? = null
    private var observeSessionJob: Job? = null

    init {
        restoreSession()
    }

    /**
     * 저장된 토큰의 만료 상태를 확인하고 최초 화면을 결정합니다.
     *
     * 초기 복원이 완료된 뒤에만 세션 관찰을 시작하여,
     * 만료된 저장 토큰이 잠시 유효한 세션으로 처리되는 것을 방지합니다.
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

                    _startupState.value =
                        sessionState.toAppStartupState()

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
                        _startupState.value =
                            sessionState.toAppStartupState()
                    }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Exception) {
                    _startupState.value = AppStartupState.Error
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
