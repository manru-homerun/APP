package com.manruhomerun.yadanbeopseok.notification.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCard
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanDivider
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.NotificationSetting
import com.manruhomerun.yadanbeopseok.notification.viewmodel.NotificationSettingUiState
import com.manruhomerun.yadanbeopseok.ui.component.YadanSettingItem

/** E·02 알림 설정 화면입니다. */
@Composable
fun NotificationSettingScreen(
    uiState: NotificationSettingUiState,
    interactionEnabled: Boolean,
    onBackClick: () -> Unit,
    onFriendNotificationChange: (Boolean) -> Unit,
    onWeeklyScheduleNotificationChange: (Boolean) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YadanBackground),
    ) {
        YadanTopAppBar(
            title = "알림 설정",
            onNavigationClick = onBackClick,
            modifier = Modifier.statusBarsPadding(),
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = YadanPrimary)
                }
            }

            uiState.setting == null -> {
                NotificationSettingErrorContent(
                    message = uiState.errorMessage
                        ?: "알림 설정을 불러오지 못했습니다.",
                    onRetryClick = onRetryClick,
                    modifier = Modifier.weight(1f),
                )
            }

            else -> {
                NotificationSettingContent(
                    setting = uiState.setting,
                    enabled = interactionEnabled && !uiState.isUpdating,
                    onFriendNotificationChange = onFriendNotificationChange,
                    onWeeklyScheduleNotificationChange = onWeeklyScheduleNotificationChange,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun NotificationSettingContent(
    setting: NotificationSetting,
    enabled: Boolean,
    onFriendNotificationChange: (Boolean) -> Unit,
    onWeeklyScheduleNotificationChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 20.dp,
                top = 4.dp,
                end = 20.dp,
                bottom = 24.dp,
            ),
    ) {
        YadanCard(modifier = Modifier.fillMaxWidth()) {
            YadanSettingItem(
                title = "친구 알림",
                supportingText = "친구 신청과 수락 소식을 알려드려요",
                checked = setting.friendNotificationEnabled,
                onCheckedChange = onFriendNotificationChange,
                enabled = enabled,
            )

            HorizontalDivider(
                thickness = 1.dp,
                color = YadanDivider,
            )

            YadanSettingItem(
                title = "경기 일정 알림",
                supportingText = "매주 월요일 오전 9시에 응원 팀의 경기 일정을 알려드려요",
                checked = setting.weeklyTeamScheduleNotificationEnabled,
                onCheckedChange = onWeeklyScheduleNotificationChange,
                enabled = enabled,
            )
        }
    }
}

@Composable
private fun NotificationSettingErrorContent(message: String, onRetryClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "알림 설정을 확인할 수 없습니다",
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
    name = "E02 알림 설정",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun NotificationSettingScreenPreview() {
    YadanbeopseokTheme {
        NotificationSettingScreen(
            uiState = NotificationSettingUiState(
                setting = NotificationSetting(
                    friendNotificationEnabled = true,
                    weeklyTeamScheduleNotificationEnabled = false,
                ),
                isLoading = false,
            ),
            interactionEnabled = true,
            onBackClick = {},
            onFriendNotificationChange = {},
            onWeeklyScheduleNotificationChange = {},
            onRetryClick = {},
        )
    }
}

@Preview(
    name = "E02 알림 설정 오류",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun NotificationSettingScreenErrorPreview() {
    YadanbeopseokTheme {
        NotificationSettingScreen(
            uiState = NotificationSettingUiState(
                isLoading = false,
                errorMessage = "인터넷 연결을 확인한 후 다시 시도해주세요.",
            ),
            interactionEnabled = true,
            onBackClick = {},
            onFriendNotificationChange = {},
            onWeeklyScheduleNotificationChange = {},
            onRetryClick = {},
        )
    }
}
