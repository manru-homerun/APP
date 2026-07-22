package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Stadium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanStatusChip
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanStatusChipSize
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanStatusChipStyle
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryGradient
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.BaseballGame
import com.manruhomerun.yadanbeopseok.model.BaseballGameType
import com.manruhomerun.yadanbeopseok.model.BaseballStadium
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.Travel
import com.manruhomerun.yadanbeopseok.model.TravelDay
import com.manruhomerun.yadanbeopseok.model.TravelPlace
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import com.manruhomerun.yadanbeopseok.model.TravelStatus
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number

/**
 * 종료된 여행 목록에서 사용하는 여행 기록 카드입니다.
 *
 * D01 여행 기록 화면의 `.rectrip` 구조에 대응합니다.
 * 여행명, 여행 날짜, 관광지 수와 스티커 획득 여부를 표시합니다.
 *
 * 그라데이션이 필요한 카드이므로 Material3 Card의 클릭과 Ripple 처리를 사용하고,
 * 이미지와 상태 표시는 기존 [YadanAsyncImage], [YadanStatusChip]을 재사용합니다.
 *
 * @param travel 표시할 완료된 여행입니다.
 * @param stickerAcquired 해당 여행에서 스티커를 획득했는지 나타냅니다.
 * @param onClick 카드를 선택했을 때 실행할 작업입니다.
 * @param modifier 카드의 크기와 배치를 지정합니다.
 * @param thumbnailImageUrl 카드에 표시할 대표 이미지 URL입니다.
 * @param enabled 카드 선택 가능 여부입니다.
 */
@Composable
fun YadanTravelRecordCard(
    travel: Travel,
    stickerAcquired: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnailImageUrl: String? = travel.thumbnailImageUrl(),
    enabled: Boolean = true,
) {
    val travelName = travel.recordDisplayName()
    val metadata =
        "${travel.recordDateText()} · " +
            "관광지 ${travel.touristPlaceCount()}곳"

    Card(
        onClick = onClick,
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
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = Color.Transparent,
                contentColor = YadanOnPrimary,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = YadanOnPrimary,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 5.dp,
                pressedElevation = 2.dp,
                disabledElevation = 0.dp,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanPrimaryGradient)
                    .padding(
                        horizontal = 15.dp,
                        vertical = 14.dp,
                    ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            /*
             * 여행명이 카드에 함께 표시되므로 대표 이미지는 장식 요소로 처리해
             * TalkBack에서 여행 정보가 중복 안내되지 않게 합니다.
             */
            YadanAsyncImage(
                imageUrl = thumbnailImageUrl,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                shape = YadanShapes.medium,
                placeholderIcon = Icons.Outlined.Stadium,
            )

            Spacer(modifier = Modifier.width(13.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = travelName,
                    style =
                        YadanTypography.bodyMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                        ),
                    color = YadanOnPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = metadata,
                    style =
                        YadanTypography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    color = YadanOnPrimary.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                /*
                 * 획득 상태는 흰색 배경, 미획득 상태는 반투명 배경을 사용합니다.
                 * 기존 StatusChip의 어두운 배경용 시각 스타일을 재사용합니다.
                 */
                YadanStatusChip(
                    text =
                        if (stickerAcquired) {
                            "스티커 획득"
                        } else {
                            "스티커 미획득"
                        },
                    style =
                        if (stickerAcquired) {
                            YadanStatusChipStyle.HOST
                        } else {
                            YadanStatusChipStyle.GUEST
                        },
                    size = YadanStatusChipSize.SMALL,
                    onDark = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                        )
                    },
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(23.dp),
                tint = YadanOnPrimary.copy(alpha = 0.70f),
            )
        }
    }
}

/**
 * 여행 이름이 없을 때 지역명을 이용해 기본 이름을 만듭니다.
 */
private fun Travel.recordDisplayName(): String =
    name
        ?.takeIf { it.isNotBlank() }
        ?: "${region.displayName} 원정 여행"

/**
 * 여행 기록 카드에 표시할 날짜 범위를 만듭니다.
 *
 * 같은 연도와 월은 중복 표기하지 않습니다.
 * 예: 2026.4.18~4.19
 */
private fun Travel.recordDateText(): String {
    val start = startDate ?: return "날짜 미정"
    val end = endDate

    return when {
        end == null || start == end ->
            start.toFullDateText()

        start.year == end.year &&
            start.month == end.month ->
            "${start.toFullDateText()}~${end.day}"

        start.year == end.year ->
            "${start.toFullDateText()}~${end.month.number}.${end.day}"

        else ->
            "${start.toFullDateText()}~${end.toFullDateText()}"
    }
}

/**
 * 숙박을 제외한 여행 일정의 방문 장소 개수를 계산합니다.
 *
 * HTML의 여행 기록 수치는 야구장을 방문 장소에 포함하므로
 * 경기장은 제외하지 않습니다.
 */
private fun Travel.touristPlaceCount(): Int =
    days.sumOf { day ->
        day.places.count { place ->
            place.spot.category != TravelSpotCategory.ACCOMMODATION
        }
    }

/**
 * 여행 일정 순서상 가장 먼저 확인되는 이미지 URL을 대표 이미지로 사용합니다.
 */
private fun Travel.thumbnailImageUrl(): String? =
    days
        .sortedBy { it.day }
        .asSequence()
        .flatMap { day ->
            day.places
                .sortedBy { it.order }
                .asSequence()
        }.firstNotNullOfOrNull { place ->
            place.spot.imageUrl?.takeIf { it.isNotBlank() }
        }

/**
 * 여행 기록에서 사용하는 연도 포함 날짜 형식입니다.
 */
private fun LocalDate.toFullDateText(): String =
    "$year.${month.number}.$day"

@Preview(
    name = "Yadan travel record cards",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
)
@Composable
private fun YadanTravelRecordCardPreview() {
    val busanTravel = previewCompletedTravel()

    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            YadanTravelRecordCard(
                travel = busanTravel,
                stickerAcquired = true,
                onClick = {},
            )

            YadanTravelRecordCard(
                travel =
                    busanTravel.copy(
                        id = "travel-daegu",
                        name = "대구 라이온즈파크 여행",
                        region = Region.DAEGU,
                        startDate = LocalDate(2026, 4, 4),
                        endDate = LocalDate(2026, 4, 5),
                        days =
                            busanTravel.days.map { day ->
                                day.copy(
                                    places = day.places.take(3),
                                )
                            },
                    ),
                stickerAcquired = false,
                onClick = {},
            )
        }
    }
}

/**
 * Preview에서 완료 여행 카드의 실제 구성을 확인하기 위한 여행 모델입니다.
 */
private fun previewCompletedTravel(): Travel {
    val region = Region.BUSAN
    val stadium =
        BaseballStadium(
            id = "stadium-sajik",
            name = "사직야구장",
            region = region,
            latitude = 35.194,
            longitude = 129.061,
        )
    val game =
        BaseballGame(
            id = "game-lotte-kia",
            stadium = stadium,
            homeTeam = KboTeam.LOTTE,
            awayTeam = KboTeam.KIA,
            gameDateTime = LocalDateTime(2026, 4, 18, 17, 0),
            gameType = BaseballGameType.REGULAR,
        )
    val placeNames =
        listOf(
            "감천문화마을",
            "광안리 해변",
            "돼지국밥 거리",
            "자갈치시장",
        )
    val places =
        placeNames.mapIndexed { index, name ->
            TravelPlace(
                id = "travel-place-$index",
                spot =
                    TravelSpot(
                        id = "spot-$index",
                        name = name,
                        latitude = 35.1 + index * 0.01,
                        longitude = 129.0 + index * 0.01,
                        region = region,
                        category = TravelSpotCategory.CULTURE,
                    ),
                day = 1,
                order = index + 1,
            )
        }

    return Travel(
        id = "travel-busan",
        name = "부산 사직 직관 여행",
        baseballGame = game,
        region = region,
        startDate = LocalDate(2026, 4, 18),
        endDate = LocalDate(2026, 4, 19),
        participants = emptyList(),
        days =
            listOf(
                TravelDay(
                    day = 1,
                    places = places,
                ),
            ),
        status = TravelStatus.COMPLETED,
    )
}
