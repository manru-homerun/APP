package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCard
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanStatusChip
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOutline
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.TravelStyleScore
import kotlin.math.roundToInt

/**
 * 사용자의 자연·도시 여행 선호도를 1~7단계로 선택하는 슬라이더입니다.
 *
 * G05 여행 프로필과 H03 취향 수정 화면에서 재사용합니다.
 * Material3 Slider의 터치, 키보드, 접근성 처리를 사용하면서
 * HTML의 7개 원형 눈금과 강조된 현재 위치를 적용합니다.
 *
 * @param score 현재 선택된 여행 스타일 점수입니다.
 * @param onScoreChange 사용자가 새로운 점수를 선택했을 때 실행할 작업입니다.
 * @param modifier 컴포넌트의 크기와 배치를 지정합니다.
 * @param enabled 슬라이더 조작 가능 여부입니다.
 * @param onValueChangeFinished 사용자가 점수 선택을 마쳤을 때 실행할 작업입니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YadanTravelStyleSlider(
    score: TravelStyleScore,
    onScoreChange: (TravelStyleScore) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    YadanCard(
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
    ) {
        Column(
            modifier =
                Modifier.padding(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 18.dp,
                ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "🌿 자연",
                    style =
                        YadanTypography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                        ),
                    color = YadanTextPrimary,
                )

                Text(
                    text = "🏙 도시",
                    style =
                        YadanTypography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                        ),
                    color = YadanTextPrimary,
                )
            }

            Slider(
                value = score.value.toFloat(),
                onValueChange = { value ->
                    val changedScore =
                        TravelStyleScore(
                            value = value.roundToInt().coerceIn(1, 7),
                        )

                    if (changedScore != score) {
                        onScoreChange(changedScore)
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "여행 스타일"
                            stateDescription =
                                "7단계 중 ${score.value}단계, ${score.description}"
                        },
                enabled = enabled,
                valueRange = 1f..7f,
                steps = 5,
                onValueChangeFinished = onValueChangeFinished,
                thumb = {
                    YadanTravelStyleThumb()
                },
                track = { sliderState ->
                    YadanTravelStyleTrack(
                        selectedFraction = sliderState.coercedValueAsFraction,
                    )
                },
            )

            /*
             * Slider의 48dp 터치 영역에 포함된 아래쪽 여백과 합쳐져
             * HTML의 트랙과 결과 칩 사이 16dp 간격에 가깝게 표시됩니다.
             */
            Box(
                modifier =
                    Modifier
                        .padding(top = 3.dp)
                        .align(Alignment.CenterHorizontally),
            ) {
                /*
                 * 선택 결과는 Slider의 stateDescription으로 이미 안내되므로
                 * TalkBack에서 같은 문구를 중복으로 읽지 않게 합니다.
                 */
                YadanStatusChip(
                    text = score.description,
                    modifier = Modifier.clearAndSetSemantics {},
                )
            }
        }
    }
}

/**
 * HTML의 5dp 트랙과 7개 원형 눈금을 그립니다.
 */
@Composable
private fun YadanTravelStyleTrack(
    selectedFraction: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(22.dp),
    ) {
        val dotCount = 7
        val lastDotIndex = dotCount - 1
        val selectedDotIndex =
            (selectedFraction * lastDotIndex)
                .roundToInt()
                .coerceIn(0, lastDotIndex)

        val centerY = size.height / 2f
        val trackStrokeWidth = 5.dp.toPx()
        val dotRadius = 6.dp.toPx()
        val dotBorderWidth = 2.dp.toPx()
        val selectedTrackEndX = size.width * selectedFraction

        drawLine(
            color = YadanOutline,
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = trackStrokeWidth,
            cap = StrokeCap.Round,
        )

        drawLine(
            color = YadanPrimary,
            start = Offset(0f, centerY),
            end = Offset(selectedTrackEndX, centerY),
            strokeWidth = trackStrokeWidth,
            cap = StrokeCap.Round,
        )

        repeat(dotCount) { dotIndex ->
            val dotFraction = dotIndex.toFloat() / lastDotIndex
            val dotCenter =
                Offset(
                    x = size.width * dotFraction,
                    y = centerY,
                )
            val activated = dotIndex <= selectedDotIndex

            drawCircle(
                color =
                    if (activated) {
                        YadanPrimary
                    } else {
                        YadanSurface
                    },
                radius = dotRadius,
                center = dotCenter,
            )

            if (!activated) {
                drawCircle(
                    color = YadanOutline,
                    radius = dotRadius,
                    center = dotCenter,
                    style = Stroke(width = dotBorderWidth),
                )
            }
        }
    }
}

/**
 * 현재 점수를 표시하는 22dp 원형 조절점입니다.
 */
@Composable
private fun YadanTravelStyleThumb() {
    Box(
        modifier =
            Modifier
                .size(22.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    clip = false,
                )
                .background(
                    color = YadanPrimary,
                    shape = CircleShape,
                ),
    )
}

/**
 * 점수에 해당하는 사용자용 여행 스타일 문구입니다.
 */
private val TravelStyleScore.description: String
    get() =
        when (value) {
            1 -> "자연 매우 선호"
            2 -> "자연 중간 선호"
            3 -> "자연 약간 선호"
            4 -> "중립"
            5 -> "도시 약간 선호"
            6 -> "도시 중간 선호"
            7 -> "도시 매우 선호"
            else -> error("TravelStyleScore must be between 1 and 7.")
        }

@Preview(
    name = "Yadan travel style slider",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
)
@Composable
private fun YadanTravelStyleSliderPreview() {
    var score by remember {
        mutableStateOf(TravelStyleScore(3))
    }

    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
        ) {
            YadanTravelStyleSlider(
                score = score,
                onScoreChange = {
                    score = it
                },
            )
        }
    }
}
