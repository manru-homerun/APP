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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.mypage.screen.TravelPreferenceEditScreen
import com.manruhomerun.yadanbeopseok.mypage.viewmodel.TravelPreferenceEditEvent
import com.manruhomerun.yadanbeopseok.mypage.viewmodel.TravelPreferenceEditViewModel

/** H·03 취향 수정 화면과 [TravelPreferenceEditViewModel]을 연결합니다. */
@Composable
fun TravelPreferenceEditRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TravelPreferenceEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                TravelPreferenceEditEvent.Saved -> onBackClick()
            }
        }
    }

    LaunchedEffect(
        uiState.errorMessage,
        uiState.originalPreference,
    ) {
        val errorMessage = uiState.errorMessage ?: return@LaunchedEffect
        if (uiState.originalPreference == null) return@LaunchedEffect

        snackbarHostState.showSnackbar(errorMessage)
        viewModel.clearErrorMessage()
    }

    Box(modifier = modifier.fillMaxSize()) {
        TravelPreferenceEditScreen(
            uiState = uiState,
            onBackClick = onBackClick,
            onRetryClick = viewModel::retry,
            onResidenceRegionSelected = viewModel::selectResidenceRegion,
            onTravelStyleScoreChange = viewModel::updateTravelStyleScore,
            onPreferredTravelRegionToggle = viewModel::togglePreferredTravelRegion,
            onSaveClick = viewModel::saveTravelPreference,
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
