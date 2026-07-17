package com.manruhomerun.yadanbeopseok.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanError
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * 메인 탭 헤더의 왼쪽 콘텐츠 유형입니다.
 */
enum class YadanMainHeaderStyle {
    /**
     * 홈 화면의 서비스 이름이나 앱 아이콘에 사용합니다.
     */
    BRAND,

    /**
     * 경기 일정, 여행 기록, 마이 화면의 제목에 사용합니다.
     */
    TITLE,
}

/**
 * 왼쪽 탐색 버튼이 없는 메인 탭 화면의 공통 헤더입니다.
 *
 * 왼쪽에는 제목 텍스트 또는 아이콘 중 하나를 표시할 수 있습니다.
 * [headerIcon]이 전달되면 [title] 대신 아이콘만 표시합니다.
 *
 * 오른쪽에는 알림 버튼 등의 콘텐츠를 표시할 수 있습니다.
 * 시스템 상태 표시줄 영역은 포함하지 않습니다.
 *
 * @param title 왼쪽에 표시할 서비스 이름 또는 화면 제목입니다.
 * 아이콘만 표시할 때는 생략합니다.
 * @param modifier 헤더 배치에 사용할 Modifier입니다.
 * @param style 브랜드 또는 일반 화면 제목 유형입니다.
 * @param headerIcon 제목 대신 왼쪽에 표시할 앱 아이콘이나 로고입니다.
 * 아이콘 크기는 호출하는 화면에서 지정합니다.
 * @param trailingContent 오른쪽에 표시할 알림 버튼 등의 콘텐츠입니다.
 */
@Composable
fun YadanMainHeader(
    title: String? = null,
    modifier: Modifier = Modifier,
    style: YadanMainHeaderStyle = YadanMainHeaderStyle.TITLE,
    headerIcon: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val titleStyle =
        when (style) {
            YadanMainHeaderStyle.BRAND ->
                YadanTypography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                )

            YadanMainHeaderStyle.TITLE ->
                YadanTypography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                )
        }

    val contentColor =
        when (style) {
            YadanMainHeaderStyle.BRAND -> YadanPrimary
            YadanMainHeaderStyle.TITLE -> YadanTextPrimary
        }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    start = HEADER_HORIZONTAL_PADDING,
                    top = HEADER_TOP_PADDING,
                    end = HEADER_HORIZONTAL_PADDING,
                    bottom = HEADER_BOTTOM_PADDING,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        /*
         * 왼쪽 영역이 남은 너비를 차지하도록 하여
         * 오른쪽 버튼과 겹치지 않게 합니다.
         */
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides contentColor,
            ) {
                when {
                    /*
                     * 아이콘이 있으면 제목 텍스트를 표시하지 않고
                     * 왼쪽에 아이콘만 표시합니다.
                     */
                    headerIcon != null -> {
                        headerIcon()
                    }

                    /*
                     * 아이콘이 없을 때만 제목 텍스트를 표시합니다.
                     */
                    title != null -> {
                        Text(
                            text = title,
                            style = titleStyle,
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        /*
         * 오른쪽 콘텐츠가 있을 때만 표시합니다.
         * YadanIconButton은 자신의 42dp 크기를 직접 관리합니다.
         */
        if (trailingContent != null) {
            CompositionLocalProvider(
                LocalContentColor provides YadanTextPrimary,
            ) {
                trailingContent()
            }
        }
    }
}

/*
 * HTML의 apphead에서 사용하는 내부 여백입니다.
 */
private val HEADER_HORIZONTAL_PADDING = 16.dp
private val HEADER_TOP_PADDING = 6.dp
private val HEADER_BOTTOM_PADDING = 8.dp

@Preview(
    name = "Yadan main headers",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
)
@Composable
private fun YadanMainHeaderPreview() {
    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground),
        ) {
            /*
             * 현재 HTML과 같은 텍스트형 브랜드 헤더입니다.
             */
            YadanMainHeader(
                title = "야단법석",
                style = YadanMainHeaderStyle.BRAND,
                trailingContent = {
                    NotificationButtonPreview(
                        hasUnreadNotification = true,
                    )
                },
            )

            /*
             * 왼쪽에 텍스트 없이 앱 아이콘만 표시하는 헤더입니다.
             *
             * 현재는 Material 아이콘으로 배치만 확인하고,
             * 실제 앱 아이콘이 정해지면 Image 또는 SVG로 교체합니다.
             */
            YadanMainHeader(
                style = YadanMainHeaderStyle.BRAND,
                headerIcon = {
                    Icon(
                        imageVector = Icons.Default.SportsBaseball,
                        contentDescription = "야단법석",
                        modifier = Modifier.size(BRAND_ICON_PREVIEW_SIZE),
                    )
                },
                trailingContent = {
                    NotificationButtonPreview(
                        hasUnreadNotification = true,
                    )
                },
            )

            /*
             * 경기 일정 탭처럼 일반 제목만 있는 헤더입니다.
             */
            YadanMainHeader(
                title = "경기 일정",
            )

            /*
             * 여행 기록 탭의 제목 헤더입니다.
             */
            YadanMainHeader(
                title = "여행 기록",
            )

            /*
             * 마이 탭의 제목 헤더입니다.
             */
            YadanMainHeader(
                title = "마이",
            )
        }
    }
}

/**
 * 홈 화면의 알림 버튼 형태를 확인하기 위한 Preview 구성입니다.
 *
 * Box 크기를 별도로 지정하지 않아도 내부 YadanIconButton의
 * 기본 컨테이너 크기인 42dp에 맞춰 자동으로 결정됩니다.
 */
@Composable
private fun NotificationButtonPreview(
    hasUnreadNotification: Boolean,
) {
    Box {
        YadanIconButton(
            onClick = {},
            style = YadanIconButtonStyle.DEFAULT,
            size = YadanIconButtonSize.DEFAULT,
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "알림",
            )
        }

        if (hasUnreadNotification) {
            /*
             * HTML의 9px 빨간 점과 바깥쪽 2px 배경 테두리입니다.
             */
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(
                            x = NOTIFICATION_RING_OFFSET_X,
                            y = NOTIFICATION_RING_OFFSET_Y,
                        )
                        .size(NOTIFICATION_RING_SIZE)
                        .background(
                            color = YadanBackground,
                            shape = CircleShape,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(NOTIFICATION_DOT_SIZE)
                            .background(
                                color = YadanError,
                                shape = CircleShape,
                            ),
                )
            }
        }
    }
}

/*
 * Preview에서 사용하는 임시 브랜드 아이콘 크기입니다.
 * 실제 앱 아이콘을 적용할 때 변경할 수 있습니다.
 */
private val BRAND_ICON_PREVIEW_SIZE = 28.dp

/*
 * 알림 점은 메인 헤더 Preview에서만 사용하는 고유 크기입니다.
 */
private val NOTIFICATION_DOT_SIZE = 9.dp
private val NOTIFICATION_RING_SIZE = 13.dp
private val NOTIFICATION_RING_OFFSET_X = (-8).dp
private val NOTIFICATION_RING_OFFSET_Y = 7.dp
