package com.manruhomerun.yadanbeopseok.travel.share.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanDivider
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOutline
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPillShape
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryGradient
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTint
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.BaseballGame
import com.manruhomerun.yadanbeopseok.model.BaseballGameType
import com.manruhomerun.yadanbeopseok.model.BaseballStadium
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.Travel
import com.manruhomerun.yadanbeopseok.model.TravelBaseballGame
import com.manruhomerun.yadanbeopseok.model.TravelDay
import com.manruhomerun.yadanbeopseok.model.TravelPlace
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import com.manruhomerun.yadanbeopseok.model.TravelStatus
import com.manruhomerun.yadanbeopseok.travel.util.toDisplayDay
import com.manruhomerun.yadanbeopseok.travel.util.toTravelDateRangeText
import com.manruhomerun.yadanbeopseok.travel.util.toTravelDayDateText
import com.manruhomerun.yadanbeopseok.ui.component.displayTitle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * C03 여행 일정 이미지 미리보기와 저장 작업을 표시합니다.
 *
 * [posterModifier]는 다음 단계에서 포스터 전체를 이미지로 캡처할 때 사용합니다.
 * 화면에 보이는 일부가 아니라 스크롤 영역의 전체 포스터에 적용됩니다.
 */
@Composable
fun TravelShareScreen(
    travel: Travel,
    baseballGame: BaseballGame,
    onBackClick: () -> Unit,
    onSaveImageClick: () -> Unit,
    modifier: Modifier = Modifier,
    posterModifier: Modifier = Modifier,
    isSaving: Boolean = false,
) {
    val scrollState = rememberScrollState()

    val displayDays = remember(travel, baseballGame) {
        travel.days
            .sortedBy { day -> day.day }
            .map { day ->
                day.toDisplayDay(
                    baseballGame = travel.baseballGame,
                    game = baseballGame,
                )
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YadanBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        YadanTopAppBar(
            title = "이미지로 공유",
            onNavigationClick = {
                if (!isSaving) {
                    onBackClick()
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 20.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TravelShareGuide()

            PreviewDivider()

            TravelSharePoster(
                travel = travel,
                baseballGame = baseballGame,
                displayDays = displayDays,
                modifier = posterModifier,
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = YadanSurface,
            shadowElevation = 8.dp,
        ) {
            YadanButton(
                text = "이미지 저장",
                onClick = onSaveImageClick,
                modifier = Modifier.padding(
                    start = 20.dp,
                    top = 12.dp,
                    end = 20.dp,
                    bottom = 20.dp,
                ),
                isLoading = isSaving,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

/**
 * 이미지 공유 화면의 사용 목적을 안내합니다.
 */
@Composable
private fun TravelShareGuide() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = YadanShapes.medium,
        color = YadanPrimaryTint,
        border = BorderStroke(
            width = 1.dp,
            color = YadanPrimary.copy(alpha = 0.45f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 11.dp,
            ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = YadanPrimaryInk,
            )

            Spacer(modifier = Modifier.width(7.dp))

            Text(
                text = "앱이 없는 친구에게 이미지 한 장으로 보내요",
                style = YadanTypography.labelSmall,
                color = YadanPrimaryInk,
            )
        }
    }
}

/**
 * 포스터 미리보기 영역의 구분선을 표시합니다.
 */
@Composable
private fun PreviewDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = YadanDivider,
        )

        Text(
            text = "미리보기",
            modifier = Modifier.padding(horizontal = 10.dp),
            style = YadanTypography.labelSmall,
            color = YadanTextMuted,
        )

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = YadanDivider,
        )
    }
}

/**
 * 실제 이미지로 저장할 전체 여행 일정 포스터입니다.
 */
@Composable
private fun TravelSharePoster(
    travel: Travel,
    baseballGame: BaseballGame,
    displayDays: List<TravelDay>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        shape = YadanShapes.medium,
        color = YadanSurface,
        shadowElevation = 8.dp,
    ) {
        Column {
            TravelSharePosterHeader(
                title = travel.displayTitle(),
                homeTeam = baseballGame.homeTeam,
                awayTeam = baseballGame.awayTeam,
                dateText = travel.startDate.toTravelDateRangeText(travel.endDate),
                durationText = travel.durationText(),
            )

            Column(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 18.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                displayDays.forEach { day ->
                    TravelShareDaySection(
                        day = day,
                        dateText = travel.startDate.toTravelDayDateText(day.day),
                    )
                }
            }

            TravelSharePosterFooter()
        }
    }
}

/**
 * 포스터 상단에 여행 이름, 대진과 여행 기간을 표시합니다.
 */
@Composable
private fun TravelSharePosterHeader(
    title: String,
    homeTeam: KboTeam,
    awayTeam: KboTeam,
    dateText: String,
    durationText: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(YadanPrimaryGradient)
            .padding(16.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lineWidth = 15.dp.toPx()

            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = size.height * 0.43f,
                center = Offset(
                    x = size.width * 0.87f,
                    y = size.height * 0.42f,
                ),
                style = Stroke(width = lineWidth),
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = size.height * 0.30f,
                center = Offset(
                    x = size.width * 1.03f,
                    y = size.height * 0.70f,
                ),
                style = Stroke(width = lineWidth),
            )
        }

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "야단법석",
                    modifier = Modifier.weight(1f),
                    style = YadanTypography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    color = YadanOnPrimary,
                )

                Surface(
                    shape = YadanPillShape,
                    color = YadanOnPrimary.copy(alpha = 0.18f),
                ) {
                    Text(
                        text = "직관 여행 티켓",
                        modifier = Modifier.padding(
                            horizontal = 9.dp,
                            vertical = 5.dp,
                        ),
                        style = YadanTypography.labelSmall,
                        color = YadanOnPrimary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = title,
                style = YadanTypography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
                color = YadanOnPrimary,
            )

            Spacer(modifier = Modifier.height(13.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = YadanShapes.small,
                    color = YadanOnPrimary.copy(alpha = 0.14f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = YadanOnPrimary.copy(alpha = 0.38f),
                    ),
                ) {
                    Text(
                        text = "${homeTeam.displayName}  VS  ${awayTeam.displayName}",
                        modifier = Modifier.padding(
                            horizontal = 9.dp,
                            vertical = 6.dp,
                        ),
                        style = YadanTypography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = YadanOnPrimary,
                    )
                }

                Spacer(modifier = Modifier.width(9.dp))

                Text(
                    text = "$dateText · $durationText",
                    modifier = Modifier.weight(1f),
                    style = YadanTypography.labelSmall,
                    color = YadanOnPrimary,
                )
            }
        }
    }
}

/**
 * 포스터에 한 일차의 날짜와 전체 일정을 표시합니다.
 */
@Composable
private fun TravelShareDaySection(
    day: TravelDay,
    dateText: String,
) {
    val orderedPlaces = day.places.sortedBy { place -> place.order }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = YadanShapes.small,
                color = YadanPrimary,
            ) {
                Text(
                    text = "DAY ${day.day}",
                    modifier = Modifier.padding(
                        horizontal = 9.dp,
                        vertical = 4.dp,
                    ),
                    style = YadanTypography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    color = YadanOnPrimary,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = dateText,
                style = YadanTypography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = YadanTextMuted,
            )
        }

        if (orderedPlaces.isEmpty()) {
            Text(
                text = "등록된 일정이 없습니다.",
                modifier = Modifier.padding(start = 4.dp),
                style = YadanTypography.bodySmall,
                color = YadanTextMuted,
            )
        } else {
            var placeNumber = 0

            orderedPlaces.forEach { place ->
                val isBaseballGame = place.spot.category == TravelSpotCategory.STADIUM

                if (!isBaseballGame) {
                    placeNumber += 1
                }

                TravelShareTimelineRow(
                    place = place,
                    placeNumber = placeNumber,
                    isBaseballGame = isBaseballGame,
                )
            }
        }
    }
}

/**
 * 포스터 일정에서 관광지 또는 야구 경기 한 항목을 표시합니다.
 */
@Composable
private fun TravelShareTimelineRow(
    place: TravelPlace,
    placeNumber: Int,
    isBaseballGame: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(21.dp)
                .background(
                    color = if (isBaseballGame) {
                        YadanTextPrimary
                    } else {
                        YadanPrimaryTint
                    },
                    shape = YadanPillShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isBaseballGame) {
                Icon(
                    imageVector = Icons.Default.SportsBaseball,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = YadanOnPrimary,
                )
            } else {
                Text(
                    text = placeNumber.toString(),
                    style = YadanTypography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = YadanPrimaryInk,
                )
            }
        }

        Spacer(modifier = Modifier.width(9.dp))

        Text(
            text = place.spot.name,
            modifier = Modifier.weight(1f),
            style = YadanTypography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = YadanTextPrimary,
        )

        if (isBaseballGame) {
            Spacer(modifier = Modifier.width(7.dp))

            Surface(
                shape = YadanPillShape,
                color = YadanTextPrimary,
            ) {
                Text(
                    text = "직관",
                    modifier = Modifier.padding(
                        horizontal = 7.dp,
                        vertical = 3.dp,
                    ),
                    style = YadanTypography.labelSmall,
                    color = YadanOnPrimary,
                )
            }
        }
    }
}

/**
 * 이미지 하단에 서비스 출처를 표시합니다.
 */
@Composable
private fun TravelSharePosterFooter() {
    Column {
        HorizontalDivider(color = YadanOutline)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 13.dp,
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.SportsBaseball,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = YadanPrimary,
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "야단법석에서 함께 직관 여행을 떠나요",
                style = YadanTypography.labelSmall,
                color = YadanTextMuted,
            )
        }
    }
}

/**
 * 여행 시작일과 종료일로 당일 또는 숙박 기간 문구를 계산합니다.
 */
private fun Travel.durationText(): String {
    val nights = (endDate.toEpochDays() - startDate.toEpochDays()).coerceAtLeast(0L)

    return if (nights == 0L) {
        "당일"
    } else {
        "${nights}박 ${nights + 1}일"
    }
}

@Preview(
    name = "C03 - 이미지 공유",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelShareScreenPreview() {
    YadanbeopseokTheme {
        TravelShareScreen(
            travel = previewTravel(),
            baseballGame = previewBaseballGame(),
            onBackClick = {},
            onSaveImageClick = {},
        )
    }
}

@Preview(
    name = "C03 - 이미지 저장 중",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelShareSavingPreview() {
    YadanbeopseokTheme {
        TravelShareScreen(
            travel = previewTravel(),
            baseballGame = previewBaseballGame(),
            onBackClick = {},
            onSaveImageClick = {},
            isSaving = true,
        )
    }
}

private fun previewTravel(): Travel = Travel(
    id = "travel-1",
    startDate = LocalDate(2026, 5, 22),
    endDate = LocalDate(2026, 5, 23),
    baseballGame = TravelBaseballGame(
        id = "game-1",
        day = 2,
        baseballGameAfterIdx = 2,
    ),
    name = "부산 사직 직관 여행",
    region = Region.BUSAN,
    friends = emptyList(),
    isLeader = true,
    themeIds = emptyList(),
    certificationTargetCount = 4,
    certifiedSpotsCount = 0,
    days = listOf(
        TravelDay(
            day = 1,
            places = listOf(
                previewPlace(
                    id = "food-1",
                    name = "돼지국밥 거리",
                    category = TravelSpotCategory.FOOD,
                    order = 1,
                ),
                previewPlace(
                    id = "culture-1",
                    name = "감천문화마을",
                    category = TravelSpotCategory.CULTURE,
                    order = 2,
                ),
                previewPlace(
                    id = "stay-1",
                    name = "스테이 광안",
                    category = TravelSpotCategory.ACCOMMODATION,
                    order = 3,
                ),
            ),
        ),
        TravelDay(
            day = 2,
            places = listOf(
                previewPlace(
                    id = "nature-1",
                    name = "광안리 해변",
                    category = TravelSpotCategory.NATURE,
                    order = 1,
                ),
                previewPlace(
                    id = "cafe-1",
                    name = "전포 카페거리",
                    category = TravelSpotCategory.FOOD,
                    order = 2,
                ),
            ),
        ),
    ),
    status = TravelStatus.UPCOMING,
)

private fun previewBaseballGame(): BaseballGame = BaseballGame(
    id = "game-1",
    stadium = BaseballStadium(
        id = "stadium-1",
        name = "사직야구장",
        region = Region.BUSAN,
        latitude = 35.1940,
        longitude = 129.0616,
    ),
    homeTeam = KboTeam.LOTTE,
    awayTeam = KboTeam.KIA,
    gameDateTime = LocalDateTime(2026, 5, 23, 17, 0),
    gameType = BaseballGameType.REGULAR,
)

private fun previewPlace(
    id: String,
    name: String,
    category: TravelSpotCategory,
    order: Int,
): TravelPlace = TravelPlace(
    spot = TravelSpot(
        id = id,
        name = name,
        region = Region.BUSAN,
        category = category,
    ),
    order = order,
)
