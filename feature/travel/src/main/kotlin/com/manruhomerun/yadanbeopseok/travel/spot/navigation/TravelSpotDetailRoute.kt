package com.manruhomerun.yadanbeopseok.travel.spot.navigation

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.travel.spot.screen.TravelSpotDetailScreen
import com.manruhomerun.yadanbeopseok.travel.spot.viewmodel.TravelSpotDetailViewModel

/**
 * 관광지 상세 화면과 ViewModel을 연결합니다.
 *
 * 관광지 상세 조회, 찜 상태 변경, 뒤로가기와 오류 메시지 표시를 처리합니다.
 * 세션 만료에 따른 로그인 화면 전환은 앱의 공통 세션 관찰이 처리합니다.
 */
@Composable
fun TravelSpotDetailRoute(
    travelSpotId: String,
    navigator: Navigator,
    modifier: Modifier = Modifier,
    viewModel: TravelSpotDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(travelSpotId, viewModel) {
        viewModel.loadTravelSpot(travelSpotId)
    }

    LaunchedEffect(uiState.errorMessage, uiState.hasDetail) {
        val errorMessage = uiState.errorMessage ?: return@LaunchedEffect

        if (!uiState.hasDetail) {
            return@LaunchedEffect
        }

        snackbarHostState.showSnackbar(message = errorMessage)
        viewModel.clearErrorMessage()
    }

    Box(modifier = modifier.fillMaxSize()) {
        TravelSpotDetailScreen(
            uiState = uiState,
            onBackClick = navigator::navigateBack,
            onDibsClick = viewModel::toggleDibs,
            onRetryClick = viewModel::retry,
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
