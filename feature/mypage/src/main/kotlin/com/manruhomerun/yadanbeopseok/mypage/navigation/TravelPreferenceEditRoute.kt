package com.manruhomerun.yadanbeopseok.mypage.navigation

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.mypage.screen.TravelPreferenceEditScreen
import com.manruhomerun.yadanbeopseok.mypage.viewmodel.TravelPreferenceEditEvent
import com.manruhomerun.yadanbeopseok.mypage.viewmodel.TravelPreferenceEditViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** H·03 취향 편집과 저장 중 뒤로가기 차단을 연결합니다. */
@Composable
fun TravelPreferenceEditRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TravelPreferenceEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var savingBackMessageJob by remember(viewModel) { mutableStateOf<Job?>(null) }

    val onUserBackClick: () -> Unit = {
        if (viewModel.uiState.value.isSaving) {
            if (savingBackMessageJob?.isActive != true) {
                savingBackMessageJob = coroutineScope.launch {
                    snackbarHostState.showSnackbar("저장 중입니다. 잠시만 기다려주세요.")
                }
            }
        } else {
            onBackClick()
        }
    }

    BackHandler(enabled = uiState.isSaving, onBack = onUserBackClick)

    // 저장 종료 시 대기 안내만 취소하고, 저장 실패 안내는 유지합니다.
    LaunchedEffect(uiState.isSaving, viewModel) {
        if (!viewModel.uiState.value.isSaving) {
            savingBackMessageJob?.cancel()
            savingBackMessageJob = null
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                TravelPreferenceEditEvent.Saved -> {
                    savingBackMessageJob?.cancel()
                    savingBackMessageJob = null
                    onBackClick()
                }
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
            onBackClick = onUserBackClick,
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
