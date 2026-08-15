package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanStatusChip
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanStatusChipStyle
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryGradient
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSummary
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

/**
 * 홈 화면에서 진행 중이거나 예정된 여행을 보여주는 카드입니다.
 *
 * 여행 목록 API의 요약 모델만 사용하며, 날짜를 기준으로 여행 중과
 * 여행 예정 상태를 구분합니다.
 *
 * @param travel 표시할 여행 요약 정보입니다.
 * @param currentDate D-day와 현재 여행 일차를 계산할 기준 날짜입니다.
 * @param onClick 일정 보기 버튼을 눌렀을 때 실행할 작업입니다.
 * @param modifier 카드의 크기와 배치를 지정할 Modifier입니다.
 * @param enabled 일정 보기 버튼의 활성화 여부입니다.
 */
@Composable
fun YadanTravelCard(
    travel: TravelSummary,
    currentDate: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val totalDayCount = travel.totalDayCount()
    val isActive = travel.isActive(currentDate)
    val currentDay =
        travel.currentDay(
            currentDate = currentDate,
            totalDayCount = totalDayCount,
        )
    val statusVisuals =
        travel.statusVisuals(
            currentDate = currentDate,
            currentDay = currentDay,
            totalDayCount = totalDayCount,
        )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = Color.Transparent,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 6.dp,
            ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanPrimaryGradient),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 236.dp)
                        .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                YadanTravelCardHeader(
                    statusText = statusVisuals.text,
                    statusStyle = statusVisuals.style,
                    isLeader = travel.isLeader,
                )

                Text(
                    text = travel.name,
                    style =
                        YadanTypography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                        ),
                    color = YadanOnPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (!isActive) {
                    YadanTravelDate(
                        dateText = travel.dateText(),
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = Color.Black.copy(alpha = 0.16f),
                    border =
                        BorderStroke(
                            width = 1.5.dp,
                            color = YadanOnPrimary.copy(alpha = 0.14f),
                        ),
                ) {
                    YadanGameMatchup(
                        homeTeam = travel.homeTeam,
                        awayTeam = travel.awayTeam,
                        style = YadanGameMatchupStyle.ON_DARK,
                        showHomeIndicator = false,
                        modifier =
                            Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 9.dp,
                            ),
                    )
                }

                if (isActive) {
                    YadanTravelProgress(
                        certifiedPlaceCount = travel.certifiedSpotsCount,
                        totalPlaceCount = travel.certificationTargetCount,
                        style = YadanTravelProgressStyle.ON_DARK,
                    )
                }

                YadanTravelScheduleButton(
                    onClick = onClick,
                    enabled = enabled,
                )
            }
        }
    }
}

/** 여행 상태와 현재 사용자의 참여 역할을 표시합니다. */
@Composable
private fun YadanTravelCardHeader(
    statusText: String,
    statusStyle: YadanStatusChipStyle,
    isLeader: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        YadanStatusChip(
            text = statusText,
            style = statusStyle,
            onDark = true,
        )

        YadanStatusChip(
            text = if (isLeader) "방장" else "동행자",
            style =
                if (isLeader) {
                    YadanStatusChipStyle.HOST
                } else {
                    YadanStatusChipStyle.GUEST
                },
            onDark = true,
            leadingIcon = {
                Icon(
                    imageVector =
                        if (isLeader) {
                            Icons.Default.Star
                        } else {
                            Icons.Default.Lock
                        },
                    contentDescription = null,
                )
            },
        )
    }
}

/** 여행 시작일과 종료일을 표시합니다. */
@Composable
private fun YadanTravelDate(
    dateText: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = YadanOnPrimary.copy(alpha = 0.82f),
        )

        Text(
            text = dateText,
            style =
                YadanTypography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
            color = YadanOnPrimary.copy(alpha = 0.88f),
        )
    }
}

/** 여행 상세 일정으로 이동하는 카드 내부 버튼입니다. */
@Composable
private fun YadanTravelScheduleButton(
    onClick: () -> Unit,
    enabled: Boolean,
) {
    Button(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(44.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = YadanSurface,
                contentColor = YadanPrimaryInk,
                disabledContainerColor = YadanSurface.copy(alpha = 0.55f),
                disabledContentColor = YadanPrimaryInk.copy(alpha = 0.55f),
            ),
        elevation = null,
        contentPadding =
            PaddingValues(
                horizontal = 16.dp,
                vertical = 0.dp,
            ),
    ) {
        Text(
            text = "일정 보기",
            style =
                YadanTypography.bodyMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
        )

        Spacer(modifier = Modifier.width(6.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
    }
}

/** 여행 시작일과 종료일을 기준으로 전체 여행 일수를 계산합니다. */
private fun TravelSummary.totalDayCount(): Int =
    (endDate.toEpochDays() - startDate.toEpochDays() + 1L)
        .coerceAtLeast(1L)
        .coerceAtMost(Int.MAX_VALUE.toLong())
        .toInt()

/** 기준 날짜가 여행 기간에 포함되는지 확인합니다. */
private fun TravelSummary.isActive(
    currentDate: LocalDate,
): Boolean =
    currentDate >= startDate &&
        currentDate <= endDate

/** 여행 중인 경우 기준 날짜가 몇 일차인지 계산합니다. */
private fun TravelSummary.currentDay(
    currentDate: LocalDate,
    totalDayCount: Int,
): Int =
    (currentDate.toEpochDays() - startDate.toEpochDays() + 1L)
        .coerceIn(
            minimumValue = 1L,
            maximumValue = totalDayCount.toLong(),
        )
        .toInt()

/** 기준 날짜를 사용해 여행 중 또는 여행 예정 칩 정보를 생성합니다. */
private fun TravelSummary.statusVisuals(
    currentDate: LocalDate,
    currentDay: Int,
    totalDayCount: Int,
): TravelStatusVisuals {
    if (isActive(currentDate)) {
        return TravelStatusVisuals(
            text = "여행 중 · DAY $currentDay/$totalDayCount",
            style = YadanStatusChipStyle.LIVE,
        )
    }

    val remainingDays =
        startDate.toEpochDays() - currentDate.toEpochDays()
    val dDayText =
        when {
            remainingDays > 0L -> "D-$remainingDays"
            remainingDays == 0L -> "D-DAY"
            else -> "여행 예정"
        }

    return TravelStatusVisuals(
        text = "$dDayText · ${region.displayName} 원정",
        style = YadanStatusChipStyle.PRIMARY,
    )
}

/** 여행 시작일과 종료일을 카드에 표시할 문자열로 변환합니다. */
private fun TravelSummary.dateText(): String =
    if (startDate == endDate) {
        startDate.toShortDateText()
    } else {
        "${startDate.toShortDateText()}~${endDate.toShortDateText()}"
    }

private fun LocalDate.toShortDateText(): String =
    "${month.number}.$day(${dayOfWeek.toKoreanShortName()})"

private data class TravelStatusVisuals(
    val text: String,
    val style: YadanStatusChipStyle,
)

@Preview(
    name = "Yadan travel cards",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
)
@Composable
private fun YadanTravelCardPreview() {
    val activeTravel =
        TravelSummary(
            id = "travel-1",
            name = "부산 원정 · 사직 직관 여행",
            startDate = LocalDate(2026, 5, 22),
            endDate = LocalDate(2026, 5, 23),
            baseballGameId = "game-1",
            homeTeam = KboTeam.LOTTE,
            awayTeam = KboTeam.KIA,
            region = Region.BUSAN,
            isLeader = true,
            spotsCount = 6,
            certificationTargetCount = 5,
            certifiedSpotsCount = 1,
            hasSticker = false,
        )

    val upcomingTravel =
        activeTravel.copy(
            id = "travel-2",
            name = "주말 부산 야구 여행",
            startDate = LocalDate(2026, 5, 23),
            endDate = LocalDate(2026, 5, 24),
            isLeader = false,
            certifiedSpotsCount = 0,
        )

    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "여행 중",
                style = YadanTypography.labelMedium,
                color = YadanTextMuted,
            )

            YadanTravelCard(
                travel = activeTravel,
                currentDate = LocalDate(2026, 5, 22),
                onClick = {},
            )

            Text(
                text = "여행 예정",
                style = YadanTypography.labelMedium,
                color = YadanTextMuted,
            )

            YadanTravelCard(
                travel = upcomingTravel,
                currentDate = LocalDate(2026, 5, 20),
                onClick = {},
            )
        }
    }
}
