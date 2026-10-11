package com.manruhomerun.yadanbeopseok.baseball.navigation

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
import com.manruhomerun.yadanbeopseok.baseball.screen.BaseballScheduleScreen
import com.manruhomerun.yadanbeopseok.baseball.viewmodel.BaseballScheduleViewModel

/**
 * 경기 일정 화면과 [BaseballScheduleViewModel]을 연결합니다.
 *
 * 진입·복귀 조회, 당겨서 새로고침과 갱신 실패 안내를 처리합니다.
 * 세션 만료에 따른 로그인 화면 전환은 앱의 공통 세션 관찰이 처리합니다.
 *
 * @param onPlanClick 선택한 경기로 여행 생성을 시작할 때 호출됩니다.
 * null이면 여행 짜기 버튼이 비활성화됩니다.
 */
@Composable
fun BaseballScheduleRoute(
    initialTeamId: Long? = null,
    onBackClick: (() -> Unit)? = null,
    onPlanClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: BaseballScheduleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onScreenResumed(initialTeamId)
    }

    LaunchedEffect(uiState.hasLoadedSchedule, uiState.errorMessage) {
        if (!uiState.hasLoadedSchedule) return@LaunchedEffect
        val message = uiState.errorMessage ?: return@LaunchedEffect

        snackbarHostState.showSnackbar(message)
        viewModel.clearErrorMessage(message)
    }

    Box(modifier = modifier.fillMaxSize()) {
        BaseballScheduleScreen(
            uiState = uiState,
            onBackClick = onBackClick,
            onTeamSelected = viewModel::selectTeam,
            onPlanClick = onPlanClick,
            onRetryClick = viewModel::retry,
            onRefreshClick = viewModel::refresh,
            modifier = Modifier.fillMaxSize(),
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        )
    }
}
