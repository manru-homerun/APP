package com.manruhomerun.yadanbeopseok.notification.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanIconButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanIconButtonSize
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanIconButtonStyle
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.AppNotification
import com.manruhomerun.yadanbeopseok.model.NotificationType
import com.manruhomerun.yadanbeopseok.notification.component.YadanNotificationItem
import com.manruhomerun.yadanbeopseok.notification.viewmodel.NotificationListItem
import com.manruhomerun.yadanbeopseok.notification.viewmodel.NotificationUiState
import kotlinx.datetime.LocalDateTime

/** E·01 알림 센터 화면입니다. */
@Composable
fun NotificationScreen(
    uiState: NotificationUiState,
    onBackClick: () -> Unit,
    onSettingClick: () -> Unit,
    onNotificationClick: (AppNotification) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YadanBackground),
    ) {
        YadanTopAppBar(
            title = "알림",
            onNavigationClick = onBackClick,
            modifier = Modifier.statusBarsPadding(),
            trailingContent = {
                YadanIconButton(
                    onClick = onSettingClick,
                    style = YadanIconButtonStyle.DEFAULT,
                    size = YadanIconButtonSize.DEFAULT,
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "알림 설정",
                    )
                }
            },
        )

        when {
            uiState.isLoading && uiState.notifications.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = YadanPrimary)
                }
            }

            uiState.notifications.isNotEmpty() -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        top = 4.dp,
                        end = 20.dp,
                        bottom = 24.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(
                        items = uiState.notifications,
                        key = { item -> item.notification.id },
                    ) { item ->
                        YadanNotificationItem(
                            notification = item.notification,
                            timeText = item.timeText,
                            onClick = {
                                onNotificationClick(item.notification)
                            },
                        )
                    }
                }
            }

            uiState.errorMessage != null -> {
                NotificationErrorContent(
                    message = uiState.errorMessage,
                    onRetryClick = onRetryClick,
                    modifier = Modifier.weight(1f),
                )
            }

            uiState.notifications.isEmpty() -> {
                NotificationEmptyContent(
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun NotificationEmptyContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsNone,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = YadanTextMuted,
        )

        Text(
            text = "아직 받은 알림이 없습니다",
            modifier = Modifier.padding(top = 12.dp),
            style = YadanTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = YadanTextPrimary,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "친구 소식과 응원 팀 경기 일정을 알려드릴게요.",
            modifier = Modifier.padding(top = 6.dp),
            style = YadanTypography.bodySmall,
            color = YadanTextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun NotificationErrorContent(message: String, onRetryClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "알림을 확인할 수 없습니다",
            style = YadanTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = YadanTextPrimary,
            textAlign = TextAlign.Center,
        )

        Text(
            text = message,
            modifier = Modifier.padding(top = 8.dp),
            style = YadanTypography.bodySmall,
            color = YadanTextSecondary,
            textAlign = TextAlign.Center,
        )

        YadanButton(
            text = "다시 시도",
            onClick = onRetryClick,
            modifier = Modifier.padding(top = 20.dp),
        )
    }
}

@Preview(
    name = "E01 알림 목록",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun NotificationScreenPreview() {
    val notifications = listOf(
        NotificationListItem(
            notification = AppNotification(
                id = 1001,
                type = NotificationType.FRIEND_REQUEST,
                title = "친구 신청이 도착했어요",
                body = "야구친구님이 친구 신청을 보냈어요.",
                referenceId = "35",
                createdAt = LocalDateTime(2026, 9, 21, 13, 55),
            ),
            timeText = "5분 전",
        ),
        NotificationListItem(
            notification = AppNotification(
                id = 1002,
                type = NotificationType.FRIEND_REQUEST_ACCEPTED,
                title = "친구 신청을 수락했어요",
                body = "야구친구님과 친구가 되었어요.",
                referenceId = "35",
                createdAt = LocalDateTime(2026, 9, 21, 11, 0),
            ),
            timeText = "3시간 전",
        ),
        NotificationListItem(
            notification = AppNotification(
                id = 1003,
                type = NotificationType.WEEKLY_TEAM_SCHEDULE,
                title = "이번 주 경기 일정을 확인하세요",
                body = "응원 팀의 이번 주 경기 일정이 도착했어요.",
                referenceId = "2",
                createdAt = LocalDateTime(2026, 9, 21, 9, 0),
            ),
            timeText = "5시간 전",
        ),
    )

    YadanbeopseokTheme {
        NotificationScreen(
            uiState = NotificationUiState(
                notifications = notifications,
                isLoading = false,
            ),
            onBackClick = {},
            onSettingClick = {},
            onNotificationClick = {},
            onRetryClick = {},
        )
    }
}

@Preview(
    name = "E01 빈 알림",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun NotificationScreenEmptyPreview() {
    YadanbeopseokTheme {
        NotificationScreen(
            uiState = NotificationUiState(isLoading = false),
            onBackClick = {},
            onSettingClick = {},
            onNotificationClick = {},
            onRetryClick = {},
        )
    }
}

@Preview(
    name = "E01 오류",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun NotificationScreenErrorPreview() {
    YadanbeopseokTheme {
        NotificationScreen(
            uiState = NotificationUiState(
                isLoading = false,
                errorMessage = "인터넷 연결을 확인한 후 다시 시도해주세요.",
            ),
            onBackClick = {},
            onSettingClick = {},
            onNotificationClick = {},
            onRetryClick = {},
        )
    }
}
