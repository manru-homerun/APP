package com.manruhomerun.yadanbeopseok.home.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanFilterChip
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanIconButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanMainHeader
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanMainHeaderStyle
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanPageIndicator
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanSectionCountBadge
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanSectionHeader
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanError
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTint
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.home.viewmodel.HomeUiState
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import com.manruhomerun.yadanbeopseok.model.TravelSummary
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelCard
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelSpotCard
import kotlinx.datetime.LocalDate

/**
 * 홈 화면의 UI를 구성합니다.
 *
 * 여행 개수와 상태에 따라 여행 중, 여행 예정, 동행 참여,
 * 여행 없음 화면을 하나의 상태 기반 화면으로 표현합니다.
 */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    currentDate: LocalDate,
    onNotificationClick: () -> Unit,
    onTravelClick: (String) -> Unit,
    onGameScheduleClick: () -> Unit,
    onRegionSelected: (Region) -> Unit,
    onCategorySelected: (TravelSpotCategory) -> Unit,
    onRefreshClick: () -> Unit,
    onTravelSpotClick: (String) -> Unit,
    onDibsClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayedTravels = uiState.displayedTravels

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(YadanBackground),
    ) {
        HomeHeader(
            hasUnreadNotifications = uiState.hasUnreadNotifications,
            onNotificationClick = onNotificationClick,
        )

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                YadanSectionHeader(
                    title = "내 원정 여행",
                    modifier =
                        Modifier.padding(
                            start = 20.dp,
                            top = 4.dp,
                            end = 20.dp,
                            bottom = 10.dp,
                        ),
                    trailingContent =
                        if (!uiState.isLoading && displayedTravels.isNotEmpty()) {
                            {
                                YadanSectionCountBadge(
                                    count = displayedTravels.size,
                                )
                            }
                        } else {
                            null
                        },
                )
            }

            item {
                HomeTravelContent(
                    travels = displayedTravels,
                    currentDate = currentDate,
                    isLoading = uiState.isLoading,
                    onTravelClick = onTravelClick,
                    onGameScheduleClick = onGameScheduleClick,
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                HomeRecommendationHeader(
                    selectedRegion = uiState.selectedRegion,
                    isRefreshing = uiState.isRefreshing,
                    onRegionSelected = onRegionSelected,
                    onRefreshClick = onRefreshClick,
                )
            }

            item {
                HomeCategoryFilters(
                    selectedCategory = uiState.selectedCategory,
                    enabled = !uiState.isRefreshing,
                    onCategorySelected = onCategorySelected,
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            when {
                uiState.isLoading && uiState.popularTravelSpots.isEmpty() -> {
                    item {
                        HomeLoadingContent()
                    }
                }

                uiState.popularTravelSpots.isEmpty() -> {
                    item {
                        HomeEmptySpotContent()
                    }
                }

                else -> {
                    items(
                        items = uiState.popularTravelSpots,
                        key = { spot -> spot.id },
                    ) { spot ->
                        YadanTravelSpotCard(
                            spot = spot,
                            onClick = {
                                onTravelSpotClick(spot.id)
                            },
                            onActionClick = {
                                onDibsClick(spot.id)
                            },
                            modifier =
                                Modifier.padding(
                                    start = 20.dp,
                                    end = 20.dp,
                                    bottom = 10.dp,
                                ),
                            enabled = spot.id !in uiState.updatingDibsSpotIds,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 서비스명과 알림 버튼을 표시합니다.
 */
@Composable
private fun HomeHeader(
    hasUnreadNotifications: Boolean,
    onNotificationClick: () -> Unit,
) {
    YadanMainHeader(
        title = "야단법석",
        style = YadanMainHeaderStyle.BRAND,
        modifier = Modifier.statusBarsPadding(),
        trailingContent = {
            Box {
                YadanIconButton(
                    onClick = onNotificationClick,
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription =
                            if (hasUnreadNotifications) {
                                "알림, 읽지 않은 알림 있음"
                            } else {
                                "알림"
                            },
                    )
                }

                if (hasUnreadNotifications) {
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.TopEnd)
                                .padding(
                                    top = 6.dp,
                                    end = 6.dp,
                                )
                                .size(7.dp)
                                .background(
                                    color = YadanError,
                                    shape = YadanShapes.extraLarge,
                                ),
                    )
                }
            }
        },
    )
}

/**
 * 여행 개수에 맞게 로딩, Pager, 단일 카드, 빈 상태를 표시합니다.
 */
@Composable
private fun HomeTravelContent(
    travels: List<TravelSummary>,
    currentDate: LocalDate,
    isLoading: Boolean,
    onTravelClick: (String) -> Unit,
    onGameScheduleClick: () -> Unit,
) {
    when {
        isLoading && travels.isEmpty() -> {
            HomeTravelLoadingContent()
        }

        travels.size > 1 -> {
            HomeTravelPager(
                travels = travels,
                currentDate = currentDate,
                onTravelClick = onTravelClick,
            )
        }

        travels.size == 1 -> {
            val travel = travels.first()

            YadanTravelCard(
                travel = travel,
                currentDate = currentDate,
                onClick = {
                    onTravelClick(travel.id)
                },
                modifier =
                    Modifier.padding(
                        horizontal = 20.dp,
                        vertical = 6.dp,
                    ),
            )
        }

        else -> {
            HomeEmptyTravelCard(
                onGameScheduleClick = onGameScheduleClick,
                modifier =
                    Modifier.padding(
                        horizontal = 20.dp,
                        vertical = 6.dp,
                    ),
            )
        }
    }
}

/**
 * 두 개 이상의 여행을 다음 카드가 보이는 가로 Pager로 표시합니다.
 */
@Composable
private fun HomeTravelPager(
    travels: List<TravelSummary>,
    currentDate: LocalDate,
    onTravelClick: (String) -> Unit,
) {
    val pagerState =
        rememberPagerState(
            pageCount = travels::size,
        )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(248.dp),
            contentPadding =
                PaddingValues(
                    start = 20.dp,
                    end = 48.dp,
                ),
            pageSpacing = 10.dp,
            key = { page ->
                travels[page].id
            },
        ) { page ->
            val travel = travels[page]

            YadanTravelCard(
                travel = travel,
                currentDate = currentDate,
                onClick = {
                    onTravelClick(travel.id)
                },
                modifier = Modifier.padding(vertical = 6.dp),
            )
        }

        YadanPageIndicator(
            pageCount = travels.size,
            currentPage = pagerState.currentPage,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

/**
 * 등록된 여행이 없을 때 경기 일정 화면 진입을 안내합니다.
 *
 * 점선 테두리는 공통 카드에 없는 홈 화면 전용 표현이므로
 * 기존 색상과 모서리 토큰만 재사용해 구성합니다.
 */
@Composable
private fun HomeEmptyTravelCard(
    onGameScheduleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color = YadanPrimaryTint,
                    shape = YadanShapes.large,
                ),
    ) {
        Canvas(
            modifier = Modifier.matchParentSize(),
        ) {
            val strokeWidth = 1.dp.toPx()

            drawRoundRect(
                color = YadanPrimary.copy(alpha = 0.7f),
                topLeft =
                    Offset(
                        x = strokeWidth / 2f,
                        y = strokeWidth / 2f,
                    ),
                size =
                    Size(
                        width = size.width - strokeWidth,
                        height = size.height - strokeWidth,
                    ),
                cornerRadius =
                    CornerRadius(
                        x = 18.dp.toPx(),
                        y = 18.dp.toPx(),
                    ),
                style =
                    Stroke(
                        width = strokeWidth,
                        pathEffect =
                            PathEffect.dashPathEffect(
                                intervals =
                                    floatArrayOf(
                                        5.dp.toPx(),
                                        4.dp.toPx(),
                                    ),
                            ),
                    ),
            )
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                shape = YadanShapes.medium,
                color = YadanSurface,
                shadowElevation = 3.dp,
            ) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Stadium,
                        contentDescription = null,
                        modifier = Modifier.size(27.dp),
                        tint = YadanPrimary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "아직 떠난 원정이 없어요",
                style =
                    YadanTypography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                color = YadanTextPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "응원 팀 경기를 고르면 구장 주변 코스를 짜드려요",
                style = YadanTypography.bodySmall,
                color = YadanTextMuted,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(14.dp))

            YadanButton(
                text = "경기 일정에서 시작",
                onClick = onGameScheduleClick,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

/**
 * 추천 지역 선택과 새로고침 작업을 표시합니다.
 */
@Composable
private fun HomeRecommendationHeader(
    selectedRegion: Region,
    isRefreshing: Boolean,
    onRegionSelected: (Region) -> Unit,
    onRefreshClick: () -> Unit,
) {
    var expanded by rememberSaveable {
        mutableStateOf(false)
    }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            TextButton(
                onClick = {
                    expanded = true
                },
                contentPadding = PaddingValues(horizontal = 0.dp),
                colors =
                    ButtonDefaults.textButtonColors(
                        contentColor = YadanTextPrimary,
                    ),
            ) {
                Text(
                    text = selectedRegion.displayName,
                    style =
                        YadanTypography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                        ),
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                },
                modifier = Modifier.widthIn(min = 140.dp),
                shape = YadanShapes.medium,
                containerColor = YadanSurface,
            ) {
                Region.entries.forEach { region ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = region.displayName,
                                style = YadanTypography.bodyMedium,
                            )
                        },
                        onClick = {
                            expanded = false
                            onRegionSelected(region)
                        },
                        trailingIcon =
                            if (region == selectedRegion) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = YadanPrimary,
                                    )
                                }
                            } else {
                                null
                            },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = onRefreshClick,
            enabled = !isRefreshing,
            contentPadding = PaddingValues(horizontal = 4.dp),
            colors =
                ButtonDefaults.textButtonColors(
                    contentColor = YadanPrimary,
                ),
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = YadanPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
            }

            Spacer(modifier = Modifier.size(4.dp))

            Text(
                text = "새로고침",
                style =
                    YadanTypography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
            )
        }
    }
}

/**
 * 홈에서 제공하는 관광지 카테고리를 가로 필터로 표시합니다.
 */
@Composable
private fun HomeCategoryFilters(
    selectedCategory: TravelSpotCategory,
    enabled: Boolean,
    onCategorySelected: (TravelSpotCategory) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        items(
            items = HOME_TRAVEL_SPOT_CATEGORIES,
            key = { category -> category.name },
        ) { category ->
            YadanFilterChip(
                text = category.displayName,
                selected = category == selectedCategory,
                onClick = {
                    onCategorySelected(category)
                },
                enabled = enabled,
            )
        }
    }
}

@Composable
private fun HomeTravelLoadingContent() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(236.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = YadanPrimary,
        )
    }
}

@Composable
private fun HomeLoadingContent() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(120.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = YadanPrimary,
        )
    }
}

@Composable
private fun HomeEmptySpotContent() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "조건에 맞는 추천 여행지가 없습니다.",
            style = YadanTypography.bodyMedium,
            color = YadanTextMuted,
            textAlign = TextAlign.Center,
        )
    }
}

/*
 * 홈에서는 API가 지원하는 관광 카테고리만 노출합니다.
 * 야구장과 알 수 없는 카테고리는 필터에서 제외합니다.
 */
private val HOME_TRAVEL_SPOT_CATEGORIES =
    listOf(
        TravelSpotCategory.ACCOMMODATION,
        TravelSpotCategory.FESTIVAL,
        TravelSpotCategory.EXPERIENCE,
        TravelSpotCategory.FOOD,
        TravelSpotCategory.HISTORY,
        TravelSpotCategory.LEISURE,
        TravelSpotCategory.NATURE,
        TravelSpotCategory.SHOPPING,
        TravelSpotCategory.CULTURE,
    )

private const val PREVIEW_USER_ID = "preview-user"

private class HomeUiStatePreviewProvider :
    PreviewParameterProvider<HomeUiState> {
    private val activeTravel =
        previewTravel(
            id = "travel-active",
            name = "부산 사직 직관 여행",
            isLeader = true,
            startDate = LocalDate(2026, 5, 20),
            endDate = LocalDate(2026, 5, 21),
            certifiedSpotsCount = 1,
        )

    private val upcomingTravel =
        previewTravel(
            id = "travel-upcoming",
            name = "주말 부산 야구 여행",
            isLeader = true,
            startDate = LocalDate(2026, 5, 23),
            endDate = LocalDate(2026, 5, 24),
        )

    private val guestTravel =
        previewTravel(
            id = "travel-guest",
            name = "부산 사직 직관 여행",
            isLeader = false,
            startDate = LocalDate(2026, 5, 23),
            endDate = LocalDate(2026, 5, 24),
        )

    private val secondUpcomingTravel =
        previewTravel(
            id = "travel-gwangju",
            name = "광주 야구 원정 여행",
            isLeader = true,
            region = Region.GWANGJU,
            startDate = LocalDate(2026, 6, 6),
            endDate = LocalDate(2026, 6, 7),
        )

    override val values: Sequence<HomeUiState> =
        sequenceOf(
            HomeUiState(
                currentUserId = PREVIEW_USER_ID,
                travels =
                    listOf(
                        activeTravel,
                        upcomingTravel,
                        secondUpcomingTravel,
                    ),
                popularTravelSpots = previewPopularSpots(),
                hasUnreadNotifications = true,
                isLoading = false,
            ),
            HomeUiState(
                currentUserId = PREVIEW_USER_ID,
                travels =
                    listOf(
                        upcomingTravel,
                        secondUpcomingTravel,
                    ),
                popularTravelSpots = previewPopularSpots(),
                isLoading = false,
            ),
            HomeUiState(
                currentUserId = PREVIEW_USER_ID,
                travels = listOf(guestTravel),
                popularTravelSpots = previewPopularSpots(),
                isLoading = false,
            ),
            HomeUiState(
                currentUserId = PREVIEW_USER_ID,
                popularTravelSpots = previewPopularSpots(),
                isLoading = false,
            ),
        )
}

private fun previewTravel(
    id: String,
    name: String,
    isLeader: Boolean,
    region: Region = Region.BUSAN,
    startDate: LocalDate,
    endDate: LocalDate,
    certifiedSpotsCount: Int = 0,
): TravelSummary {
    val homeTeam =
        if (region == Region.GWANGJU) {
            KboTeam.KIA
        } else {
            KboTeam.LOTTE
        }

    val awayTeam =
        if (homeTeam == KboTeam.LOTTE) {
            KboTeam.KIA
        } else {
            KboTeam.LOTTE
        }

    return TravelSummary(
        id = id,
        name = name,
        startDate = startDate,
        endDate = endDate,
        baseballGameId = "game-$id",
        homeTeam = homeTeam,
        awayTeam = awayTeam,
        region = region,
        isLeader = isLeader,
        spotsCount = 6,
        certificationTargetCount = 5,
        certifiedSpotsCount = certifiedSpotsCount,
        hasSticker = false,
    )
}

private fun previewPopularSpots(): List<TravelSpot> =
    listOf(
        TravelSpot(
            id = "spot-1",
            name = "스테이 광안",
            region = Region.BUSAN,
            category = TravelSpotCategory.ACCOMMODATION,
        ),
        TravelSpot(
            id = "spot-2",
            name = "해운대 오션뷰 호텔",
            region = Region.BUSAN,
            category = TravelSpotCategory.ACCOMMODATION,
        ),
        TravelSpot(
            id = "spot-3",
            name = "송정 게스트하우스",
            region = Region.BUSAN,
            category = TravelSpotCategory.ACCOMMODATION,
        ),
    )

@Preview(
    name = "Home states",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun HomeScreenPreview(
    @PreviewParameter(HomeUiStatePreviewProvider::class)
    uiState: HomeUiState,
) {
    YadanbeopseokTheme {
        HomeScreen(
            uiState = uiState,
            currentDate = LocalDate(2026, 5, 20),
            onNotificationClick = {},
            onTravelClick = {},
            onGameScheduleClick = {},
            onRegionSelected = {},
            onCategorySelected = {},
            onRefreshClick = {},
            onTravelSpotClick = {},
            onDibsClick = {},
        )
    }
}
