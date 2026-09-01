package com.manruhomerun.yadanbeopseok.travel.course.screen

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButtonStyle
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanStatusChip
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanStatusChipSize
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanStatusChipStyle
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryGradient
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import kotlinx.datetime.LocalDate

/**
 * C02에서 여행 저장 완료 결과와 저장된 여행 요약을 표시합니다.
 *
 * 실제 홈 이동은 Route가 [onHomeClick]으로 연결합니다.
 */
@Composable
fun TravelCourseSavedScreen(
    travelName: String,
    travelSpotCount: Int,
    companionCount: Int,
    startDate: LocalDate?,
    currentDate: LocalDate,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(YadanPrimaryGradient)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                SavedTravelContent(
                    travelName = travelName,
                    travelSpotCount = travelSpotCount,
                    companionCount = companionCount,
                    dDayText = startDate?.toDdayText(currentDate),
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                )
            }

            YadanButton(
                text = "나의 여행에서 보기",
                onClick = onHomeClick,
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .padding(
                        horizontal = 28.dp,
                        vertical = 24.dp,
                    ),
                style = YadanButtonStyle.GHOST,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
        }
    }
}

@Composable
private fun SavedTravelContent(
    travelName: String,
    travelSpotCount: Int,
    companionCount: Int,
    dDayText: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CompletionBadge()

        Spacer(modifier = Modifier.size(24.dp))

        Text(
            text = "여행 계획 완성!",
            style = YadanTypography.headlineSmall,
            color = YadanOnPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.size(8.dp))

        Text(
            text = "나의 여행에 저장됐어요",
            style = YadanTypography.bodySmall,
            color = YadanOnPrimary.copy(alpha = 0.72f),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.size(28.dp))

        SavedTravelSummary(
            travelName = travelName,
            travelSpotCount = travelSpotCount,
            companionCount = companionCount,
            dDayText = dDayText,
        )
    }
}

@Composable
private fun CompletionBadge() {
    Box(
        modifier = Modifier.size(144.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(144.dp)
                .border(
                    width = 2.dp,
                    color = YadanPrimary.copy(alpha = 0.14f),
                    shape = CircleShape,
                ),
        )

        Box(
            modifier = Modifier
                .size(118.dp)
                .border(
                    width = 2.dp,
                    color = YadanPrimary.copy(alpha = 0.28f),
                    shape = CircleShape,
                ),
        )

        Box(
            modifier = Modifier
                .size(92.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = CircleShape,
                )
                .background(
                    color = YadanPrimary,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = YadanOnPrimary,
            )
        }
    }
}

@Composable
private fun SavedTravelSummary(
    travelName: String,
    travelSpotCount: Int,
    companionCount: Int,
    dDayText: String?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = YadanOnPrimary.copy(alpha = 0.08f),
        border = BorderStroke(
            width = 1.5.dp,
            color = YadanOnPrimary.copy(alpha = 0.16f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 12.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = travelName,
                    style = YadanTypography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    color = YadanOnPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.size(3.dp))

                Text(
                    text = "코스 ${travelSpotCount.coerceAtLeast(0)}곳 · " +
                        "동행 ${companionCount.coerceAtLeast(0)}명",
                    style = YadanTypography.labelSmall,
                    color = YadanOnPrimary.copy(alpha = 0.62f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            dDayText?.let { text ->
                YadanStatusChip(
                    text = text,
                    style = YadanStatusChipStyle.PRIMARY,
                    size = YadanStatusChipSize.SMALL,
                    onDark = true,
                )
            }
        }
    }
}

private fun LocalDate.toDdayText(currentDate: LocalDate): String? {
    val remainingDays = toEpochDays() - currentDate.toEpochDays()

    return when {
        remainingDays > 0L -> "D-$remainingDays"
        remainingDays == 0L -> "D-DAY"
        else -> null
    }
}

@Preview(
    name = "C02 저장 완료",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelCourseSavedScreenPreview() {
    YadanbeopseokTheme {
        TravelCourseSavedScreen(
            travelName = "부산 사직 직관 여행",
            travelSpotCount = 5,
            companionCount = 2,
            startDate = LocalDate(2026, 5, 23),
            currentDate = LocalDate(2026, 5, 20),
            onHomeClick = {},
        )
    }
}
