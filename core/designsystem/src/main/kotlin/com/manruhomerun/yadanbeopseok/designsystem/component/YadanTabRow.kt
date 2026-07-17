package com.manruhomerun.yadanbeopseok.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPillShape
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTintStrong
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * 밑줄형 탭 하나에 표시할 정보입니다.
 *
 * @property label 탭에 표시할 이름입니다.
 * @property count 탭 이름 오른쪽에 표시할 선택적 개수입니다.
 */
@Immutable
data class YadanTabItem(
    val label: String,
    val count: Int? = null,
)

/**
 * 야단법석 화면에서 공통으로 사용하는 밑줄형 탭 목록입니다.
 *
 * HTML의 `.rectabs`에 대응하며 다음 화면에서 사용합니다.
 *
 * - 여행 만들기의 맞춤 추천 및 찜
 * - 일정 편집의 관광지 추천 및 찜
 * - 친구 화면의 친구 및 요청
 *
 * 각 탭은 내용 너비만 차지하며 왼쪽부터 배치됩니다.
 * 선택된 탭에는 내용 너비와 같은 하늘색 밑줄을 표시합니다.
 *
 * @param tabs 표시할 탭 목록입니다.
 * @param selectedIndex 현재 선택된 탭의 인덱스입니다.
 * @param onTabSelected 탭을 눌렀을 때 선택한 인덱스를 전달합니다.
 * @param modifier 탭 목록의 크기와 배치에 사용할 Modifier입니다.
 * @param enabled 전체 탭의 활성화 여부입니다.
 */
@Composable
fun YadanTabRow(
    tabs: List<YadanTabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier.selectableGroup(),
        horizontalArrangement =
            Arrangement.spacedBy(TAB_HORIZONTAL_SPACING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEachIndexed { index, item ->
            YadanTab(
                item = item,
                selected = index == selectedIndex,
                onClick = {
                    onTabSelected(index)
                },
                enabled = enabled,
            )
        }
    }
}

/**
 * 탭 목록 안에서 사용하는 개별 탭입니다.
 *
 * 선택 동작은 탭 전체에 적용하고 ripple은 별도의 오버레이에서 그립니다.
 * 따라서 캡슐 형태의 ripple을 사용해도 글자와 숫자 배지는 잘리지 않습니다.
 */
@Composable
private fun YadanTab(
    item: YadanTabItem,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    val contentColor =
        if (selected) {
            YadanTextPrimary
        } else {
            YadanTextMuted
        }

    /*
     * IntrinsicSize.Max를 사용해 밑줄 너비가
     * 글자와 개수 배지를 합친 전체 내용 너비와 같아지게 합니다.
     */
    Box(
        modifier =
            modifier
                .alpha(
                    if (enabled) {
                        1f
                    } else {
                        DISABLED_ALPHA
                    },
                )
                .width(IntrinsicSize.Max)
                /*
                 * 선택 동작과 접근성 정보는 탭 전체가 담당합니다.
                 * 기본 indication은 사용하지 않고 아래 오버레이에서 그립니다.
                 */
                .selectable(
                    selected = selected,
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    role = Role.Tab,
                    onClick = onClick,
                ),
    ) {
        Row(
            modifier =
                Modifier.padding(
                    bottom = TAB_BOTTOM_PADDING,
                ),
            horizontalArrangement =
                Arrangement.spacedBy(TAB_CONTENT_SPACING),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.label,
                style =
                    YadanTypography.labelMedium.copy(
                        fontSize = TAB_LABEL_FONT_SIZE,
                        lineHeight = TAB_LABEL_LINE_HEIGHT,
                        fontWeight = FontWeight.ExtraBold,
                    ),
                color = contentColor,
                maxLines = 1,
                softWrap = false,
            )

            item.count?.let { count ->
                YadanTabCountBadge(
                    count = count,
                )
            }
        }

        /*
         * 콘텐츠는 자르지 않고 ripple만 탭 크기의
         * 캡슐형 영역 안에서 표시합니다.
         *
         * matchParentSize는 탭의 크기를 결정하지 않고,
         * 글자와 숫자 배지가 결정한 탭 크기만 따라갑니다.
         */
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .clip(YadanPillShape)
                    .indication(
                        interactionSource = interactionSource,
                        indication =
                            ripple(
                                bounded = true,
                                color = YadanPrimary,
                            ),
                    ),
        )

        /*
         * HTML의 ::after 밑줄처럼 레이아웃 높이를 늘리지 않고
         * 선택된 탭의 아래쪽에 겹쳐 표시합니다.
         */
        if (selected) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(TAB_INDICATOR_HEIGHT)
                        .background(
                            color = YadanPrimary,
                            shape = YadanPillShape,
                        ),
            )
        }
    }
}

/**
 * 탭 이름 오른쪽에 선택적으로 표시하는 개수 배지입니다.
 */
@Composable
private fun YadanTabCountBadge(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = YadanShapes.extraSmall,
        color = YadanPrimaryTintStrong,
        contentColor = YadanPrimary,
    ) {
        Text(
            text = count.toString(),
            modifier =
                Modifier.padding(
                    horizontal = COUNT_BADGE_HORIZONTAL_PADDING,
                    vertical = COUNT_BADGE_VERTICAL_PADDING,
                ),
            style =
                YadanTypography.labelSmall.copy(
                    fontSize = COUNT_BADGE_FONT_SIZE,
                    lineHeight = COUNT_BADGE_LINE_HEIGHT,
                    fontWeight = FontWeight.ExtraBold,
                ),
            maxLines = 1,
            softWrap = false,
        )
    }
}

/*
 * HTML의 rectabs와 탭 내부 간격입니다.
 */
private val TAB_HORIZONTAL_SPACING = 18.dp
private val TAB_CONTENT_SPACING = 5.dp
private val TAB_BOTTOM_PADDING = 7.dp

/*
 * HTML의 탭 글자와 선택 밑줄 크기입니다.
 */
private val TAB_LABEL_FONT_SIZE = 14.5.sp
private val TAB_LABEL_LINE_HEIGHT = 20.sp
private val TAB_INDICATOR_HEIGHT = 2.5.dp

/*
 * HTML의 rectabs i 개수 배지 크기입니다.
 */
private val COUNT_BADGE_HORIZONTAL_PADDING = 6.dp
private val COUNT_BADGE_VERTICAL_PADDING = 1.dp
private val COUNT_BADGE_FONT_SIZE = 10.5.sp
private val COUNT_BADGE_LINE_HEIGHT = 13.sp

private const val DISABLED_ALPHA = 0.42f

@Preview(
    name = "Yadan tab rows",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
)
@Composable
private fun YadanTabRowPreview() {
    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            /*
             * 여행 만들기 및 일정 편집 화면에서 사용하는 탭입니다.
             */
            YadanTabRow(
                tabs =
                    listOf(
                        YadanTabItem(
                            label = "맞춤 추천",
                        ),
                        YadanTabItem(
                            label = "찜",
                            count = 3,
                        ),
                    ),
                selectedIndex = 0,
                onTabSelected = {},
            )

            /*
             * 친구 화면에서 요청 탭을 선택한 상태입니다.
             */
            YadanTabRow(
                tabs =
                    listOf(
                        YadanTabItem(
                            label = "친구",
                            count = 4,
                        ),
                        YadanTabItem(
                            label = "요청",
                            count = 2,
                        ),
                    ),
                selectedIndex = 1,
                onTabSelected = {},
            )
        }
    }
}
