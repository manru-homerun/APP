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
import com.manruhomerun.yadanbeopseok.model.BaseballGame
import com.manruhomerun.yadanbeopseok.model.BaseballGameType
import com.manruhomerun.yadanbeopseok.model.BaseballStadium
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.LoginProvider
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.Travel
import com.manruhomerun.yadanbeopseok.model.TravelCertification
import com.manruhomerun.yadanbeopseok.model.TravelDay
import com.manruhomerun.yadanbeopseok.model.TravelParticipant
import com.manruhomerun.yadanbeopseok.model.TravelPlace
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import com.manruhomerun.yadanbeopseok.model.TravelStatus
import com.manruhomerun.yadanbeopseok.model.UserProfile
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number

/**
 * 홈 화면에서 진행 중이거나 예정된 여행을 보여주는 카드입니다.
 *
 * @param travel 표시할 여행입니다.
 * @param currentUserId 현재 로그인한 사용자의 ID입니다.
 * @param currentDate D-day와 현재 여행 일차를 계산할 기준 날짜입니다.
 * @param onClick 일정 보기 버튼을 눌렀을 때 실행할 작업입니다.
 * @param modifier 카드의 크기와 배치를 지정할 Modifier입니다.
 * @param enabled 일정 보기 버튼의 활성화 여부입니다.
 */
@Composable
fun YadanTravelCard(
    travel: Travel,
    currentUserId: String,
    currentDate: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val totalDayCount = travel.totalDayCount()
    val currentDay = travel.currentDay(currentDate, totalDayCount)
    val statusVisuals =
        travel.statusVisuals(
            currentDate = currentDate,
            currentDay = currentDay,
            totalDayCount = totalDayCount,
        )
    val isLeader =
        travel.participants.any { participant ->
            participant.user.id == currentUserId && participant.isLeader
        }

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
            /*
             * HTML의 min-height: 236px와
             * justify-content: space-between에 대응합니다.
             *
             * 내용이 적은 예정 여행 카드에서도 버튼 아래쪽에
             * 남는 공간이 몰리지 않고 요소 사이에 고르게 분배됩니다.
             */
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
                    isLeader = isLeader,
                )

                Text(
                    text = travel.displayName(),
                    style =
                        YadanTypography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                        ),
                    color = YadanOnPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (travel.status != TravelStatus.ACTIVE) {
                    travel.dateText()?.let { dateText ->
                        YadanTravelDate(
                            dateText = dateText,
                        )
                    }
                }

                /*
                 * HTML 히어로 여행 카드의 대진 영역처럼
                 * 반투명 검정 배경과 옅은 흰색 테두리를 적용합니다.
                 */
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
                        homeTeam = travel.baseballGame.homeTeam,
                        awayTeam = travel.baseballGame.awayTeam,
                        style = YadanGameMatchupStyle.ON_DARK,
                        showHomeIndicator = false,
                        modifier =
                            Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 9.dp,
                            ),
                    )
                }

                if (travel.status == TravelStatus.ACTIVE) {
                    YadanTravelProgress(
                        certifiedPlaceCount =
                            travel.days.sumOf { day ->
                                day.places.count { place ->
                                    place.spot.category !=
                                        TravelSpotCategory.ACCOMMODATION &&
                                        place.isCertifiedBy(currentUserId)
                                }
                            },
                        totalPlaceCount =
                            travel.days.sumOf { day ->
                                day.places.count { place ->
                                    place.spot.category !=
                                        TravelSpotCategory.ACCOMMODATION
                                }
                            },
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

/**
 * 여행 상태와 현재 사용자의 참여 역할을 표시합니다.
 */
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

/**
 * 여행 시작일과 종료일을 표시합니다.
 */
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

/**
 * 여행 상세 일정으로 이동하는 카드 내부 버튼입니다.
 */
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

private fun Travel.displayName(): String =
    name
        ?.takeIf { it.isNotBlank() }
        ?: "${region.displayName} 원정 여행"

private fun Travel.totalDayCount(): Int {
    val start = startDate
    val end = endDate

    return when {
        start != null && end != null ->
            (end.toEpochDays() - start.toEpochDays() + 1L)
                .coerceAtLeast(1L)
                .coerceAtMost(Int.MAX_VALUE.toLong())
                .toInt()

        else ->
            days
                .maxOfOrNull { it.day }
                ?.coerceAtLeast(1)
                ?: 1
    }
}

private fun Travel.currentDay(
    currentDate: LocalDate,
    totalDayCount: Int,
): Int {
    val start = startDate ?: return 1

    return (currentDate.toEpochDays() - start.toEpochDays() + 1L)
        .coerceIn(
            minimumValue = 1L,
            maximumValue = totalDayCount.toLong(),
        )
        .toInt()
}

private fun Travel.statusVisuals(
    currentDate: LocalDate,
    currentDay: Int,
    totalDayCount: Int,
): TravelStatusVisuals =
    when (status) {
        TravelStatus.ACTIVE ->
            TravelStatusVisuals(
                text = "여행 중 · DAY $currentDay/$totalDayCount",
                style = YadanStatusChipStyle.LIVE,
            )

        TravelStatus.UPCOMING -> {
            val remainingDays =
                startDate?.let { start ->
                    start.toEpochDays() - currentDate.toEpochDays()
                }

            val dDayText =
                when {
                    remainingDays == null -> "여행 예정"
                    remainingDays > 0L -> "D-$remainingDays"
                    remainingDays == 0L -> "D-DAY"
                    else -> "여행 예정"
                }

            TravelStatusVisuals(
                text = "$dDayText · ${region.displayName} 원정",
                style = YadanStatusChipStyle.PRIMARY,
            )
        }

        TravelStatus.COMPLETED ->
            TravelStatusVisuals(
                text = "여행 완료",
                style = YadanStatusChipStyle.MUTED,
            )
    }

private fun Travel.dateText(): String? {
    val start = startDate ?: return null
    val end = endDate

    return if (end == null || start == end) {
        start.toShortDateText()
    } else {
        "${start.toShortDateText()}~${end.toShortDateText()}"
    }
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
    val user =
        UserProfile(
            id = "user-1",
            provider = LoginProvider.KAKAO,
            providerUserId = "kakao-user-1",
            nickname = "준호",
        )

    val game =
        BaseballGame(
            id = "game-1",
            stadium =
                BaseballStadium(
                    id = "stadium-1",
                    name = "사직야구장",
                    region = Region.BUSAN,
                    latitude = 35.194,
                    longitude = 129.061,
                ),
            homeTeam = KboTeam.LOTTE,
            awayTeam = KboTeam.KIA,
            gameDateTime = LocalDateTime(2026, 5, 23, 17, 0),
            gameType = BaseballGameType.REGULAR,
        )

    val certifiedPlace =
        TravelPlace(
            id = "place-1",
            spot =
                TravelSpot(
                    id = "spot-1",
                    name = "감천문화마을",
                    latitude = 35.097,
                    longitude = 129.010,
                    region = Region.BUSAN,
                    category = TravelSpotCategory.CULTURE,
                ),
            day = 1,
            order = 1,
            certifications =
                listOf(
                    TravelCertification(
                        id = "certification-1",
                        userId = user.id,
                        certificatedAt = LocalDateTime(2026, 5, 22, 11, 0),
                    ),
                ),
        )

    val waitingPlace =
        TravelPlace(
            id = "place-2",
            spot =
                TravelSpot(
                    id = "spot-2",
                    name = "사직야구장",
                    latitude = 35.194,
                    longitude = 129.061,
                    region = Region.BUSAN,
                    category = TravelSpotCategory.STADIUM,
                ),
            day = 1,
            order = 2,
        )

    val activeTravel =
        Travel(
            id = "travel-1",
            name = "부산 원정 · 사직 직관 여행",
            baseballGame = game,
            region = Region.BUSAN,
            startDate = LocalDate(2026, 5, 22),
            endDate = LocalDate(2026, 5, 23),
            participants =
                listOf(
                    TravelParticipant(
                        user = user,
                        isLeader = true,
                    ),
                ),
            days =
                listOf(
                    TravelDay(
                        day = 1,
                        places = listOf(
                            certifiedPlace,
                            waitingPlace,
                        ),
                    ),
                ),
            status = TravelStatus.ACTIVE,
        )

    val upcomingTravel =
        activeTravel.copy(
            id = "travel-2",
            name = "주말 부산 야구 여행",
            startDate = LocalDate(2026, 5, 23),
            endDate = LocalDate(2026, 5, 24),
            participants =
                listOf(
                    TravelParticipant(
                        user = user,
                        isLeader = false,
                    ),
                ),
            status = TravelStatus.UPCOMING,
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
                currentUserId = user.id,
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
                currentUserId = user.id,
                currentDate = LocalDate(2026, 5, 20),
                onClick = {},
            )
        }
    }
}
