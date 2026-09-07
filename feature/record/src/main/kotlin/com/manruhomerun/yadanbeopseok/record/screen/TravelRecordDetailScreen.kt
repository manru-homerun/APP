package com.manruhomerun.yadanbeopseok.record.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCard
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanSectionHeader
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTint
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.BaseballGame
import com.manruhomerun.yadanbeopseok.model.BaseballGameType
import com.manruhomerun.yadanbeopseok.model.BaseballStadium
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.Sticker
import com.manruhomerun.yadanbeopseok.model.StickerPack
import com.manruhomerun.yadanbeopseok.model.Travel
import com.manruhomerun.yadanbeopseok.model.TravelBaseballGame
import com.manruhomerun.yadanbeopseok.model.TravelDay
import com.manruhomerun.yadanbeopseok.model.TravelPlace
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import com.manruhomerun.yadanbeopseok.model.TravelStatus
import com.manruhomerun.yadanbeopseok.record.viewmodel.TravelRecordDetailUiState
import com.manruhomerun.yadanbeopseok.ui.component.YadanStickerSize
import com.manruhomerun.yadanbeopseok.ui.component.YadanStickerView
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelHeader
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate

/**
 * D·01b 지난 여행 상세 화면입니다.
 *
 * 완료된 여행 일정과 획득한 스티커를 표시합니다.
 * 실제 화면 이동과 데이터 조회는 Route에 위임합니다.
 */
@Composable
fun TravelRecordDetailScreen(
    uiState: TravelRecordDetailUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onStickerRetryClick: () -> Unit,
    onDecoratePhotoClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YadanBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        YadanTopAppBar(
            title = "여행 기록",
            onNavigationClick = onBackClick,
        )

        when {
            uiState.isLoading -> {
                TravelRecordDetailLoadingContent(
                    modifier = Modifier.weight(1f),
                )
            }

            uiState.hasTravelDetail -> {
                TravelRecordDetailContent(
                    uiState = uiState,
                    onStickerRetryClick = onStickerRetryClick,
                    modifier = Modifier.weight(1f),
                )
            }

            else -> {
                TravelRecordDetailErrorContent(
                    message = uiState.errorMessage
                        ?: "지난 여행 정보를 불러오지 못했습니다.",
                    onRetryClick = onRetryClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (uiState.canDecoratePhoto && onDecoratePhotoClick != null) {
            YadanButton(
                text = "스티커로 사진 꾸미기",
                onClick = onDecoratePhotoClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 20.dp,
                        top = 8.dp,
                        end = 20.dp,
                        bottom = 20.dp,
                    ),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
        }
    }
}

/**
 * 여행 정보, 지난 일정과 스티커 영역을 표시합니다.
 */
@Composable
private fun TravelRecordDetailContent(
    uiState: TravelRecordDetailUiState,
    onStickerRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val travel = checkNotNull(uiState.travel)
    val baseballGame = checkNotNull(uiState.baseballGame)

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 8.dp,
            end = 20.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item(key = "travel-record-header") {
            YadanTravelHeader(
                travel = travel,
                dateText = travel.toRecordDateRangeText(),
                trailingContent = {
                    TravelRecordStickerStatusBadge(
                        uiState = uiState,
                    )
                },
            )
        }

        item(key = "travel-record-schedule-header") {
            YadanSectionHeader(title = "다녀온 일정")
        }

        item(key = "travel-record-schedule") {
            TravelRecordScheduleCard(
                travel = travel,
                baseballGame = baseballGame,
            )
        }

        item(key = "travel-record-sticker-header") {
            YadanSectionHeader(title = "받은 스티커")
        }

        item(key = "travel-record-stickers") {
            TravelRecordStickerCard(
                uiState = uiState,
                onRetryClick = onStickerRetryClick,
            )
        }
    }
}

/**
 * 스티커 조회 상태에 맞는 배지를 표시합니다.
 */
@Composable
private fun TravelRecordStickerStatusBadge(
    uiState: TravelRecordDetailUiState,
) {
    val statusText = when {
        uiState.isStickerLoading -> "스티커 확인 중"
        uiState.hasSticker -> "스티커 획득"
        uiState.stickerErrorMessage != null -> "스티커 확인 필요"
        else -> "스티커 미획득"
    }

    Surface(
        shape = CircleShape,
        color = YadanOnPrimary.copy(alpha = 0.18f),
        contentColor = YadanOnPrimary,
        border = BorderStroke(
            width = 1.dp,
            color = YadanOnPrimary.copy(alpha = 0.30f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 9.dp,
                vertical = 4.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = statusText,
                style = YadanTypography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
                maxLines = 1,
            )
        }
    }
}

/**
 * 일차별 관광지와 야구 경기를 하나의 지난 일정 카드로 표시합니다.
 */
@Composable
private fun TravelRecordScheduleCard(
    travel: Travel,
    baseballGame: BaseballGame,
    modifier: Modifier = Modifier,
) {
    val travelDays = travel.days.sortedBy { travelDay ->
        travelDay.day
    }

    YadanCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            if (travelDays.isEmpty()) {
                Text(
                    text = "저장된 여행 일정이 없습니다.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    style = YadanTypography.bodyMedium,
                    color = YadanTextSecondary,
                    textAlign = TextAlign.Center,
                )
                return@Column
            }

            travelDays.forEachIndexed { dayIndex, travelDay ->
                if (dayIndex > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = "DAY ${travelDay.day} · ${travel.toDayDateText(travelDay.day)}",
                    style = YadanTypography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    color = YadanPrimaryInk,
                )

                Spacer(modifier = Modifier.height(12.dp))

                val timelineItems = travelDay.toRecordTimelineItems(
                    travelBaseballGame = travel.baseballGame,
                    baseballGame = baseballGame,
                )

                timelineItems.forEachIndexed { index, item ->
                    TravelRecordTimelineRow(
                        item = item,
                        isLast = index == timelineItems.lastIndex,
                    )
                }
            }
        }
    }
}

/**
 * 지난 일정의 관광지 또는 야구 경기 한 항목을 표시합니다.
 */
@Composable
private fun TravelRecordTimelineRow(
    item: TravelRecordTimelineItem,
    isLast: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.width(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(
                        color = if (item.isBaseballGame) {
                            YadanTextPrimary
                        } else {
                            YadanPrimary
                        },
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (item.isBaseballGame) {
                        Icons.Default.SportsBaseball
                    } else {
                        Icons.Default.Check
                    },
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = YadanOnPrimary,
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(30.dp)
                        .background(YadanPrimaryTint),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = 10.dp,
                    bottom = if (isLast) 0.dp else 16.dp,
                ),
        ) {
            Text(
                text = item.name,
                style = YadanTypography.bodyMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
                color = YadanTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = item.supportingText,
                modifier = Modifier.padding(top = 2.dp),
                style = YadanTypography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = YadanTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * 스티커 조회 상태에 따라 로딩, 오류, 미획득 또는 획득 목록을 표시합니다.
 */
@Composable
private fun TravelRecordStickerCard(
    uiState: TravelRecordDetailUiState,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    YadanCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        when {
            uiState.isStickerLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = YadanPrimary,
                        strokeWidth = 3.dp,
                    )
                }
            }

            uiState.stickerErrorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = uiState.stickerErrorMessage,
                        style = YadanTypography.bodySmall,
                        color = YadanTextSecondary,
                        textAlign = TextAlign.Center,
                    )

                    TextButton(
                        onClick = onRetryClick,
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Text(text = "다시 시도")
                    }
                }
            }

            uiState.hasSticker -> {
                val stickerPack = checkNotNull(uiState.stickerPack)

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 16.dp,
                        alignment = Alignment.CenterHorizontally,
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    stickerPack.stickers.forEachIndexed { index, sticker ->
                        YadanStickerView(
                            sticker = sticker,
                            contentDescription = "${stickerPack.name} ${index + 1}번 스티커",
                            size = YadanStickerSize.DEFAULT,
                        )
                    }
                }
            }

            else -> {
                Text(
                    text = "이 여행에서 획득한 스티커가 없습니다.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 32.dp,
                        ),
                    style = YadanTypography.bodyMedium,
                    color = YadanTextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * 여행과 경기 배치 정보를 지난 일정 화면용 항목으로 변환합니다.
 */
private fun TravelDay.toRecordTimelineItems(
    travelBaseballGame: TravelBaseballGame,
    baseballGame: BaseballGame,
): List<TravelRecordTimelineItem> {
    val timelineItems = places
        .sortedBy { place -> place.order }
        .mapTo(mutableListOf()) { place ->
            TravelRecordTimelineItem(
                name = place.spot.name,
                supportingText = place.spot.category.displayName,
                isBaseballGame = false,
            )
        }

    if (travelBaseballGame.day == day) {
        val gameIndex = travelBaseballGame.baseballGameAfterIdx
            .coerceIn(0, timelineItems.size)

        timelineItems.add(
            index = gameIndex,
            element = TravelRecordTimelineItem(
                name = baseballGame.stadium.name,
                supportingText = "${baseballGame.toTimeText()} · 직관",
                isBaseballGame = true,
            ),
        )
    }

    return timelineItems
}

private data class TravelRecordTimelineItem(
    val name: String,
    val supportingText: String,
    val isBaseballGame: Boolean,
)

private fun Travel.toRecordDateRangeText(): String {
    val startText = "${startDate.year}.${startDate.month.number}.${startDate.day}"

    if (startDate == endDate) {
        return startText
    }

    val endText = if (startDate.year == endDate.year) {
        "${endDate.month.number}.${endDate.day}"
    } else {
        "${endDate.year}.${endDate.month.number}.${endDate.day}"
    }

    return "$startText – $endText"
}

private fun Travel.toDayDateText(day: Int): String {
    val dayOffset = (day - 1).coerceAtLeast(0)
    val date = startDate.plus(dayOffset, DateTimeUnit.DAY)

    return date.toJavaLocalDate().format(recordDayDateFormatter)
}

private fun BaseballGame.toTimeText(): String {
    val hourText = gameDateTime.hour.toString().padStart(2, '0')
    val minuteText = gameDateTime.minute.toString().padStart(2, '0')

    return "$hourText:$minuteText"
}

private val recordDayDateFormatter =
    DateTimeFormatter.ofPattern("M.d (E)", Locale.KOREAN)

/**
 * 여행 상세 조회 중 표시합니다.
 */
@Composable
private fun TravelRecordDetailLoadingContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = YadanPrimary)
    }
}

/**
 * 여행 또는 경기 상세 조회 실패 시 재시도를 제공합니다.
 */
@Composable
private fun TravelRecordDetailErrorContent(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "지난 여행을 확인할 수 없습니다",
            style = YadanTypography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = YadanTextPrimary,
            textAlign = TextAlign.Center,
        )

        Text(
            text = message,
            modifier = Modifier.padding(top = 8.dp),
            style = YadanTypography.bodySmall,
            color = YadanTextSecondary,
            textAlign = TextAlign.Center,
        )

        YadanButton(
            text = "다시 시도",
            onClick = onRetryClick,
            modifier = Modifier
                .padding(top = 20.dp)
                .widthIn(min = 120.dp),
        )
    }
}

@Preview(
    name = "D01b 지난 여행 상세 - 스티커 획득",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelRecordDetailScreenPreview() {
    YadanbeopseokTheme {
        TravelRecordDetailScreen(
            uiState = travelRecordDetailPreviewState(hasSticker = true),
            onBackClick = {},
            onRetryClick = {},
            onStickerRetryClick = {},
            onDecoratePhotoClick = {},
        )
    }
}

@Preview(
    name = "D01b 지난 여행 상세 - 스티커 미획득",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelRecordDetailWithoutStickerPreview() {
    YadanbeopseokTheme {
        TravelRecordDetailScreen(
            uiState = travelRecordDetailPreviewState(hasSticker = false),
            onBackClick = {},
            onRetryClick = {},
            onStickerRetryClick = {},
            onDecoratePhotoClick = null,
        )
    }
}

@Preview(
    name = "D01b 지난 여행 상세 - 로딩",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelRecordDetailLoadingPreview() {
    YadanbeopseokTheme {
        TravelRecordDetailScreen(
            uiState = TravelRecordDetailUiState(),
            onBackClick = {},
            onRetryClick = {},
            onStickerRetryClick = {},
            onDecoratePhotoClick = null,
        )
    }
}

@Preview(
    name = "D01b 지난 여행 상세 - 오류",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelRecordDetailErrorPreview() {
    YadanbeopseokTheme {
        TravelRecordDetailScreen(
            uiState = TravelRecordDetailUiState(
                isLoading = false,
                errorMessage = "인터넷 연결을 확인한 후 다시 시도해주세요.",
            ),
            onBackClick = {},
            onRetryClick = {},
            onStickerRetryClick = {},
            onDecoratePhotoClick = null,
        )
    }
}

private fun travelRecordDetailPreviewState(
    hasSticker: Boolean,
): TravelRecordDetailUiState {
    val travel = Travel(
        id = "travel-1",
        startDate = LocalDate(2026, 4, 12),
        endDate = LocalDate(2026, 4, 13),
        baseballGame = TravelBaseballGame(
            id = "game-1",
            day = 1,
            baseballGameAfterIdx = 2,
        ),
        name = "부산 사직 직관 여행",
        region = Region.BUSAN,
        friends = listOf("한별"),
        isLeader = true,
        themeIds = emptyList(),
        certificationTargetCount = 3,
        certifiedSpotsCount = if (hasSticker) 3 else 2,
        days = previewTravelDays(),
        status = TravelStatus.COMPLETED,
    )

    val baseballGame = BaseballGame(
        id = "game-1",
        stadium = BaseballStadium(
            id = "stadium-1",
            name = "사직야구장",
            region = Region.BUSAN,
            latitude = 35.1940,
            longitude = 129.0615,
        ),
        homeTeam = KboTeam.LOTTE,
        awayTeam = KboTeam.KIA,
        gameDateTime = LocalDateTime(2026, 4, 12, 17, 0),
        gameType = BaseballGameType.REGULAR,
        homeTeamScore = 4,
        awayTeamScore = 2,
    )

    val stickerPack = if (hasSticker) {
        StickerPack(
            id = "pack-busan",
            name = "사직 한정 스티커팩",
            stickers = listOf(
                Sticker(
                    id = "sticker-sajik",
                    stickerPackId = "pack-busan",
                    imageUrl = "",
                ),
                Sticker(
                    id = "sticker-gamcheon",
                    stickerPackId = "pack-busan",
                    imageUrl = "",
                ),
            ),
        )
    } else {
        null
    }

    return TravelRecordDetailUiState(
        travel = travel,
        baseballGame = baseballGame,
        stickerPack = stickerPack,
        isLoading = false,
    )
}

private fun previewTravelDays(): List<TravelDay> {
    return listOf(
        TravelDay(
            day = 1,
            places = listOf(
                previewTravelPlace(
                    id = "spot-1",
                    name = "돼지국밥 거리",
                    category = TravelSpotCategory.FOOD,
                    order = 1,
                ),
                previewTravelPlace(
                    id = "spot-2",
                    name = "감천문화마을",
                    category = TravelSpotCategory.CULTURE,
                    order = 2,
                ),
            ),
        ),
        TravelDay(
            day = 2,
            places = listOf(
                previewTravelPlace(
                    id = "spot-3",
                    name = "광안리 해변",
                    category = TravelSpotCategory.NATURE,
                    order = 1,
                ),
            ),
        ),
    )
}

private fun previewTravelPlace(
    id: String,
    name: String,
    category: TravelSpotCategory,
    order: Int,
): TravelPlace {
    return TravelPlace(
        spot = TravelSpot(
            id = id,
            name = name,
            region = Region.BUSAN,
            category = category,
        ),
        order = order,
        isCertificationTarget = true,
        isCertified = true,
    )
}
