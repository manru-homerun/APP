package com.manruhomerun.yadanbeopseok.travel.navigation

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
import com.manruhomerun.yadanbeopseok.navigation.route.LoginNavKey
import com.manruhomerun.yadanbeopseok.travel.screen.TravelSpotDetailScreen
import com.manruhomerun.yadanbeopseok.travel.viewmodel.TravelSpotDetailNavigationEvent
import com.manruhomerun.yadanbeopseok.travel.viewmodel.TravelSpotDetailViewModel

/**
 * 관광지 상세 화면과 [TravelSpotDetailViewModel]을 연결합니다.
 *
 * 관광지 ID를 사용해 상세 정보를 조회하고 찜 상태 변경,
 * 세션 만료 및 화면 이동을 처리합니다.
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

    // NavKey로 전달받은 관광지 ID가 변경되면 상세 정보를 조회합니다.
    LaunchedEffect(travelSpotId, viewModel) {
        viewModel.loadTravelSpot(travelSpotId)
    }

    // 세션이 만료되면 전체 백스택을 제거하고 로그인 화면으로 이동합니다.
    LaunchedEffect(viewModel, navigator) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                TravelSpotDetailNavigationEvent.NavigateToLogin -> {
                    navigator.resetTo(LoginNavKey)
                }
            }
        }
    }

    /*
     * 상세 정보가 표시된 상태에서 발생한 찜 변경 오류만 Snackbar로 안내합니다.
     * 최초 조회 실패는 Screen의 오류 화면에 표시하여 재시도할 수 있게 유지합니다.
     */
    LaunchedEffect(uiState.errorMessage, uiState.hasDetail) {
        val errorMessage = uiState.errorMessage ?: return@LaunchedEffect

        if (!uiState.hasDetail) {
            return@LaunchedEffect
        }

        snackbarHostState.showSnackbar(errorMessage)
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
