package com.manruhomerun.yadanbeopseok.auth.navigation

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.manruhomerun.yadanbeopseok.auth.screen.LoginScreen
import com.manruhomerun.yadanbeopseok.auth.viewmodel.LoginNavigationEvent
import com.manruhomerun.yadanbeopseok.auth.viewmodel.LoginViewModel
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.HomeNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.TermsAgreementNavKey

/**
 * 로그인 화면의 상태 수집, 카카오 SDK 실행 및 화면 이동을 연결합니다.
 *
 * 화면 UI는 [LoginScreen], 백엔드 로그인 처리는 [LoginViewModel]에 위임합니다.
 */
@Composable
fun LoginRoute(
    navigator: Navigator,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    /*
     * 로그인 결과에 따라 온보딩 또는 홈으로 이동합니다.
     * 기존 회원은 뒤로 가기로 로그인 화면에 돌아오지 않도록 백스택을 초기화합니다.
     */
    LaunchedEffect(viewModel, navigator) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                LoginNavigationEvent.NavigateToTermsAgreement -> {
                    navigator.navigate(TermsAgreementNavKey)
                }

                LoginNavigationEvent.NavigateToHome -> {
                    navigator.resetTo(HomeNavKey)
                }
            }
        }
    }

    /*
     * ViewModel에서 전달된 로그인 오류를 한 번 표시한 뒤 제거합니다.
     */
    LaunchedEffect(uiState.errorMessage) {
        val errorMessage = uiState.errorMessage ?: return@LaunchedEffect

        snackbarHostState.showSnackbar(
            message = errorMessage,
        )
        viewModel.clearErrorMessage()
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        LoginScreen(
            onKakaoLoginClick = {
                if (viewModel.startKakaoLogin()) {
                    requestKakaoLogin(
                        context = context,
                        onSuccess = { kakaoAccessToken ->
                            viewModel.loginWithKakao(
                                kakaoAccessToken = kakaoAccessToken,
                            )
                        },
                        onCancel = viewModel::cancelKakaoLogin,
                        onFailure = viewModel::failKakaoLogin,
                    )
                }
            },
            loginEnabled = !uiState.isLoading,
            modifier = Modifier.fillMaxSize(),
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(
                        horizontal = 20.dp,
                        vertical = 12.dp,
                    ),
        )
    }
}

/**
 * 카카오톡 로그인이 가능하면 카카오톡으로 인증을 요청합니다.
 *
 * 카카오톡을 사용할 수 없거나 카카오톡 로그인에 실패하면
 * 카카오계정 로그인으로 전환합니다. 사용자가 직접 취소한 경우에는
 * 카카오계정 로그인을 다시 실행하지 않습니다.
 */
private fun requestKakaoLogin(
    context: Context,
    onSuccess: (String) -> Unit,
    onCancel: () -> Unit,
    onFailure: () -> Unit,
) {
    try {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                when {
                    error.isKakaoLoginCancelled() -> {
                        onCancel()
                    }

                    error != null -> {
                        requestKakaoAccountLogin(
                            context = context,
                            onSuccess = onSuccess,
                            onCancel = onCancel,
                            onFailure = onFailure,
                        )
                    }

                    token != null -> {
                        onSuccess(token.accessToken)
                    }

                    else -> {
                        onFailure()
                    }
                }
            }
        } else {
            requestKakaoAccountLogin(
                context = context,
                onSuccess = onSuccess,
                onCancel = onCancel,
                onFailure = onFailure,
            )
        }
    } catch (_: Exception) {
        onFailure()
    }
}

/**
 * 기본 브라우저를 사용하는 카카오계정 로그인을 요청합니다.
 */
private fun requestKakaoAccountLogin(
    context: Context,
    onSuccess: (String) -> Unit,
    onCancel: () -> Unit,
    onFailure: () -> Unit,
) {
    val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        when {
            error.isKakaoLoginCancelled() -> {
                onCancel()
            }

            error != null -> {
                onFailure()
            }

            token != null -> {
                onSuccess(token.accessToken)
            }

            else -> {
                onFailure()
            }
        }
    }

    try {
        UserApiClient.instance.loginWithKakaoAccount(
            context = context,
            callback = callback,
        )
    } catch (_: Exception) {
        onFailure()
    }
}

/**
 * 카카오 SDK 오류가 사용자의 명시적인 로그인 취소인지 확인합니다.
 */
private fun Throwable?.isKakaoLoginCancelled(): Boolean =
    this is ClientError &&
        reason == ClientErrorCause.Cancelled
