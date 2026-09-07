package com.manruhomerun.yadanbeopseok.mypage.navigation

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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kakao.sdk.auth.TokenManagerProvider
import com.manruhomerun.yadanbeopseok.mypage.screen.MyPageScreen
import com.manruhomerun.yadanbeopseok.mypage.viewmodel.MyPageViewModel

/**
 * H·01 마이 화면과 [MyPageViewModel]을 연결합니다.
 *
 * 프로필과 여행 취향 상태를 수집하고 사용자 입력을 ViewModel 또는
 * 상위 내비게이션 콜백에 전달합니다.
 *
 * 로그아웃이나 회원 탈퇴로 로컬 인증 정보가 삭제되면
 * 앱의 공통 세션 관찰이 로그인 화면 전환을 처리합니다.
 */
@Composable
fun MyPageRoute(
    onProfileClick: () -> Unit,
    onDibsClick: () -> Unit,
    onTravelPreferenceClick: () -> Unit,
    onFriendsClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyPageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    /*
     * 프로필 수정 화면 등에서 돌아오면 마이페이지 정보를 다시 조회합니다.
     * 최초 진입 시에는 ViewModel이 이미 조회 중이므로 중복 요청되지 않습니다.
     */
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.retry()
    }

    /*
     * 기존 화면 데이터가 있는 상태에서 발생한 작업 오류만 스낵바로 표시합니다.
     * 최초 조회 실패는 MyPageScreen의 오류 화면에서 처리합니다.
     */
    LaunchedEffect(
        uiState.errorMessage,
        uiState.userProfile,
    ) {
        val errorMessage = uiState.errorMessage ?: return@LaunchedEffect
        if (uiState.userProfile == null) return@LaunchedEffect

        snackbarHostState.showSnackbar(errorMessage)
        viewModel.clearErrorMessage()
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        MyPageScreen(
            uiState = uiState,
            onRetryClick = viewModel::retry,
            onProfileClick = onProfileClick,
            onDibsClick = onDibsClick,
            onTravelPreferenceClick = onTravelPreferenceClick,
            onFriendsClick = onFriendsClick,
            onTermsClick = onTermsClick,
            onPrivacyPolicyClick = onPrivacyPolicyClick,
            onLogoutClick = {
                viewModel.logout(
                    kakaoAccessToken = getKakaoAccessToken(),
                )
            },
            onWithdrawalClick = viewModel::withdraw,
            modifier = Modifier.fillMaxSize(),
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
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
 * 카카오 SDK가 로컬에 저장한 현재 액세스 토큰을 반환합니다.
 */
private fun getKakaoAccessToken(): String {
    return TokenManagerProvider.instance.manager
        .getToken()
        ?.accessToken
        .orEmpty()
}
