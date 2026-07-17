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
import androidx.compose.ui.tooling.preview.Preview
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
    /*
     * 카드가 하나뿐이거나 없는 경우에는 페이지 이동이 없으므로
     * 표시기를 구성하지 않습니다.
     */
    if (pageCount <= 1) {
        return
    }

    /*
     * Pager 상태가 갱신되는 순간 잘못된 인덱스가 전달되더라도
     * 유효한 범위 안에서 선택 상태를 표시합니다.
     */
    val selectedPage =
        currentPage.coerceIn(
            minimumValue = 0,
            maximumValue = pageCount - 1,
        )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(
                space = INDICATOR_SPACING,
                alignment = Alignment.CenterHorizontally,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { pageIndex ->
            YadanPageIndicatorItem(
                selected = pageIndex == selectedPage,
            )
        }
    }
}

/**
 * 페이지 표시기를 구성하는 개별 점입니다.
 *
 * 선택되면 너비가 늘어나고 대표 색상으로 변경됩니다.
 */
@Composable
private fun YadanPageIndicatorItem(
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val targetWidth =
        if (selected) {
            SELECTED_INDICATOR_WIDTH
        } else {
            INDICATOR_SIZE
        }

    val targetColor =
        if (selected) {
            YadanPrimary
        } else {
            YadanOutline
        }

    /*
     * HTML의 transition: .2s에 대응합니다.
     * 선택 위치가 변경될 때 너비와 색상을 함께 전환합니다.
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
                .height(INDICATOR_SIZE)
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
private val SELECTED_INDICATOR_WIDTH = 17.dp
private val INDICATOR_SPACING = 5.dp

private const val INDICATOR_ANIMATION_DURATION_MILLIS = 200

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
