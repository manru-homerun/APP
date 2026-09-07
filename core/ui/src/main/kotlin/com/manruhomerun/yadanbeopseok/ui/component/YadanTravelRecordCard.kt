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
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSummary
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

/**
 * 종료된 여행 목록에서 사용하는 여행 기록 카드입니다.
 *
 * D01 여행 기록 화면의 여행 카드 구조에 대응합니다.
 * 여행명, 여행 날짜, 관광지 수와 스티커 획득 여부를 표시합니다.
 *
 * @param travel 완료된 여행의 목록 요약 정보입니다.
 * @param onClick 카드를 선택했을 때 실행할 작업입니다.
 * @param modifier 카드의 크기와 배치를 지정합니다.
 * @param thumbnailImageUrl 카드에 표시할 대표 이미지 URL입니다.
 * @param enabled 카드 선택 가능 여부입니다.
 */
@Composable
fun YadanTravelRecordCard(
    travel: TravelSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnailImageUrl: String? = null,
    enabled: Boolean = true,
) {
    val travelName = travel.recordDisplayName()
    val metadata = "${travel.recordDateText()} · 관광지 ${travel.spotsCount}곳"

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.42f),
        enabled = enabled,
        shape = YadanShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = YadanOnPrimary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = YadanOnPrimary,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp,
            pressedElevation = 2.dp,
            disabledElevation = 0.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(YadanPrimaryGradient)
                .padding(
                    horizontal = 15.dp,
                    vertical = 14.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
                    style = YadanTypography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    color = YadanOnPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = metadata,
                    style = YadanTypography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = YadanOnPrimary.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                YadanStatusChip(
                    text = if (travel.hasSticker) {
                        "스티커 획득"
                    } else {
                        "스티커 미획득"
                    },
                    style = if (travel.hasSticker) {
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
 * 여행 이름이 비어 있으면 지역명을 이용해 기본 이름을 만듭니다.
 */
private fun TravelSummary.recordDisplayName(): String {
    return name.takeIf { it.isNotBlank() }
        ?: "${region.displayName} 원정 여행"
}

/**
 * 여행 기록 카드에 표시할 날짜 범위를 만듭니다.
 *
 * 예: 2026.4.18~4.19
 */
private fun TravelSummary.recordDateText(): String =
    when {
        startDate == endDate -> {
            startDate.toFullDateText()
        }

        startDate.year == endDate.year -> {
            "${startDate.toFullDateText()}~${endDate.month.number}.${endDate.day}"
        }

        else -> {
            "${startDate.toFullDateText()}~${endDate.toFullDateText()}"
        }
    }

/**
 * 연도를 포함한 날짜 문구를 만듭니다.
 */
private fun LocalDate.toFullDateText(): String =
    "$year.${month.number}.$day"

@Preview(
    name = "D01 여행 기록 카드",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
)
@Composable
private fun YadanTravelRecordCardPreview() {
    val acquiredTravel = previewTravelSummary()

    YadanbeopseokTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(YadanBackground)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            YadanTravelRecordCard(
                travel = acquiredTravel,
                onClick = {},
            )

            YadanTravelRecordCard(
                travel = acquiredTravel.copy(
                    id = "235",
                    name = "대구 라이온즈파크 여행",
                    startDate = LocalDate(2026, 4, 4),
                    endDate = LocalDate(2026, 4, 5),
                    baseballGameId = "124",
                    homeTeam = KboTeam.SAMSUNG,
                    awayTeam = KboTeam.KIA,
                    region = Region.DAEGU,
                    spotsCount = 3,
                    certificationTargetCount = 3,
                    certifiedSpotsCount = 2,
                    hasSticker = false,
                ),
                onClick = {},
            )
        }
    }
}

private fun previewTravelSummary(): TravelSummary =
    TravelSummary(
        id = "234",
        name = "부산 사직 직관 여행",
        startDate = LocalDate(2026, 4, 18),
        endDate = LocalDate(2026, 4, 19),
        baseballGameId = "123",
        homeTeam = KboTeam.LOTTE,
        awayTeam = KboTeam.KIA,
        region = Region.BUSAN,
        isLeader = true,
        spotsCount = 4,
        certificationTargetCount = 4,
        certifiedSpotsCount = 4,
        hasSticker = true,
    )
