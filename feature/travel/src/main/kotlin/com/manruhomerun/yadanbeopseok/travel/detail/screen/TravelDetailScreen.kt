package com.manruhomerun.yadanbeopseok.travel.detail.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButtonStyle
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOutline
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTint
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
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
import com.manruhomerun.yadanbeopseok.travel.component.TravelScheduleContent
import com.manruhomerun.yadanbeopseok.travel.detail.viewmodel.TravelDetailUiState
import com.manruhomerun.yadanbeopseok.travel.util.toDisplayDay
import com.manruhomerun.yadanbeopseok.travel.util.toTravelDateRangeText
import com.manruhomerun.yadanbeopseok.travel.util.toTravelDayDateText
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelDaySelector
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelPlaceItemMode
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelProgress
import com.manruhomerun.yadanbeopseok.ui.component.displayTitle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * 선택한 여행의 상세 일정과 방문 인증 상태를 표시합니다.
 */
@Composable
fun TravelDetailScreen(
    uiState: TravelDetailUiState,
    onBackClick: () -> Unit,
    onDaySelected: (Int) -> Unit,
    onVerifyClick: ((TravelPlace) -> Unit)? = null,
    onRetryClick: () -> Unit,
    onRenameClick: (() -> Unit)? = null,
    onEditScheduleClick: (() -> Unit)? = null,
    onShareImageClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val travel = uiState.travel

    val screenTitle = when (travel?.status) {
        TravelStatus.UPCOMING -> "여행 상세"
        else -> "여행 일정"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YadanBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        YadanTopAppBar(
            title = screenTitle,
            onNavigationClick = onBackClick,
        )

        val baseballGame = uiState.baseballGame

        when {
            uiState.isLoading -> {
                TravelDetailLoadingContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }

            travel != null && baseballGame != null -> {
                TravelDetailContent(
                    travel = travel,
                    baseballGame = baseballGame,
                    dayNumbers = uiState.dayNumbers,
                    selectedDay = uiState.selectedDay,
                    selectedTravelDay = uiState.selectedTravelDay,
                    onDaySelected = onDaySelected,
                    onVerifyClick = onVerifyClick,
                    onRenameClick = onRenameClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }

            else -> {
                TravelDetailErrorContent(
                    message = uiState.errorMessage
                        ?: "여행 정보를 불러오지 못했습니다.",
                    onRetryClick = onRetryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }

        if (
            travel?.status == TravelStatus.UPCOMING &&
            travel.isLeader &&
            (onEditScheduleClick != null || onShareImageClick != null)
        ) {
            TravelDetailEditBottomBar(
                onEditScheduleClick = onEditScheduleClick,
                onShareImageClick = onShareImageClick,
            )
        }
    }
}

/**
 * 여행 전에는 전체 일정을 표시하고,
 * 여행 중에는 선택된 일차와 방문 인증 상태를 표시합니다.
 */
@Composable
private fun TravelDetailContent(
    travel: Travel,
    baseballGame: BaseballGame,
    dayNumbers: List<Int>,
    selectedDay: Int?,
    selectedTravelDay: TravelDay?,
    onDaySelected: (Int) -> Unit,
    onVerifyClick: ((TravelPlace) -> Unit)?,
    onRenameClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val isUpcoming = travel.status == TravelStatus.UPCOMING

    val visibleTravelDays = remember(travel, baseballGame, selectedTravelDay) {
        val sourceTravelDays = if (isUpcoming) {
            travel.days
        } else {
            listOfNotNull(selectedTravelDay)
        }

        sourceTravelDays.map { travelDay ->
            travelDay.toDisplayDay(
                baseballGame = travel.baseballGame,
                game = baseballGame,
            )
        }
    }

    val renameAction = if (isUpcoming && travel.isLeader) {
        onRenameClick
    } else {
        null
    }

    TravelScheduleContent(
        title = travel.displayTitle(),
        dateText = travel.startDate.toTravelDateRangeText(travel.endDate),
        isLeader = travel.isLeader,
        visibleTravelDays = visibleTravelDays,
        dateTextForDay = { day -> travel.startDate.toTravelDayDateText(day) },
        modifier = modifier,
        sectionTitle = null,
        onRenameClick = renameAction,

        /*
         * 공통 일정 본문의 헤더와 일차 선택기 사이 슬롯을 재사용합니다.
         * 여행 중에는 인증 진행률을 표시하고,
         * 여행 전 동행자 화면에서는 안내 카드를 표시합니다.
         */
        progressContent = when {
            !isUpcoming -> {
                {
                    YadanTravelProgress(
                        certifiedPlaceCount = travel.certifiedSpotsCount,
                        totalPlaceCount = travel.certificationTargetCount,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            !travel.isLeader -> {
                {
                    TravelGuestNotice()
                }
            }

            else -> {
                null
            }
        },

        daySelectorContent = if (
            dayNumbers.isNotEmpty() &&
            selectedDay != null
        ) {
            {
                YadanTravelDaySelector(
                    dayNumbers = dayNumbers,
                    selectedDay = selectedDay,
                    onDaySelected = onDaySelected,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            null
        },

        selectedDay = selectedDay,
        scrollToSelectedDay = isUpcoming,
        onVisibleDayChanged = if (isUpcoming) {
            onDaySelected
        } else {
            null
        },

        placeItemMode = travel.status.toPlaceItemMode(),
        onVerifyClick = if (travel.status == TravelStatus.ACTIVE) {
            onVerifyClick
        } else {
            null
        },
    )
}

/**
 * 여행 전 동행자 상세 화면에서 방장 권한과 방문 인증 참여를 안내합니다.
 */
@Composable
private fun TravelGuestNotice(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = YadanShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = YadanSurface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = YadanOutline,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
        ),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 12.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = YadanPrimaryTint,
                        shape = YadanShapes.small,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = YadanPrimaryInk,
                )
            }

            Text(
                text = "동행자도 방문 인증에 함께 참여해요. 단, 일정 편집은 방장만 할 수 있어요.",
                style = YadanTypography.bodySmall,
                color = YadanTextSecondary,
            )
        }
    }
}

/**
 * 여행 전 방장에게 일정 편집과 이미지 공유 작업을 제공합니다.
 */
@Composable
private fun TravelDetailEditBottomBar(
    onEditScheduleClick: (() -> Unit)?,
    onShareImageClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(YadanSurface)
            .padding(
                start = 20.dp,
                top = 12.dp,
                end = 20.dp,
                bottom = 20.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        YadanButton(
            text = "일정 편집",
            onClick = {
                onEditScheduleClick?.invoke()
            },
            modifier = Modifier.weight(1f),
            style = YadanButtonStyle.GHOST,
            enabled = onEditScheduleClick != null,
        )

        YadanButton(
            text = "이미지로 공유",
            onClick = {
                onShareImageClick?.invoke()
            },
            modifier = Modifier.weight(1.35f),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                )
            },
            enabled = onShareImageClick != null,
        )
    }
}

/**
 * 여행 정보를 불러오는 동안 표시합니다.
 */
@Composable
private fun TravelDetailLoadingContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(30.dp),
            color = YadanPrimary,
            strokeWidth = 3.dp,
        )
    }
}

/**
 * 여행 상세 조회에 실패했을 때 재시도 작업을 제공합니다.
 */
@Composable
private fun TravelDetailErrorContent(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "여행 일정을 확인할 수 없습니다",
            style = YadanTypography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
            ),
            color = YadanTextPrimary,
            textAlign = TextAlign.Center,
        )

        Text(
            text = message,
            modifier = Modifier.padding(top = 8.dp),
            style = YadanTypography.bodyMedium,
            color = YadanTextSecondary,
            textAlign = TextAlign.Center,
        )

        YadanButton(
            text = "다시 시도",
            onClick = onRetryClick,
            modifier = Modifier
                .padding(top = 20.dp)
                .widthIn(min = 148.dp),
        )
    }
}

/**
 * 여행 상태에 맞는 일정 항목 표시 방식을 반환합니다.
 */
private fun TravelStatus.toPlaceItemMode(): YadanTravelPlaceItemMode =
    when (this) {
        TravelStatus.UPCOMING -> YadanTravelPlaceItemMode.VIEW
        TravelStatus.ACTIVE -> YadanTravelPlaceItemMode.ACTIVE
        TravelStatus.COMPLETED -> YadanTravelPlaceItemMode.COMPLETED
    }

@Preview(
    name = "Travel detail - Upcoming leader",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelDetailUpcomingLeaderPreview() {
    TravelDetailStatePreviewContent(
        status = TravelStatus.UPCOMING,
        isLeader = true,
    )
}

@Preview(
    name = "Travel detail - Upcoming companion",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelDetailUpcomingCompanionPreview() {
    TravelDetailStatePreviewContent(
        status = TravelStatus.UPCOMING,
        isLeader = false,
    )
}

@Preview(
    name = "Travel detail - Active",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelDetailActivePreview() {
    TravelDetailStatePreviewContent(
        status = TravelStatus.ACTIVE,
    )
}

/**
 * 여행 진행 상태와 방장 여부에 따른 상세 화면을 Preview에 제공합니다.
 */
@Composable
private fun TravelDetailStatePreviewContent(
    status: TravelStatus,
    isLeader: Boolean = true,
) {
    YadanbeopseokTheme {
        TravelDetailScreen(
            uiState = TravelDetailUiState(
                travel = previewTravel(
                    status = status,
                    isLeader = isLeader,
                ),
                baseballGame = previewBaseballGame(),
                selectedDay = 1,
                isLoading = false,
            ),
            onBackClick = {},
            onDaySelected = {},
            onVerifyClick = {},
            onRetryClick = {},
            onRenameClick = null,
            onEditScheduleClick = {},
            onShareImageClick = {},
        )
    }
}

@Preview(
    name = "Travel detail - Loading",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelDetailLoadingPreview() {
    YadanbeopseokTheme {
        TravelDetailScreen(
            uiState = TravelDetailUiState(),
            onBackClick = {},
            onDaySelected = {},
            onVerifyClick = {},
            onRetryClick = {},
        )
    }
}

@Preview(
    name = "Travel detail - Error",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelDetailErrorPreview() {
    YadanbeopseokTheme {
        TravelDetailScreen(
            uiState = TravelDetailUiState(
                isLoading = false,
                errorMessage = "인터넷 연결을 확인한 후 다시 시도해주세요.",
            ),
            onBackClick = {},
            onDaySelected = {},
            onVerifyClick = {},
            onRetryClick = {},
        )
    }
}

private fun previewTravel(
    status: TravelStatus,
    isLeader: Boolean,
): Travel =
    Travel(
        id = "travel-123",
        startDate = LocalDate(2026, 5, 22),
        endDate = LocalDate(2026, 5, 23),
        baseballGame = TravelBaseballGame(
            id = "123",
            day = 1,
            baseballGameAfterIdx = 2,
        ),
        name = "부산 사직 직관 여행",
        region = Region.BUSAN,
        friends = listOf("야구팬", "원정러"),
        isLeader = isLeader,
        themeIds = listOf("1", "3"),
        certificationTargetCount = 3,
        certifiedSpotsCount = 1,
        days = listOf(
            TravelDay(
                day = 1,
                places = listOf(
                    previewPlace(
                        id = "spot-1",
                        name = "감천문화마을",
                        category = TravelSpotCategory.CULTURE,
                        order = 1,
                        isCertificationTarget = true,
                        isCertified = true,
                    ),
                    previewPlace(
                        id = "spot-3",
                        name = "광안리 해수욕장",
                        category = TravelSpotCategory.NATURE,
                        order = 3,
                        isCertificationTarget = true,
                    ),
                ),
            ),
            TravelDay(
                day = 2,
                places = listOf(
                    previewPlace(
                        id = "spot-4",
                        name = "해운대 오션뷰 호텔",
                        category = TravelSpotCategory.ACCOMMODATION,
                        order = 1,
                    ),
                    previewPlace(
                        id = "spot-5",
                        name = "부평깡통시장",
                        category = TravelSpotCategory.FOOD,
                        order = 2,
                        isCertificationTarget = true,
                    ),
                ),
            ),
        ),
        status = status,
    )

private fun previewPlace(
    id: String,
    name: String,
    category: TravelSpotCategory,
    order: Int,
    isCertificationTarget: Boolean = false,
    isCertified: Boolean = false,
): TravelPlace =
    TravelPlace(
        spot = TravelSpot(
            id = id,
            name = name,
            region = Region.BUSAN,
            category = category,
        ),
        order = order,
        isCertificationTarget = isCertificationTarget,
        isCertified = isCertified,
    )

private fun previewBaseballGame(): BaseballGame =
    BaseballGame(
        id = "123",
        stadium = BaseballStadium(
            id = "preview-stadium",
            name = "사직야구장",
            region = Region.BUSAN,
            latitude = 35.194,
            longitude = 129.061,
        ),
        homeTeam = KboTeam.LOTTE,
        awayTeam = KboTeam.KIA,
        gameDateTime = LocalDateTime(2026, 5, 22, 17, 0),
        gameType = BaseballGameType.REGULAR,
    )
