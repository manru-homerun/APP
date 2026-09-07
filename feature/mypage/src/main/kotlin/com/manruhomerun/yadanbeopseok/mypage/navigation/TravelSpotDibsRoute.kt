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
import com.manruhomerun.yadanbeopseok.mypage.screen.TravelSpotDibsScreen
import com.manruhomerun.yadanbeopseok.mypage.viewmodel.TravelSpotDibsViewModel

/**
 * H·04 찜한 관광지 화면과 [TravelSpotDibsViewModel]을 연결합니다.
 *
 * 관광지 상세 화면에서 돌아오면 찜 목록을 다시 조회하여
 * 상세 화면에서 변경된 찜 상태를 목록에 반영합니다.
 */
@Composable
fun TravelSpotDibsRoute(
    onBackClick: () -> Unit,
    onTravelSpotClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TravelSpotDibsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    LaunchedEffect(
        uiState.errorMessage,
        uiState.dibsSpots.isNotEmpty(),
    ) {
        val errorMessage = uiState.errorMessage ?: return@LaunchedEffect
        if (uiState.dibsSpots.isEmpty()) return@LaunchedEffect

        snackbarHostState.showSnackbar(errorMessage)
        viewModel.clearErrorMessage()
    }

    Box(modifier = modifier.fillMaxSize()) {
        TravelSpotDibsScreen(
            uiState = uiState,
            onBackClick = onBackClick,
            onCategorySelected = viewModel::selectCategory,
            onTravelSpotClick = onTravelSpotClick,
            onDibsClick = viewModel::deleteDibs,
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
