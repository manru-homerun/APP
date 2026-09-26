package com.manruhomerun.yadanbeopseok.notification.navigation

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
import com.manruhomerun.yadanbeopseok.model.AppNotification
import com.manruhomerun.yadanbeopseok.notification.screen.NotificationScreen
import com.manruhomerun.yadanbeopseok.notification.viewmodel.NotificationViewModel

/** E·01 알림 화면과 ViewModel, 내비게이션 콜백을 연결합니다. */
@Composable
fun NotificationRoute(
    onBackClick: () -> Unit,
    onSettingClick: () -> Unit,
    onNotificationClick: (AppNotification) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    LaunchedEffect(uiState.errorMessage, uiState.notifications) {
        val errorMessage = uiState.errorMessage ?: return@LaunchedEffect
        if (uiState.notifications.isEmpty()) return@LaunchedEffect

        snackbarHostState.showSnackbar(errorMessage)
        viewModel.clearErrorMessage()
    }

    Box(modifier = modifier.fillMaxSize()) {
        NotificationScreen(
            uiState = uiState,
            onBackClick = onBackClick,
            onSettingClick = onSettingClick,
            onNotificationClick = onNotificationClick,
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
