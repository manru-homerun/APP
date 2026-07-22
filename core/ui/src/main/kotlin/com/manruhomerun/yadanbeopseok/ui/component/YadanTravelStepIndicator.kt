package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOutline
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPillShape
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * 여행 만들기 화면 상단에 표시하는 단계 진행 상태입니다.
 *
 * @param currentStepIndex 현재 단계의 0부터 시작하는 인덱스입니다.
 * @param modifier 컴포넌트의 크기와 배치를 지정합니다.
 * @param stepNames 각 단계에 표시할 이름입니다.
 */
@Composable
fun YadanTravelStepIndicator(
    currentStepIndex: Int,
    modifier: Modifier = Modifier,
    stepNames: List<String> = DEFAULT_TRAVEL_STEP_NAMES,
) {
    if (stepNames.isEmpty()) {
        return
    }

    // 화면 상태가 변경되는 순간 잘못된 인덱스가 전달돼도 안전하게 보정합니다.
    val selectedStepIndex =
        currentStepIndex.coerceIn(
            minimumValue = 0,
            maximumValue = stepNames.lastIndex,
        )

    val currentStepNumber = selectedStepIndex + 1
    val totalStepCount = stepNames.size
    val currentStepName = stepNames[selectedStepIndex]
    val progress = currentStepNumber.toFloat() / totalStepCount

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    start = 24.dp,
                    top = 2.dp,
                    end = 24.dp,
                    bottom = 9.dp,
                )
                .semantics(mergeDescendants = true) {
                    contentDescription = "여행 만들기 진행 단계"
                    stateDescription =
                        "${totalStepCount}단계 중 " +
                            "${currentStepNumber}단계, $currentStepName"

                    progressBarRangeInfo =
                        ProgressBarRangeInfo(
                            current = progress,
                            range = 0f..1f,
                        )
                },
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            repeat(totalStepCount) { stepIndex ->
                YadanTravelStepSegment(
                    completed = stepIndex <= selectedStepIndex,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "STEP $currentStepNumber · $currentStepName",
                modifier = Modifier.weight(1f),
                style =
                    YadanTypography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                color = YadanPrimaryInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = "$currentStepNumber / $totalStepCount",
                style =
                    YadanTypography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                color = YadanTextMuted,
                maxLines = 1,
            )
        }
    }
}

/**
 * 완료 여부에 따라 색상이 변경되는 개별 단계 막대입니다.
 */
@Composable
private fun YadanTravelStepSegment(
    completed: Boolean,
    modifier: Modifier = Modifier,
) {
    val segmentColor by
    animateColorAsState(
        targetValue =
            if (completed) {
                YadanPrimary
            } else {
                YadanOutline
            },
        animationSpec =
            tween(
                durationMillis = 300,
            ),
        label = "YadanTravelStepSegmentColor",
    )

    Box(
        modifier =
            modifier
                .height(5.dp)
                .background(
                    color = segmentColor,
                    shape = YadanPillShape,
                ),
    )
}

/**
 * HTML에 정의된 여행 만들기 6단계입니다.
 *
 * 기본 흐름에서 공통으로 사용하되, 흐름이 달라지는 경우
 * stepNames 매개변수로 다른 단계 목록을 전달할 수 있습니다.
 */
private val DEFAULT_TRAVEL_STEP_NAMES =
    listOf(
        "경기 선택",
        "동행 조건",
        "목적·스타일",
        "동행자",
        "여행 기간",
        "관광지 담기",
    )

@Preview(
    name = "Yadan travel step indicator",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
)
@Composable
private fun YadanTravelStepIndicatorPreview() {
    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            YadanTravelStepIndicator(
                currentStepIndex = 0,
            )

            YadanTravelStepIndicator(
                currentStepIndex = 2,
            )

            YadanTravelStepIndicator(
                currentStepIndex = 5,
            )
        }
    }
}
