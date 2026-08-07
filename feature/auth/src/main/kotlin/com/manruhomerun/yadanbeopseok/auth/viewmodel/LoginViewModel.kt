package com.manruhomerun.yadanbeopseok.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.error.ApiException
import com.manruhomerun.yadanbeopseok.common.error.InvalidResponseException
import com.manruhomerun.yadanbeopseok.common.error.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.error.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.data.repository.AuthRepository
import com.manruhomerun.yadanbeopseok.model.LoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 로그인 화면에 표시할 상태입니다.
 *
 * @property isLoading 카카오 인증 또는 백엔드 로그인이 진행 중인지 나타냅니다.
 * @property errorMessage 사용자에게 안내할 로그인 오류 문구입니다.
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * 로그인 결과에 따라 화면에서 처리할 일회성 이동 이벤트입니다.
 */
sealed interface LoginNavigationEvent {
    /**
     * 신규 회원 또는 온보딩을 완료하지 않은 회원을 약관 동의 화면으로 이동시킵니다.
     */
    data object NavigateToTermsAgreement : LoginNavigationEvent

    /**
     * 온보딩을 완료한 기존 회원을 홈 화면으로 이동시킵니다.
     */
    data object NavigateToHome : LoginNavigationEvent
}

/**
 * 카카오 로그인과 야단법석 백엔드 로그인을 처리합니다.
 *
 * 카카오 SDK 화면 실행은 Android Context가 필요한 UI 계층에서 담당하고,
 * SDK가 발급한 카카오 액세스 토큰만 [loginWithKakao]에 전달합니다.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _navigationEvents = Channel<LoginNavigationEvent>(capacity = Channel.BUFFERED)
    val navigationEvents: Flow<LoginNavigationEvent> = _navigationEvents.receiveAsFlow()

    private var loginJob: Job? = null

    /**
     * 카카오 SDK 로그인을 시작하기 전에 호출합니다.
     *
     * 이미 로그인 중이면 false를 반환하여 중복 SDK 실행을 방지합니다.
     */
    fun startKakaoLogin(): Boolean {
        if (_uiState.value.isLoading) return false

        _uiState.update { currentState ->
            currentState.copy(
                isLoading = true,
                errorMessage = null,
            )
        }

        return true
    }

    /**
     * 카카오 SDK에서 받은 액세스 토큰으로 야단법석 백엔드에 로그인합니다.
     *
     * @param kakaoAccessToken 카카오 SDK가 발급한 카카오 액세스 토큰입니다.
     * @param fcmToken 알림 전송에 사용할 FCM 토큰이며 아직 없으면 null입니다.
     */
    fun loginWithKakao(
        kakaoAccessToken: String,
        fcmToken: String? = null,
    ) {
        if (loginJob?.isActive == true) return

        if (kakaoAccessToken.isBlank()) {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    errorMessage = "카카오 로그인 정보를 확인할 수 없습니다. 다시 시도해주세요.",
                )
            }
            return
        }

        _uiState.update { currentState ->
            currentState.copy(
                isLoading = true,
                errorMessage = null,
            )
        }

        loginJob = viewModelScope.launch {
                try {
                    val loginResult = authRepository.loginWithKakao(
                            kakaoAccessToken = kakaoAccessToken,
                            fcmToken = fcmToken,
                        )

                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            errorMessage = null,
                        )
                    }

                    _navigationEvents.send(
                        loginResult.toNavigationEvent(),
                    )
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            errorMessage = exception.toLoginErrorMessage(),
                        )
                    }
                }
            }
    }

    /**
     * 사용자가 카카오 인증 화면을 닫았을 때 로그인 진행 상태를 해제합니다.
     *
     * 사용자가 직접 취소한 것이므로 오류 문구는 표시하지 않습니다.
     */
    fun cancelKakaoLogin() {
        if (loginJob?.isActive == true) return

        _uiState.update { currentState ->
            currentState.copy(
                isLoading = false,
                errorMessage = null,
            )
        }
    }

    /**
     * 카카오 SDK 인증 자체가 실패했을 때 호출합니다.
     */
    fun failKakaoLogin() {
        if (loginJob?.isActive == true) return

        _uiState.update { currentState ->
            currentState.copy(
                isLoading = false,
                errorMessage = "카카오 로그인에 실패했습니다. 다시 시도해주세요.",
            )
        }
    }

    /**
     * 화면에서 오류 문구를 표시한 후 현재 오류 상태를 제거합니다.
     */
    fun clearErrorMessage() {
        _uiState.update { currentState ->
            currentState.copy(
                errorMessage = null,
            )
        }
    }
}

/**
 * 로그인 결과를 다음 화면 이동 이벤트로 변환합니다.
 */
private fun LoginResult.toNavigationEvent(): LoginNavigationEvent =
    if (isNewUser || !onboardingCompleted) {
        LoginNavigationEvent.NavigateToTermsAgreement
    } else {
        LoginNavigationEvent.NavigateToHome
    }

/**
 * 공통 예외를 로그인 화면에 표시할 사용자 안내 문구로 변환합니다.
 *
 * 내부 예외 메시지나 토큰 값은 화면과 로그에 노출하지 않습니다.
 */
private fun Exception.toLoginErrorMessage(): String =
    when (this) {
        is NetworkConnectionException ->
            "인터넷 연결을 확인한 후 다시 시도해주세요."

        is NetworkTimeoutException ->
            "서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요."

        is ApiException ->
            "로그인에 실패했습니다. 잠시 후 다시 시도해주세요."

        is InvalidResponseException ->
            "로그인 응답을 확인할 수 없습니다. 잠시 후 다시 시도해주세요."

        else ->
            "로그인에 실패했습니다. 잠시 후 다시 시도해주세요."
    }
