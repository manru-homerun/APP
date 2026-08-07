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
import kotlinx.coroutines.launch

/**
 * 앱 시작 시 저장된 인증 정보를 확인한 결과입니다.
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
 * 앱 실행 시 저장된 야단법석 세션을 확인합니다.
 */
@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _startupState = MutableStateFlow<AppStartupState>(AppStartupState.Checking)

    val startupState: StateFlow<AppStartupState> = _startupState.asStateFlow()

    private var restoreSessionJob: Job? = null

    init {
        restoreSession()
    }

    /**
     * 저장된 인증 정보를 확인하고 앱의 최초 화면 상태를 결정합니다.
     *
     * 오류 화면에서 재시도할 때도 이 함수를 사용합니다.
     */
    fun restoreSession() {
        if (restoreSessionJob?.isActive == true) {
            return
        }

        _startupState.value = AppStartupState.Checking

        restoreSessionJob =
            viewModelScope.launch {
                try {
                    _startupState.value =
                        when (authRepository.restoreSession()) {
                            AuthSessionState.LOGGED_OUT -> AppStartupState.LoginRequired
                            AuthSessionState.ONBOARDING_REQUIRED -> AppStartupState.OnboardingRequired
                            AuthSessionState.AUTHENTICATED -> AppStartupState.Authenticated
                        }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Exception) {
                    _startupState.value = AppStartupState.Error
                }
            }
    }
}
