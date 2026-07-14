package com.manruhomerun.yadanbeopseok.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * 야단법석 상단 앱 바의 시각적 유형입니다.
 */
enum class YadanTopAppBarStyle {
    /**
     * 일반적인 밝은 화면에서 사용하는 앱 바입니다.
     */
    DEFAULT,

    /**
     * 이미지나 어두운 화면 위에서 사용하는 흰색 앱 바입니다.
     */
    ON_DARK,
}

/**
 * 야단법석 화면 상단에서 사용하는 공통 앱 바입니다.
 *
 * 좌우 영역을 동일한 42dp로 유지하여 오른쪽 콘텐츠 유무와 관계없이
 * 제목이 화면 중앙에 배치되도록 합니다.
 *
 * 시스템 상태 표시줄 영역은 포함하지 않습니다.
 *
 * @param title 앱 바 중앙에 표시할 제목입니다.
 * @param onNavigationClick 왼쪽 탐색 버튼을 눌렀을 때 실행할 작업입니다.
 * @param modifier 앱 바의 배치에 사용할 Modifier입니다.
 * @param style 밝은 화면 또는 어두운 화면에 사용할 시각적 유형입니다.
 * @param navigationContentDescription 기본 뒤로가기 아이콘의 접근성 설명입니다.
 * @param navigationIcon 기본 뒤로가기 아이콘 대신 표시할 아이콘입니다.
 * 닫기 버튼 등이 필요한 화면에서 전달합니다.
 * @param trailingContent 앱 바 오른쪽에 표시할 작업 버튼이나 단계 표시입니다.
 */
@Composable
fun YadanTopAppBar(
    title: String,
    onNavigationClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: YadanTopAppBarStyle = YadanTopAppBarStyle.DEFAULT,
    navigationContentDescription: String = "뒤로가기",
    navigationIcon: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val contentColor =
        when (style) {
            YadanTopAppBarStyle.DEFAULT -> YadanTextPrimary
            YadanTopAppBarStyle.ON_DARK -> YadanOnPrimary
        }

    val iconButtonStyle =
        when (style) {
            YadanTopAppBarStyle.DEFAULT ->
                YadanIconButtonStyle.DEFAULT

            YadanTopAppBarStyle.ON_DARK ->
                YadanIconButtonStyle.ON_DARK
        }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    start = APP_BAR_HORIZONTAL_PADDING,
                    top = APP_BAR_TOP_PADDING,
                    end = APP_BAR_HORIZONTAL_PADDING,
                    bottom = APP_BAR_BOTTOM_PADDING,
                ),
        horizontalArrangement =
            Arrangement.spacedBy(APP_BAR_CONTENT_SPACING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        /*
         * 상세 화면용 앱 바는 항상 뒤로가기 또는 닫기 버튼을 표시합니다.
         * 왼쪽 버튼이 없는 메인 탭 화면은 별도의 메인 헤더를 사용합니다.
         */
        Box(
            modifier = Modifier.size(APP_BAR_SIDE_SIZE),
            contentAlignment = Alignment.Center,
        ) {
            YadanIconButton(
                onClick = onNavigationClick,
                style = iconButtonStyle,
                size = YadanIconButtonSize.DEFAULT,
            ) {
                if (navigationIcon != null) {
                    navigationIcon()
                } else {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription =
                            navigationContentDescription,
                    )
                }
            }
        }

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style =
                YadanTypography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
            color = contentColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        /*
         * 오른쪽 콘텐츠가 없는 경우에도 왼쪽과 동일한 영역을 유지합니다.
         * 전달되는 일반 Icon은 앱 바의 콘텐츠 색상을 상속받습니다.
         */
        Box(
            modifier = Modifier.size(APP_BAR_SIDE_SIZE),
            contentAlignment = Alignment.Center,
        ) {
            if (trailingContent != null) {
                CompositionLocalProvider(
                    LocalContentColor provides contentColor,
                ) {
                    trailingContent()
                }
            }
        }
    }
}

private val APP_BAR_SIDE_SIZE = 42.dp

private val APP_BAR_HORIZONTAL_PADDING = 12.dp
private val APP_BAR_TOP_PADDING = 4.dp
private val APP_BAR_BOTTOM_PADDING = 8.dp
private val APP_BAR_CONTENT_SPACING = 8.dp

@Preview(
    name = "Yadan top app bars",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
)
@Composable
private fun YadanTopAppBarPreview() {
    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground),
        ) {
            /*
             * 오른쪽 콘텐츠가 없는 일반 상세 화면입니다.
             */
            YadanTopAppBar(
                title = "여행 상세",
                onNavigationClick = {},
            )

            /*
             * 알림 설정 버튼이 있는 알림 화면입니다.
             */
            YadanTopAppBar(
                title = "알림",
                onNavigationClick = {},
                trailingContent = {
                    YadanIconButton(
                        onClick = {},
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

            /*
             * 여행 만들기 첫 단계처럼 닫기 버튼을 사용하는 화면입니다.
             */
            YadanTopAppBar(
                title = "여행 만들기",
                onNavigationClick = {},
                navigationContentDescription = "닫기",
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기",
                    )
                },
            )

            /*
             * 친구 추가 작업이 있는 친구 화면입니다.
             */
            YadanTopAppBar(
                title = "친구",
                onNavigationClick = {},
                trailingContent = {
                    YadanIconButton(
                        onClick = {},
                        style = YadanIconButtonStyle.DEFAULT,
                        size = YadanIconButtonSize.DEFAULT,
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "친구 찾기",
                        )
                    }
                },
            )

            /*
             * 이미지나 어두운 화면 위에서 사용하는 형태입니다.
             */
            Box(
                modifier =
                    Modifier.background(YadanTextPrimary),
            ) {
                YadanTopAppBar(
                    title = "스티커 사진",
                    onNavigationClick = {},
                    style = YadanTopAppBarStyle.ON_DARK,
                )
            }
        }
    }
}
