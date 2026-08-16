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
import com.manruhomerun.yadanbeopseok.model.TravelPlace
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.LoginNavKey
import com.manruhomerun.yadanbeopseok.travel.screen.TravelDetailScreen
import com.manruhomerun.yadanbeopseok.travel.viewmodel.TravelDetailNavigationEvent
import com.manruhomerun.yadanbeopseok.travel.viewmodel.TravelDetailViewModel

/**
 * 여행 상세 화면과 [TravelDetailViewModel]을 연결합니다.
 *
 * 여행 ID를 사용해 상세 정보를 조회하고, 화면 상태와 사용자 입력,
 * 세션 만료 및 화면 이동을 처리합니다.
 */
@Composable
fun TravelDetailRoute(
    travelId: String,
    navigator: Navigator,
    onVerifyClick: ((TravelPlace) -> Unit)? = null,
    onRenameClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: TravelDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    /**
     * NavKey로 전달받은 여행 ID가 변경되면 해당 여행을 조회합니다.
     */
    LaunchedEffect(travelId, viewModel) {
        viewModel.loadTravel(travelId)
    }

    /**
     * 세션이 만료되면 전체 백스택을 제거하고 로그인 화면으로 이동합니다.
     */
    LaunchedEffect(viewModel, navigator) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                TravelDetailNavigationEvent.NavigateToLogin -> {
                    navigator.resetTo(LoginNavKey)
                }
            }
        }
    }

    /*
     * 여행 정보가 이미 표시된 상태에서 발생한 작업 오류만
     * Snackbar로 안내합니다.
     *
     * 최초 조회 실패는 TravelDetailScreen의 오류 화면에 계속 표시하여
     * 사용자가 재시도할 수 있도록 유지합니다.
     */
    LaunchedEffect(uiState.errorMessage, uiState.hasTravel) {
        val errorMessage = uiState.errorMessage ?: return@LaunchedEffect

        if (!uiState.hasTravel) {
            return@LaunchedEffect
        }

        snackbarHostState.showSnackbar(
            message = errorMessage,
        )
        viewModel.clearErrorMessage()
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        TravelDetailScreen(
            uiState = uiState,
            onBackClick = navigator::navigateBack,
            onDaySelected = viewModel::selectDay,
            onVerifyClick = onVerifyClick,
            onRetryClick = viewModel::retry,
            onRenameClick = onRenameClick,
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
