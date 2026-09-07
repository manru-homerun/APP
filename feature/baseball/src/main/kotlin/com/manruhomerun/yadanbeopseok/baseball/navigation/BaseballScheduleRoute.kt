package com.manruhomerun.yadanbeopseok.baseball.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.baseball.screen.BaseballScheduleScreen
import com.manruhomerun.yadanbeopseok.baseball.viewmodel.BaseballScheduleViewModel

/**
 * A·05 경기 일정 화면과 [BaseballScheduleViewModel]을 연결합니다.
 *
 * 화면 상태와 사용자 입력을 ViewModel에 연결합니다.
 *
 * @param onPlanClick 선택한 경기로 여행 생성을 시작할 때 호출됩니다.
 * null이면 여행 짜기 버튼이 비활성화됩니다.
 */
@Composable
fun BaseballScheduleRoute(
    onPlanClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: BaseballScheduleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BaseballScheduleScreen(
        uiState = uiState,
        onTeamSelected = viewModel::selectTeam,
        onPlanClick = onPlanClick,
        onRetryClick = viewModel::retry,
        modifier = modifier,
    )
}
