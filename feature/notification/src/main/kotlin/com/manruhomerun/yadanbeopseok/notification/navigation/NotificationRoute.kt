package com.manruhomerun.yadanbeopseok.notification.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.manruhomerun.yadanbeopseok.model.AppNotification
import com.manruhomerun.yadanbeopseok.notification.screen.NotificationScreen
import com.manruhomerun.yadanbeopseok.notification.viewmodel.NotificationViewModel
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

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
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    // 화면을 보고 있을 때만 시간을 갱신하고, 복귀 시 즉시 다시 계산합니다.
    LaunchedEffect(lifecycle, viewModel) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (isActive) {
                viewModel.refreshTimeTexts()
                delay(1.minutes)
            }
        }
    }

    NotificationScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onSettingClick = onSettingClick,
        onNotificationClick = onNotificationClick,
        onRetryClick = viewModel::retry,
        modifier = modifier,
    )
}
