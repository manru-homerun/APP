package com.manruhomerun.yadanbeopseok.baseball.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.baseball.screen.BaseballScheduleScreen
import com.manruhomerun.yadanbeopseok.baseball.viewmodel.BaseballScheduleNavigationEvent
import com.manruhomerun.yadanbeopseok.baseball.viewmodel.BaseballScheduleViewModel
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.LoginNavKey

/**
 * A·05 경기 일정 화면과 [BaseballScheduleViewModel]을 연결합니다.
 *
 * 화면 상태와 사용자 입력을 ViewModel에 연결하고,
 * 세션 만료와 여행 생성 화면 이동 요청을 처리합니다.
 *
 * @param navigator 세션 만료 시 로그인 화면으로 이동하는 Navigator입니다.
 * @param onPlanClick 선택한 경기로 여행 생성을 시작할 때 호출됩니다.
 * null이면 여행 짜기 버튼이 비활성화됩니다.
 */
@Composable
fun BaseballScheduleRoute(
    navigator: Navigator,
    onPlanClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: BaseballScheduleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    /*
     * 세션이 만료되면 인증이 필요한 기존 백스택을 제거하고
     * 로그인 화면으로 이동합니다.
     */
    LaunchedEffect(viewModel, navigator) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                BaseballScheduleNavigationEvent.NavigateToLogin -> {
                    navigator.resetTo(LoginNavKey)
                }
            }
        }
    }

    BaseballScheduleScreen(
        uiState = uiState,
        onTeamSelected = viewModel::selectTeam,
        onPlanClick = onPlanClick,
        onRetryClick = viewModel::retry,
        modifier = modifier,
    )
}
