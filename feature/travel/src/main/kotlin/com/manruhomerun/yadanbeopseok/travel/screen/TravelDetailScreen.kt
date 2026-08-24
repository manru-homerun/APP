package com.manruhomerun.yadanbeopseok.travel.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.Travel
import com.manruhomerun.yadanbeopseok.model.TravelBaseballGame
import com.manruhomerun.yadanbeopseok.model.TravelDay
import com.manruhomerun.yadanbeopseok.model.TravelPlace
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import com.manruhomerun.yadanbeopseok.model.TravelStatus
import com.manruhomerun.yadanbeopseok.travel.viewmodel.TravelDetailUiState
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelDaySection
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelDaySelector
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelHeader
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelPlaceItemMode
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelProgress

import kotlinx.datetime.LocalDate

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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YadanBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        YadanTopAppBar(
            title = "여행 일정",
            onNavigationClick = onBackClick,
        )

        val travel = uiState.travel

        when {
            uiState.isLoading -> {
                TravelDetailLoadingContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }

            travel != null -> {
                TravelDetailContent(
                    travel = travel,
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
    }
}

/**
 * 조회한 여행 상세 정보를 일차별 일정으로 표시합니다.
 */
@Composable
private fun TravelDetailContent(
    travel: Travel,
    dayNumbers: List<Int>,
    selectedDay: Int?,
    selectedTravelDay: TravelDay?,
    onDaySelected: (Int) -> Unit,
    onVerifyClick: ((TravelPlace) -> Unit)?,
    onRenameClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val placeItemMode = travel.status.toPlaceItemMode()

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 8.dp,
            end = 20.dp,
            bottom = 32.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            YadanTravelHeader(
                travel = travel,
                dateText = travel.startDate.toTravelDateRangeText(travel.endDate),
                onRenameClick = onRenameClick,
            )
        }

        if (travel.status != TravelStatus.UPCOMING) {
            item {
                YadanTravelProgress(
                    certifiedPlaceCount = travel.certifiedSpotsCount,
                    totalPlaceCount = travel.certificationTargetCount,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (dayNumbers.isNotEmpty() && selectedDay != null) {
            item {
                YadanTravelDaySelector(
                    dayNumbers = dayNumbers,
                    selectedDay = selectedDay,
                    onDaySelected = onDaySelected,
                )
            }
        }

        if (selectedTravelDay != null) {
            item(key = selectedTravelDay.day) {
                YadanTravelDaySection(
                    travelDay = selectedTravelDay,
                    dateText = travel.startDate.toTravelDayDateText(selectedTravelDay.day),
                    mode = placeItemMode,
                    onVerifyClick =
                        if (travel.status == TravelStatus.ACTIVE) {
                            onVerifyClick
                        } else {
                            null
                        },
                )
            }
        } else {
            item {
                TravelDetailEmptyScheduleContent()
            }
        }
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
 * 여행에 아직 등록된 일정이 없을 때 표시합니다.
 */
@Composable
private fun TravelDetailEmptyScheduleContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 44.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "등록된 여행 일정이 없습니다",
            style = YadanTypography.bodyMedium,
            color = YadanTextSecondary,
            textAlign = TextAlign.Center,
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
    name = "Travel detail - Active",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelDetailScreenPreview() {
    YadanbeopseokTheme {
        TravelDetailScreen(
            uiState = TravelDetailUiState(
                travel = previewTravel(),
                selectedDay = 1,
                isLoading = false,
            ),
            onBackClick = {},
            onDaySelected = {},
            onVerifyClick = {},
            onRetryClick = {},
            onRenameClick = {},
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

private fun previewTravel(): Travel =
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
        isLeader = true,
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
                        id = "spot-2",
                        name = "사직야구장",
                        category = TravelSpotCategory.STADIUM,
                        order = 2,
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
        status = TravelStatus.ACTIVE,
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
