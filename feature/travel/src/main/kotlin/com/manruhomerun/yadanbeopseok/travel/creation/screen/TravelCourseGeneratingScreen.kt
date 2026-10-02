package com.manruhomerun.yadanbeopseok.travel.creation.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryDark
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import kotlinx.coroutines.delay

private const val GENERATION_STEP_DELAY_MILLIS = 600L
private const val GENERATION_COMPLETION_DELAY_MILLIS = 300L

private data class GenerationStep(
    val title: String,
    val description: String,
)

private enum class GenerationStepState {
    PENDING,
    ACTIVE,
    COMPLETED,
}

private val generationSteps = listOf(
    GenerationStep(
        title = "취향·동행 취향 종합",
        description = "취향과 동행 조건을 종합하는 중이에요",
    ),
    GenerationStep(
        title = "베리어프리 조건 필터링",
        description = "베리어프리 조건을 확인하는 중이에요",
    ),
    GenerationStep(
        title = "주변 관광지·맛집 탐색",
        description = "주변 관광지와 맛집을 찾는 중이에요",
    ),
    GenerationStep(
        title = "방문 순서 최적화",
        description = "방문 순서를 최적화하는 중이에요",
    ),
)

/**
 * B·06에서 추천 여행 코스를 생성하는 동안 표시하는 화면입니다.
 *
 * 표시되는 단계는 사용자에게 작업 내용을 안내하기 위한 시각적 정보이며,
 * 실제 생성 완료 여부는 API 응답과 ViewModel 상태로 판단합니다.
 */
@Composable
fun TravelCourseGeneratingScreen(
    regionName: String,
    generationAttemptId: Int,
    isGeneratedCourseReady: Boolean,
    onPresentationFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var completedStepCount by rememberSaveable(generationAttemptId) {
        mutableIntStateOf(0)
    }
    val currentOnPresentationFinished by rememberUpdatedState(onPresentationFinished)

    LaunchedEffect(generationAttemptId) {
        while (completedStepCount < generationSteps.lastIndex) {
            delay(GENERATION_STEP_DELAY_MILLIS)
            completedStepCount = (completedStepCount + 1).coerceAtMost(generationSteps.lastIndex)
        }
    }

    LaunchedEffect(isGeneratedCourseReady, completedStepCount, generationAttemptId) {
        if (isGeneratedCourseReady && completedStepCount == generationSteps.lastIndex) {
            completedStepCount = generationSteps.size
        }
    }

    LaunchedEffect(completedStepCount, generationAttemptId) {
        if (completedStepCount == generationSteps.size) {
            delay(GENERATION_COMPLETION_DELAY_MILLIS)
            currentOnPresentationFinished()
        }
    }

    TravelCourseGeneratingContent(
        regionName = regionName,
        completedStepCount = completedStepCount,
        modifier = modifier,
    )
}

@Composable
private fun TravelCourseGeneratingContent(
    regionName: String,
    completedStepCount: Int,
    modifier: Modifier = Modifier,
) {
    val safeCompletedStepCount = completedStepCount.coerceIn(0, generationSteps.size)
    val activeStep = generationSteps.getOrNull(safeCompletedStepCount)
    val progressDescription = if (activeStep == null) {
        "${generationSteps.size}단계 완료"
    } else {
        "${generationSteps.size}단계 중 ${safeCompletedStepCount + 1}단계, ${activeStep.title} 진행 중"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(YadanPrimaryDark)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Icon(
            imageVector = Icons.Default.SportsBaseball,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(
                    x = 58.dp,
                    y = -18.dp,
                )
                .size(240.dp),
            tint = YadanOnPrimary.copy(alpha = 0.08f),
        )

        Icon(
            imageVector = Icons.Outlined.Map,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(
                    x = -42.dp,
                    y = -60.dp,
                )
                .size(150.dp),
            tint = YadanOnPrimary.copy(alpha = 0.06f),
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 26.dp)
                .semantics {
                    contentDescription = "여행 코스 생성 중"
                    stateDescription = progressDescription
                    progressBarRangeInfo = ProgressBarRangeInfo(
                        current = safeCompletedStepCount.toFloat(),
                        range = 0f..generationSteps.size.toFloat(),
                        steps = generationSteps.lastIndex,
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "$regionName 여행 코스를\n짜고 있어요",
                style = YadanTypography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
                color = YadanOnPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(9.dp))

            Text(
                text = activeStep?.description ?: "여행 코스를 완성했어요",
                style = YadanTypography.bodySmall,
                color = YadanOnPrimary.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(30.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                generationSteps.forEachIndexed { index, step ->
                    val state = when {
                        index < safeCompletedStepCount -> GenerationStepState.COMPLETED
                        index == safeCompletedStepCount -> GenerationStepState.ACTIVE
                        else -> GenerationStepState.PENDING
                    }

                    GenerationProgressItem(
                        title = step.title,
                        state = state,
                    )
                }
            }
        }
    }
}

/**
 * 코스 생성 과정의 개별 진행 단계를 표시합니다.
 */
@Composable
private fun GenerationProgressItem(
    title: String,
    state: GenerationStepState,
    modifier: Modifier = Modifier,
) {
    val progressDescription = when (state) {
        GenerationStepState.PENDING -> "대기 중"
        GenerationStepState.ACTIVE -> "진행 중"
        GenerationStepState.COMPLETED -> "완료"
    }
    val isCompleted = state == GenerationStepState.COMPLETED

    val containerColor by animateColorAsState(
        targetValue = when (state) {
            GenerationStepState.PENDING -> YadanOnPrimary.copy(alpha = 0.07f)
            GenerationStepState.ACTIVE -> YadanOnPrimary.copy(alpha = 0.16f)
            GenerationStepState.COMPLETED -> YadanOnPrimary.copy(alpha = 0.1f)
        },
        animationSpec = tween(durationMillis = 220),
        label = "generation_item_container",
    )
    val borderColor by animateColorAsState(
        targetValue = when (state) {
            GenerationStepState.PENDING -> YadanOnPrimary.copy(alpha = 0.12f)
            GenerationStepState.ACTIVE -> YadanOnPrimary.copy(alpha = 0.5f)
            GenerationStepState.COMPLETED -> YadanOnPrimary.copy(alpha = 0.15f)
        },
        animationSpec = tween(durationMillis = 220),
        label = "generation_item_border",
    )
    val checkBackgroundColor by animateColorAsState(
        targetValue = if (isCompleted) YadanOnPrimary else YadanOnPrimary.copy(alpha = 0f),
        animationSpec = tween(durationMillis = 220),
        label = "generation_item_check_background",
    )
    val checkBorderColor by animateColorAsState(
        targetValue = when (state) {
            GenerationStepState.PENDING -> YadanOnPrimary.copy(alpha = 0.3f)
            GenerationStepState.ACTIVE -> YadanOnPrimary.copy(alpha = 0.9f)
            GenerationStepState.COMPLETED -> YadanOnPrimary
        },
        animationSpec = tween(durationMillis = 220),
        label = "generation_item_check_border",
    )
    val textColor by animateColorAsState(
        targetValue = when (state) {
            GenerationStepState.PENDING -> YadanOnPrimary.copy(alpha = 0.62f)
            GenerationStepState.ACTIVE -> YadanOnPrimary
            GenerationStepState.COMPLETED -> YadanOnPrimary.copy(alpha = 0.92f)
        },
        animationSpec = tween(durationMillis = 220),
        label = "generation_item_text",
    )

    Surface(
        modifier = modifier.semantics {
            stateDescription = progressDescription
        },
        shape = YadanShapes.medium,
        color = containerColor,
        contentColor = YadanOnPrimary,
        border = BorderStroke(
            width = 1.5.dp,
            color = borderColor,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 13.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(
                        color = checkBackgroundColor,
                        shape = CircleShape,
                    )
                    .border(
                        width = 2.dp,
                        color = checkBorderColor,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = YadanPrimaryDark,
                    )
                }
            }

            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = YadanTypography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = textColor,
            )
        }
    }
}

@Preview(
    name = "B06d 첫 단계 진행 중",
    showBackground = true,
    backgroundColor = 0xFF3E7AC2,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelCourseGeneratingFirstStepPreview() {
    YadanbeopseokTheme {
        TravelCourseGeneratingContent(
            regionName = "부산",
            completedStepCount = 0,
        )
    }
}

@Preview(
    name = "B06d 세 번째 단계 진행 중",
    showBackground = true,
    backgroundColor = 0xFF3E7AC2,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelCourseGeneratingThirdStepPreview() {
    YadanbeopseokTheme {
        TravelCourseGeneratingContent(
            regionName = "부산",
            completedStepCount = 2,
        )
    }
}

@Preview(
    name = "B06d 마지막 단계 진행 중",
    showBackground = true,
    backgroundColor = 0xFF3E7AC2,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelCourseGeneratingLastStepPreview() {
    YadanbeopseokTheme {
        TravelCourseGeneratingContent(
            regionName = "부산",
            completedStepCount = 3,
        )
    }
}

@Preview(
    name = "B06d 전체 완료",
    showBackground = true,
    backgroundColor = 0xFF3E7AC2,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelCourseGeneratingCompletedPreview() {
    YadanbeopseokTheme {
        TravelCourseGeneratingContent(
            regionName = "부산",
            completedStepCount = 4,
        )
    }
}
