package com.manruhomerun.yadanbeopseok.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanAccessibilityBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanAccessibilityContent
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanCareBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanCareContent
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanDivider
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTintStrong
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * 장소 카테고리 배지의 시각적 유형입니다.
 */
enum class YadanCategoryBadgeStyle {
    /** 음식, 문화, 자연, 숙박 등 일반 장소 카테고리입니다. */
    DEFAULT,

    /** 야구 경기 및 야구장 방문을 나타내는 직관 카테고리입니다. */
    GAME,

    /** 지역 행사와 축제를 나타내는 카테고리입니다. */
    FESTIVAL,

    /** 장소 상세 화면에서 선택된 주요 카테고리입니다. */
    SELECTED,

    /** 휠체어 접근 등 장소의 접근성 정보를 나타냅니다. */
    ACCESSIBILITY,

    /** 아이 및 어르신 동반과 같은 배려 정보를 나타냅니다. */
    CARE,
}

/**
 * 장소의 종류와 특성을 표시하는 작은 배지입니다.
 *
 * HTML의 `.cat`, `.cat.game`, `.cat.fest`, `.cat.sel`,
 * `.cat.access`, `.cat.care`에 대응합니다.
 *
 * 장소 정보를 표시하는 용도이므로 자체 클릭 동작은 제공하지 않습니다.
 * 축제 이모지와 같은 장식은 [text]에 포함해서 전달합니다.
 *
 * @param text 배지에 표시할 장소 종류 또는 특성입니다.
 * @param modifier 배지의 배치에 사용할 Modifier입니다.
 * @param style 배지의 색상 유형입니다.
 */
@Composable
fun YadanCategoryBadge(
    text: String,
    modifier: Modifier = Modifier,
    style: YadanCategoryBadgeStyle = YadanCategoryBadgeStyle.DEFAULT,
) {
    val (containerColor, contentColor) =
        when (style) {
            YadanCategoryBadgeStyle.DEFAULT ->
                YadanDivider to YadanTextSecondary

            YadanCategoryBadgeStyle.GAME ->
                YadanTextPrimary to YadanOnPrimary

            YadanCategoryBadgeStyle.FESTIVAL ->
                YadanPrimaryTintStrong to YadanPrimaryInk

            YadanCategoryBadgeStyle.SELECTED ->
                YadanPrimary to YadanOnPrimary

            YadanCategoryBadgeStyle.ACCESSIBILITY ->
                YadanAccessibilityBackground to YadanAccessibilityContent

            YadanCategoryBadgeStyle.CARE ->
                YadanCareBackground to YadanCareContent
        }

    Surface(
        modifier = modifier,
        shape = YadanShapes.extraSmall,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Text(
            text = text,
            modifier =
                Modifier.padding(
                    horizontal = BADGE_HORIZONTAL_PADDING,
                    vertical = BADGE_VERTICAL_PADDING,
                ),
            style =
                YadanTypography.labelSmall.copy(
                    fontSize = BADGE_FONT_SIZE,
                    lineHeight = BADGE_LINE_HEIGHT,
                    fontWeight = FontWeight.ExtraBold,
                ),
            maxLines = 1,
            softWrap = false,
        )
    }
}

private val BADGE_HORIZONTAL_PADDING = 7.dp
private val BADGE_VERTICAL_PADDING = 2.dp

private val BADGE_FONT_SIZE = 10.sp
private val BADGE_LINE_HEIGHT = 12.sp

@Preview(
    name = "Yadan category badges",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
)
@Composable
private fun YadanCategoryBadgePreview() {
    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "장소 카테고리",
                style = YadanTypography.labelMedium,
                color = YadanTextSecondary,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                YadanCategoryBadge(text = "문화")

                YadanCategoryBadge(
                    text = "직관",
                    style = YadanCategoryBadgeStyle.GAME,
                )

                YadanCategoryBadge(
                    text = "🎉 축제",
                    style = YadanCategoryBadgeStyle.FESTIVAL,
                )

                YadanCategoryBadge(
                    text = "문화",
                    style = YadanCategoryBadgeStyle.SELECTED,
                )
            }

            Text(
                text = "접근성과 동행 정보",
                style = YadanTypography.labelMedium,
                color = YadanTextSecondary,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                YadanCategoryBadge(
                    text = "휠체어 접근 가능",
                    style = YadanCategoryBadgeStyle.ACCESSIBILITY,
                )

                YadanCategoryBadge(
                    text = "아이 동반 가능",
                    style = YadanCategoryBadgeStyle.CARE,
                )
            }

            YadanCategoryBadge(
                text = "어르신 동반 가능",
                style = YadanCategoryBadgeStyle.CARE,
            )
        }
    }
}
