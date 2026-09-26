package com.manruhomerun.yadanbeopseok.notification.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.notification.screen.NotificationSettingScreen
import com.manruhomerun.yadanbeopseok.notification.viewmodel.NotificationSettingViewModel
import com.manruhomerun.yadanbeopseok.notifications.YadanSystemNotificationManager
import kotlinx.coroutines.launch

/** E·02 알림 설정 화면과 ViewModel을 연결합니다. */
@Composable
fun NotificationSettingRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationSettingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var systemStateRevision by remember { mutableIntStateOf(0) }
    var pendingTarget by rememberSaveable { mutableStateOf<NotificationSettingTarget?>(null) }

    val updateServerSetting: (NotificationSettingTarget, Boolean) -> Unit = { target, enabled ->
        when (target) {
            NotificationSettingTarget.FRIEND ->
                viewModel.updateFriendNotification(enabled)

            NotificationSettingTarget.WEEKLY_SCHEDULE ->
                viewModel.updateWeeklyTeamScheduleNotification(enabled)
        }
    }

    val showSystemNotificationUnavailableMessage: () -> Unit = {
        coroutineScope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "시스템 알림이 꺼져 있어 알림을 받을 수 없습니다.",
                actionLabel = "설정",
            )

            if (result == SnackbarResult.ActionPerformed) {
                YadanSystemNotificationManager.openNotificationSettings(context)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        val target = pendingTarget
        pendingTarget = null

        if (
            isGranted &&
            target != null &&
            YadanSystemNotificationManager.canPostNotifications(context)
        ) {
            updateServerSetting(target, true)
        } else {
            showSystemNotificationUnavailableMessage()
        }
    }

    val updateNotificationSetting: (NotificationSettingTarget, Boolean) -> Unit = { target, enabled ->
        when {
            !enabled -> updateServerSetting(target, false)

            YadanSystemNotificationManager.canPostNotifications(context) ->
                updateServerSetting(target, true)

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !YadanSystemNotificationManager.hasPostNotificationsPermission(context) -> {
                pendingTarget = target
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }

            else -> showSystemNotificationUnavailableMessage()
        }
    }

    LaunchedEffect(uiState.userMessage) {
        val message = uiState.userMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearUserMessage()
    }

    LaunchedEffect(uiState.setting, systemStateRevision) {
        if (pendingTarget != null) return@LaunchedEffect

        val setting = uiState.setting ?: return@LaunchedEffect
        val hasEnabledSetting = setting.friendNotificationEnabled ||
            setting.weeklyTeamScheduleNotificationEnabled

        if (
            hasEnabledSetting &&
            !YadanSystemNotificationManager.canPostNotifications(context)
        ) {
            showSystemNotificationUnavailableMessage()
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        systemStateRevision += 1
    }

    Box(modifier = modifier.fillMaxSize()) {
        NotificationSettingScreen(
            uiState = uiState,
            interactionEnabled = pendingTarget == null,
            onBackClick = onBackClick,
            onFriendNotificationChange = { enabled ->
                updateNotificationSetting(
                    NotificationSettingTarget.FRIEND,
                    enabled,
                )
            },
            onWeeklyScheduleNotificationChange = { enabled ->
                updateNotificationSetting(
                    NotificationSettingTarget.WEEKLY_SCHEDULE,
                    enabled,
                )
            },
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

private enum class NotificationSettingTarget {
    FRIEND,
    WEEKLY_SCHEDULE,
}
