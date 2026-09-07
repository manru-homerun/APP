package com.manruhomerun.yadanbeopseok.record.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.record.screen.TravelRecordDetailScreen
import com.manruhomerun.yadanbeopseok.record.viewmodel.TravelRecordDetailViewModel

/**
 * D·01b 지난 여행 상세 화면과 ViewModel을 연결합니다.
 *
 * 여행 상세와 경기·스티커 정보를 조회하고,
 * 뒤로가기와 D·04 사진 꾸미기 이동 콜백을 화면에 전달합니다.
 *
 * 세션 만료에 따른 로그인 이동은 앱의 공통 세션 관찰이 처리합니다.
 *
 * @param travelId 조회할 완료 여행의 고유 식별자입니다.
 * @param navigator 이전 화면으로 돌아갈 때 사용하는 Navigator입니다.
 * @param onDecoratePhotoClick D·04 사진 꾸미기 화면으로 이동하는 콜백입니다.
 * 스티커를 획득하지 않은 여행에서는 화면에 전달하지 않습니다.
 */
@Composable
fun TravelRecordDetailRoute(
    travelId: String,
    navigator: Navigator,
    onDecoratePhotoClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: TravelRecordDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(travelId, viewModel) {
        viewModel.loadTravel(travelId)
    }

    val decoratePhotoAction = if (uiState.canDecoratePhoto) {
        onDecoratePhotoClick
    } else {
        null
    }

    TravelRecordDetailScreen(
        uiState = uiState,
        onBackClick = navigator::navigateBack,
        onRetryClick = viewModel::retry,
        onStickerRetryClick = viewModel::retrySticker,
        onDecoratePhotoClick = decoratePhotoAction,
        modifier = modifier,
    )
}
