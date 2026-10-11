package com.manruhomerun.yadanbeopseok.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOutline
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPillShape
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * 현재 페이지 위치를 보여주는 공통 페이지 표시기입니다.
 *
 * HTML의 `.pdots`에 대응하며 홈 화면의
 * `내 원정 여행` 카드 캐러셀 아래에서 사용합니다.
 *
 * 페이지가 한 개 이하라면 HTML의 `solo` 상태와 동일하게
 * 아무것도 표시하지 않습니다.
 * 최대 9개 점을 표시하며 숨겨진 페이지가 있는 방향의 양끝 점을 축소합니다.
 *
 * @param pageCount 전체 페이지 개수입니다.
 * @param currentPage 현재 선택된 페이지의 인덱스입니다.
 * @param modifier 페이지 표시기의 크기와 배치에 사용할 Modifier입니다.
 */
@Composable
fun YadanPageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    if (pageCount <= 1) {
        return
    }

    val selectedPage = currentPage.coerceIn(0, pageCount - 1)
    val visibleCount = minOf(pageCount, MAX_VISIBLE_INDICATORS)
    val startPage = (selectedPage - MAX_VISIBLE_INDICATORS / 2).coerceIn(0, pageCount - visibleCount)
    val endPageExclusive = startPage + visibleCount
    val hasPreviousPages = startPage > 0
    val hasNextPages = endPageExclusive < pageCount

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(INDICATOR_SIZE)
                /*
                 * 페이지 점은 시각적 표시만 담당하므로
                 * 전체 표시기를 하나의 접근성 요소로 제공합니다.
                 */
                .semantics(
                    mergeDescendants = true,
                ) {
                    contentDescription = "페이지"
                    stateDescription = "${selectedPage + 1} / $pageCount"
                },
        horizontalArrangement =
            Arrangement.spacedBy(
                space = INDICATOR_SPACING,
                alignment = Alignment.CenterHorizontally,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 전역 페이지가 아닌 표시 슬롯을 유지해 구간이 바뀌어도 크기 애니메이션을 이어갑니다.
        repeat(visibleCount) { slot ->
            val pageIndex = startPage + slot
            val size = when {
                (hasPreviousPages && slot == 0) || (hasNextPages && slot == visibleCount - 1) -> OUTER_INDICATOR_SIZE
                (hasPreviousPages && slot == 1) || (hasNextPages && slot == visibleCount - 2) -> INNER_INDICATOR_SIZE
                else -> INDICATOR_SIZE
            }

            YadanPageIndicatorItem(
                selected = pageIndex == selectedPage,
                size = size,
            )
        }
    }
}

/**
 * 페이지 표시기를 구성하는 개별 점입니다.
 *
 * 선택되면 축소 크기보다 선택 크기를 우선하고 대표 색상으로 변경됩니다.
 */
@Composable
private fun YadanPageIndicatorItem(
    selected: Boolean,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val targetWidth = if (selected) SELECTED_INDICATOR_WIDTH else size
    val targetHeight = if (selected) INDICATOR_SIZE else size
    val targetColor = if (selected) YadanPrimary else YadanOutline

    /*
     * HTML의 transition: .2s에 대응합니다.
     * 선택 위치와 표시 구간이 변경될 때 크기와 색상을 함께 전환합니다.
     */
    val animatedWidth by
    animateDpAsState(
        targetValue = targetWidth,
        animationSpec =
            tween(
                durationMillis = INDICATOR_ANIMATION_DURATION_MILLIS,
            ),
        label = "YadanPageIndicatorWidth",
    )

    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = tween(durationMillis = INDICATOR_ANIMATION_DURATION_MILLIS),
        label = "YadanPageIndicatorHeight",
    )

    val animatedColor by
    animateColorAsState(
        targetValue = targetColor,
        animationSpec =
            tween(
                durationMillis = INDICATOR_ANIMATION_DURATION_MILLIS,
            ),
        label = "YadanPageIndicatorColor",
    )

    Box(
        modifier =
            modifier
                .width(animatedWidth)
                .height(animatedHeight)
                .background(
                    color = animatedColor,
                    shape = YadanPillShape,
                ),
    )
}

/*
 * 페이지 표시기에서만 사용하는 크기입니다.
 *
 * 다른 컴포넌트의 동일한 숫자와 역할이 다르므로
 * 공통 크기 토큰으로 분리하지 않고 이 파일 안에서 관리합니다.
 */
private val INDICATOR_SIZE = 6.dp
private val OUTER_INDICATOR_SIZE = 2.dp
private val INNER_INDICATOR_SIZE = 4.dp
private val SELECTED_INDICATOR_WIDTH = 17.dp
private val INDICATOR_SPACING = 5.dp

private const val INDICATOR_ANIMATION_DURATION_MILLIS = 200
private const val MAX_VISIBLE_INDICATORS = 9

@Preview(
    name = "Yadan page indicators",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
)
@Composable
private fun YadanPageIndicatorPreview() {
    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            /*
             * 여행 카드가 세 개이며 첫 번째 카드가 선택된 상태입니다.
             */
            YadanPageIndicator(
                pageCount = 3,
                currentPage = 0,
            )

            /*
             * 여행 카드가 세 개이며 두 번째 카드가 선택된 상태입니다.
             */
            YadanPageIndicator(
                pageCount = 3,
                currentPage = 1,
            )

            /*
             * 여행 카드가 세 개이며 마지막 카드가 선택된 상태입니다.
             */
            YadanPageIndicator(
                pageCount = 3,
                currentPage = 2,
            )

            /*
             * 여행 카드가 두 개이며 두 번째 카드가 선택된 상태입니다.
             */
            YadanPageIndicator(
                pageCount = 2,
                currentPage = 1,
            )
        }
    }
}

@Preview(name = "페이지 점 - 개수와 구간 경계", showBackground = true, widthDp = 320)
@Composable
private fun YadanPageIndicatorBoundaryPreview() {
    YadanbeopseokTheme {
        Column(
            modifier = Modifier.fillMaxWidth().background(YadanBackground).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            YadanPageIndicator(pageCount = 0, currentPage = 0)
            YadanPageIndicator(pageCount = 1, currentPage = 0)
            YadanPageIndicator(pageCount = 2, currentPage = 0)
            YadanPageIndicator(pageCount = 9, currentPage = 4)
            YadanPageIndicator(pageCount = 10, currentPage = 0)
            YadanPageIndicator(pageCount = 10, currentPage = 4)
            YadanPageIndicator(pageCount = 10, currentPage = 5)
            YadanPageIndicator(pageCount = 10, currentPage = 9)
            YadanPageIndicator(pageCount = 30, currentPage = 0)
            YadanPageIndicator(pageCount = 30, currentPage = 4)
            YadanPageIndicator(pageCount = 30, currentPage = 5)
            YadanPageIndicator(pageCount = 30, currentPage = 14)
            YadanPageIndicator(pageCount = 30, currentPage = 24)
            YadanPageIndicator(pageCount = 30, currentPage = 25)
            YadanPageIndicator(pageCount = 30, currentPage = 29)
        }
    }
}
