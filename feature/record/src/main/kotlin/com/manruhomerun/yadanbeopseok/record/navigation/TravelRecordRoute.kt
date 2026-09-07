package com.manruhomerun.yadanbeopseok.record.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.record.screen.TravelRecordScreen
import com.manruhomerun.yadanbeopseok.record.viewmodel.TravelRecordViewModel

/**
 * D·01 여행 기록 화면과 [TravelRecordViewModel]을 연결합니다.
 *
 * 완료 여행 목록과 시즌 선택을 화면에 전달하고,
 * 완료 여행 선택 동작을 처리합니다.
 *
 * @param onTravelClick 선택한 완료 여행의 D·01b 화면을 엽니다.
 */
@Composable
fun TravelRecordRoute(
    onTravelClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TravelRecordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    /*
     * D·01b 또는 D·04에서 돌아왔을 때 완료 여행과
     * 스티커 획득 상태를 최신 서버 응답으로 갱신합니다.
     */
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    TravelRecordScreen(
        uiState = uiState,
        onSeasonSelected = viewModel::selectSeason,
        onTravelClick = onTravelClick,
        onRetryClick = viewModel::retry,
        modifier = modifier,
    )
}
