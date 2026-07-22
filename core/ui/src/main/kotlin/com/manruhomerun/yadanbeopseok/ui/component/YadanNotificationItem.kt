package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCard
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanAccessibilityBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanAccessibilityContent
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanCareBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanCareContent
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanDivider
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPillShape
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTint
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.AppNotification
import com.manruhomerun.yadanbeopseok.model.NotificationType
import kotlinx.datetime.LocalDateTime

/**
 * 알림 센터에 표시하는 알림 카드입니다.
 *
 * HTML의 `.noti`, `.noti-ic`, `.noti-body`, `.noti-dest` 구조에 대응합니다.
 * 알림 종류에 따라 아이콘, 아이콘 배경과 이동 목적지를 결정합니다.
 *
 * @param notification 표시할 앱 내부 알림입니다.
 * @param timeText "5분 전", "3시간 전"처럼 화면에 표시할 시간입니다.
 * 상대 시간 계산은 현재 시각과 사용자 시간대가 필요하므로 화면 또는
 * ViewModel에서 처리한 문자열을 전달합니다.
 * @param onClick 알림을 눌렀을 때 연결된 화면으로 이동하는 작업입니다.
 * @param modifier 카드의 크기와 배치를 지정합니다.
 * @param enabled 알림 선택 가능 여부입니다.
 */
@Composable
fun YadanNotificationItem(
    notification: AppNotification,
    timeText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val visuals = notification.type.visuals()

    YadanCard(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .alpha(
                    if (enabled) {
                        1f
                    } else {
                        0.42f
                    },
                ),
        enabled = enabled,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            /*
             * 제목에서 알림의 의미를 다시 제공하므로 아이콘은
             * TalkBack에서 중복으로 읽히지 않도록 장식 요소로 처리합니다.
             */
            Surface(
                modifier = Modifier.size(42.dp),
                shape = YadanShapes.medium,
                color = visuals.iconContainerColor,
                contentColor = visuals.iconContentColor,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = visuals.icon,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = notification.title,
                        modifier =
                            Modifier
                                .weight(1f)
                                .alignByBaseline(),
                        style =
                            YadanTypography.bodyMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                            ),
                        color = YadanTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    timeText
                        .takeIf { text -> text.isNotBlank() }
                        ?.let { text ->
                            Text(
                                text = text,
                                modifier = Modifier.alignByBaseline(),
                                style =
                                    YadanTypography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                color = YadanTextMuted,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                }

                Spacer(modifier = Modifier.size(3.dp))

                Text(
                    text = notification.content,
                    style =
                        YadanTypography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    color = YadanTextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.size(9.dp))

                /*
                 * 카드 전체가 클릭 영역이므로 목적지 표시는 별도의 버튼이 아닌
                 * 이동 결과를 설명하는 시각적 요소로 구성합니다.
                 */
                Surface(
                    shape = YadanPillShape,
                    color = YadanPrimaryTint,
                    contentColor = YadanPrimaryInk,
                    border =
                        BorderStroke(
                            width = 1.dp,
                            color = YadanPrimary.copy(alpha = 0.22f),
                        ),
                ) {
                    Row(
                        modifier =
                            Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 5.dp,
                            ),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = visuals.destinationIcon,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                        )

                        Text(
                            text = visuals.destinationText,
                            style =
                                YadanTypography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                ),
                            maxLines = 1,
                            softWrap = false,
                        )

                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * 알림 종류별 아이콘과 이동 목적지 표현을 반환합니다.
 */
private fun NotificationType.visuals(): YadanNotificationVisuals =
    when (this) {
        NotificationType.TICKET_OPEN ->
            YadanNotificationVisuals(
                icon = Icons.Default.ConfirmationNumber,
                iconContainerColor = YadanCareBackground,
                iconContentColor = YadanCareContent,
                destinationText = "경기 일정",
                destinationIcon = Icons.Default.Stadium,
            )

        NotificationType.VISIT_CERTIFICATION_REMINDER ->
            YadanNotificationVisuals(
                icon = Icons.Default.MyLocation,
                iconContainerColor = YadanPrimaryTint,
                iconContentColor = YadanPrimaryInk,
                destinationText = "방문 인증",
                destinationIcon = Icons.Default.MyLocation,
            )

        NotificationType.NEARBY_GAME ->
            YadanNotificationVisuals(
                icon = Icons.Default.SportsBaseball,
                iconContainerColor = YadanDivider,
                iconContentColor = YadanTextPrimary,
                destinationText = "여행 만들기",
                destinationIcon = Icons.Default.AddCircle,
            )

        NotificationType.FRIEND_REQUEST ->
            YadanNotificationVisuals(
                icon = Icons.Default.PersonAdd,
                iconContainerColor = YadanPrimaryTint,
                iconContentColor = YadanPrimaryInk,
                destinationText = "친구 요청",
                destinationIcon = Icons.Default.PersonAdd,
            )

        NotificationType.FRIEND_ACCEPTED ->
            YadanNotificationVisuals(
                icon = Icons.Default.Group,
                iconContainerColor = YadanPrimaryTint,
                iconContentColor = YadanPrimaryInk,
                destinationText = "친구 목록",
                destinationIcon = Icons.Default.Group,
            )

        NotificationType.TRAVEL_RECOMMENDATION ->
            YadanNotificationVisuals(
                icon = Icons.Default.TravelExplore,
                iconContainerColor = YadanAccessibilityBackground,
                iconContentColor = YadanAccessibilityContent,
                destinationText = "추천 여행",
                destinationIcon = Icons.Default.TravelExplore,
            )

        NotificationType.UNKNOWN ->
            YadanNotificationVisuals(
                icon = Icons.Default.Notifications,
                iconContainerColor = YadanDivider,
                iconContentColor = YadanTextMuted,
                destinationText = "알림 확인",
                destinationIcon = Icons.Default.Notifications,
            )
    }

/**
 * 알림 유형별 시각 정보를 묶어 전달합니다.
 */
private data class YadanNotificationVisuals(
    val icon: ImageVector,
    val iconContainerColor: Color,
    val iconContentColor: Color,
    val destinationText: String,
    val destinationIcon: ImageVector,
)

@Preview(
    name = "Yadan notification items",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
)
@Composable
private fun YadanNotificationItemPreview() {
    val ticketNotification =
        AppNotification(
            id = "notification-ticket",
            userId = "preview-user",
            type = NotificationType.TICKET_OPEN,
            title = "오늘 사직 경기 예매 오픈",
            content = "롯데 vs KIA · 오후 2시 예매가 시작돼요",
            createdAt = LocalDateTime(2026, 5, 23, 13, 55),
        )

    val certificationNotification =
        AppNotification(
            id = "notification-certification",
            userId = "preview-user",
            type = NotificationType.VISIT_CERTIFICATION_REMINDER,
            title = "감천문화마을 근처예요",
            content = "지금 방문 인증하고 사직 스티커를 받아보세요",
            createdAt = LocalDateTime(2026, 5, 23, 11, 0),
        )

    val gameNotification =
        AppNotification(
            id = "notification-game",
            userId = "preview-user",
            type = NotificationType.NEARBY_GAME,
            title = "이번 주말 우리 팀 홈경기",
            content = "5.23(토) 사직 · 직관 여행 코스를 만들어볼까요?",
            createdAt = LocalDateTime(2026, 5, 23, 9, 0),
        )

    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            YadanNotificationItem(
                notification = ticketNotification,
                timeText = "5분 전",
                onClick = {},
            )

            YadanNotificationItem(
                notification = certificationNotification,
                timeText = "3시간 전",
                onClick = {},
            )

            YadanNotificationItem(
                notification = gameNotification,
                timeText = "5시간 전",
                onClick = {},
            )
        }
    }
}
