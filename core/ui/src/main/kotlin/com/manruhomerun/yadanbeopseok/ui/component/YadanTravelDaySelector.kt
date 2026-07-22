package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanDivider
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * 여행 일정에서 표시할 일차를 선택하는 분할형 선택 컴포넌트입니다.
 *
 * HTML의 `.daysel`, `.daysel span`, `.daysel span.on` 구조에 대응합니다.
 * 진행 중인 여행, 일정 편집과 여행 상세 화면에서 재사용합니다.
 *
 * [dayNumbers]에는 `Travel.days.map { it.day }`와 같이
 * 앱 내부 여행 모델의 일차 번호를 전달할 수 있습니다.
 *
 * @param dayNumbers 표시할 일차 번호 목록입니다. 일차는 1부터 시작합니다.
 * @param selectedDay 현재 선택된 일차 번호입니다.
 * @param onDaySelected 일차를 선택했을 때 해당 일차 번호를 전달합니다.
 * @param modifier 컴포넌트의 배치와 크기를 지정합니다.
 * @param enabled 전체 일차 선택 가능 여부입니다.
 */
@Composable
fun YadanTravelDaySelector(
    dayNumbers: List<Int>,
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    if (dayNumbers.isEmpty()) {
        return
    }

    /*
     * Material Surface의 기본 최소 레이아웃 크기를 해제하여
     * HTML의 작은 분할 선택 영역과 비슷한 시각적 높이를 유지합니다.
     * Material의 터치 영역 확장과 선택 접근성 처리는 그대로 사용합니다.
     */
    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides 0.dp,
    ) {
        Row(
            modifier =
                modifier
                    .alpha(
                        if (enabled) {
                            1f
                        } else {
                            0.42f
                        },
                    )
                    .background(
                        color = YadanDivider,
                        shape = YadanShapes.medium,
                    )
                    .padding(3.dp)
                    .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            dayNumbers.forEach { dayNumber ->
                YadanTravelDayItem(
                    dayNumber = dayNumber,
                    selected = dayNumber == selectedDay,
                    onClick = {
                        onDaySelected(dayNumber)
                    },
                    enabled = enabled,
                )
            }
        }
    }
}

/**
 * 일차 선택 영역 안에 표시되는 개별 일차 항목입니다.
 */
@Composable
private fun YadanTravelDayItem(
    dayNumber: Int,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val containerColor =
        if (selected) {
            YadanSurface
        } else {
            YadanDivider
        }

    val contentColor =
        if (selected) {
            YadanTextPrimary
        } else {
            YadanTextMuted
        }

    Surface(
        selected = selected,
        onClick = onClick,
        modifier =
            modifier.semantics {
                role = Role.Tab
            },
        enabled = enabled,
        shape = YadanShapes.small,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation =
            if (selected) {
                2.dp
            } else {
                0.dp
            },
    ) {
        Text(
            text = "DAY $dayNumber",
            modifier =
                Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 7.dp,
                ),
            style =
                YadanTypography.bodySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Preview(
    name = "Yadan travel day selector",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
)
@Composable
private fun YadanTravelDaySelectorPreview() {
    var selectedDay by remember {
        mutableIntStateOf(1)
    }

    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            YadanTravelDaySelector(
                dayNumbers = listOf(1, 2),
                selectedDay = selectedDay,
                onDaySelected = { day ->
                    selectedDay = day
                },
            )

            YadanTravelDaySelector(
                dayNumbers = listOf(1, 2, 3),
                selectedDay = 2,
                onDaySelected = {},
            )

            YadanTravelDaySelector(
                dayNumbers = listOf(1, 2),
                selectedDay = 1,
                onDaySelected = {},
                enabled = false,
            )
        }
    }
}
