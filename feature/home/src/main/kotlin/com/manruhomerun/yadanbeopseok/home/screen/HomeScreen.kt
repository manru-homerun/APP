package com.manruhomerun.yadanbeopseok.home.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanBottomNavigation
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanBottomNavigationCenterAction
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanBottomNavigationItem
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanIconButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanMainHeader
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanMainHeaderStyle
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanPageIndicator
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanSectionCountBadge
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
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
import com.manruhomerun.yadanbeopseok.model.TravelSpotFilterCategory
import com.manruhomerun.yadanbeopseok.model.TravelSummary
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelCard
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelCardDefaults
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelRegionDropdown
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelSpotCard
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelSpotCategoryFilters
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
    onCategorySelected: (TravelSpotFilterCategory?) -> Unit,
    onRefreshClick: () -> Unit,
    onTravelRetryClick: () -> Unit,
    onPopularSpotsRefreshClick: () -> Unit,
    onTravelSpotClick: (String) -> Unit,
    onDibsClick: (String) -> Unit,
    initialTravelPage: Int = 0,
    modifier: Modifier = Modifier,
) {
    val displayedTravels = uiState.displayedTravels
    val isHomeLoading = uiState.isLoading || uiState.isRefreshing
    val isTravelRetryEnabled = !isHomeLoading && !uiState.isTravelListLoading
    val isPopularRefreshEnabled = !isHomeLoading && !uiState.isPopularSpotLoading

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YadanBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        HomeHeader(
            onNotificationClick = onNotificationClick,
        )

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefreshClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item {
                    FlowRow(
                        modifier = Modifier.padding(
                            start = 20.dp,
                            top = 4.dp,
                            end = 20.dp,
                            bottom = 10.dp,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        itemVerticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "내 원정 여행",
                            style = YadanTypography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                            ),
                            color = YadanTextPrimary,
                        )

                        if (displayedTravels.isNotEmpty()) {
                            YadanSectionCountBadge(count = displayedTravels.size)
                        }
                    }
                }

                item {
                    HomeTravelContent(
                        travels = displayedTravels,
                        currentDate = currentDate,
                        isLoading = uiState.isTravelListLoading,
                        hasLoadedTravelList = uiState.hasLoadedTravelList,
                        errorMessage = uiState.travelErrorMessage,
                        onRetryClick = onTravelRetryClick,
                        retryEnabled = isTravelRetryEnabled,
                        onTravelClick = onTravelClick,
                        onGameScheduleClick = onGameScheduleClick,
                        initialPage = initialTravelPage,
                    )
                }

                if (displayedTravels.isNotEmpty() && uiState.travelErrorMessage != null) {
                    item {
                        HomeSectionErrorContent(
                            message = uiState.travelErrorMessage,
                            onRetryClick = onTravelRetryClick,
                            retryEnabled = isTravelRetryEnabled,
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    HomeRecommendationHeader(
                        selectedRegion = uiState.selectedRegion,
                        isLoading = uiState.isPopularSpotLoading,
                        filterEnabled = !isHomeLoading,
                        refreshEnabled = isPopularRefreshEnabled,
                        onRegionSelected = onRegionSelected,
                        onRefreshClick = onPopularSpotsRefreshClick,
                    )
                }

                item {
                    YadanTravelSpotCategoryFilters(
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = onCategorySelected,
                        enabled = !isHomeLoading,
                        contentPadding = PaddingValues(horizontal = 20.dp),
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                when {
                    uiState.popularTravelSpots.isNotEmpty() -> {
                        items(
                            items = uiState.popularTravelSpots,
                            key = { spot -> spot.id },
                        ) { spot ->
                            YadanTravelSpotCard(
                                spot = spot,
                                onClick = { onTravelSpotClick(spot.id) },
                                onActionClick = { onDibsClick(spot.id) },
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
                                actionEnabled = isPopularRefreshEnabled && spot.id !in uiState.updatingDibsSpotIds,
                            )
                        }
                    }

                    uiState.travelSpotErrorMessage != null -> {
                        item {
                            HomeSectionErrorContent(
                                message = uiState.travelSpotErrorMessage,
                                onRetryClick = onPopularSpotsRefreshClick,
                                retryEnabled = isPopularRefreshEnabled,
                            )
                        }
                    }

                    uiState.isPopularSpotLoading -> {
                        item {
                            HomeLoadingContent()
                        }
                    }

                    else -> {
                        item {
                            HomeEmptySpotContent()
                        }
                    }
                }

                if (uiState.popularTravelSpots.isNotEmpty() && uiState.travelSpotErrorMessage != null) {
                    item {
                        HomeSectionErrorContent(
                            message = uiState.travelSpotErrorMessage,
                            onRetryClick = onPopularSpotsRefreshClick,
                            retryEnabled = isPopularRefreshEnabled,
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
    onNotificationClick: () -> Unit,
) {
    YadanMainHeader(
        title = "야단법석",
        style = YadanMainHeaderStyle.BRAND,
        modifier = Modifier.statusBarsPadding(),
        trailingContent = {
            YadanIconButton(
                onClick = onNotificationClick,
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "알림",
                )
            }
        },
    )
}

/**
 * 여행 조회 상태와 개수에 맞게 로딩, 오류, 카드 또는 확인된 빈 상태를 표시합니다.
 */
@Composable
private fun HomeTravelContent(
    travels: List<TravelSummary>,
    currentDate: LocalDate,
    isLoading: Boolean,
    hasLoadedTravelList: Boolean,
    errorMessage: String?,
    onRetryClick: () -> Unit,
    retryEnabled: Boolean,
    onTravelClick: (String) -> Unit,
    onGameScheduleClick: () -> Unit,
    initialPage: Int,
) {
    SubcomposeLayout(modifier = Modifier.fillMaxWidth()) { constraints ->
        val cardWidth = (constraints.maxWidth - HOME_TRAVEL_HORIZONTAL_PADDING.roundToPx() * 2).coerceAtLeast(0)
        val sizingConstraints = Constraints.fixedWidth(cardWidth)
        val travelHeight = subcompose("travel-height") {
            YadanTravelCardDefaults.HeightReference(travels, currentDate)
        }.single().measure(sizingConstraints).height
        val emptyHeight = subcompose("empty-height") {
            HomeEmptyTravelCardContent(onGameScheduleClick = {}, enabled = false)
        }.single().measure(sizingConstraints).height
        val cardHeight = maxOf(YadanTravelCardDefaults.Height.roundToPx(), travelHeight, emptyHeight).toDp()
        // 표시 후 높이를 갱신하지 않고, 첫 측정에서 모든 카드의 공통 높이를 결정합니다.
        val content = subcompose("content") {
            HomeTravelCards(
                travels = travels,
                currentDate = currentDate,
                isLoading = isLoading,
                hasLoadedTravelList = hasLoadedTravelList,
                errorMessage = errorMessage,
                onRetryClick = onRetryClick,
                retryEnabled = retryEnabled,
                onTravelClick = onTravelClick,
                onGameScheduleClick = onGameScheduleClick,
                initialPage = initialPage,
                cardHeight = cardHeight,
            )
        }.single().measure(constraints)
        layout(constraints.maxWidth, content.height) { content.placeRelative(0, 0) }
    }
}

private val HOME_TRAVEL_HORIZONTAL_PADDING = 24.dp
private val HOME_TRAVEL_VERTICAL_PADDING = 6.dp

/** 조회된 여행은 유지하고, 데이터가 없을 때 조회 실패와 실제 빈 목록을 구분합니다. */
@Composable
private fun HomeTravelCards(
    travels: List<TravelSummary>,
    currentDate: LocalDate,
    isLoading: Boolean,
    hasLoadedTravelList: Boolean,
    errorMessage: String?,
    onRetryClick: () -> Unit,
    retryEnabled: Boolean,
    onTravelClick: (String) -> Unit,
    onGameScheduleClick: () -> Unit,
    initialPage: Int,
    cardHeight: Dp,
) {
    when {
        travels.size > 1 -> {
            HomeTravelPager(
                travels = travels,
                currentDate = currentDate,
                onTravelClick = onTravelClick,
                initialPage = initialPage,
                cardHeight = cardHeight,
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
                modifier = Modifier.padding(horizontal = HOME_TRAVEL_HORIZONTAL_PADDING, vertical = HOME_TRAVEL_VERTICAL_PADDING)
                    .height(cardHeight),
            )
        }

        errorMessage != null -> {
            HomeSectionErrorContent(
                message = errorMessage,
                onRetryClick = onRetryClick,
                retryEnabled = retryEnabled,
                modifier = Modifier.heightIn(min = cardHeight + HOME_TRAVEL_VERTICAL_PADDING * 2),
            )
        }

        isLoading || !hasLoadedTravelList -> {
            HomeTravelLoadingContent(cardHeight)
        }

        else -> {
            HomeEmptyTravelCard(
                onGameScheduleClick = onGameScheduleClick,
                cardHeight = cardHeight,
                modifier = Modifier.padding(horizontal = HOME_TRAVEL_HORIZONTAL_PADDING, vertical = HOME_TRAVEL_VERTICAL_PADDING),
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
    initialPage: Int,
    cardHeight: Dp,
) {
    val pagerState =
        rememberPagerState(
            initialPage = initialPage,
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
                    .height(cardHeight + HOME_TRAVEL_VERTICAL_PADDING * 2),
            contentPadding = PaddingValues(horizontal = HOME_TRAVEL_HORIZONTAL_PADDING),
            pageSize = PageSize.Fill,
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
                modifier = Modifier.padding(vertical = HOME_TRAVEL_VERTICAL_PADDING).height(cardHeight),
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
    cardHeight: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
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

        HomeEmptyTravelCardContent(onGameScheduleClick = onGameScheduleClick)
    }
}

/** 빈 카드의 표시와 높이 측정에서 같은 본문을 사용합니다. */
@Composable
private fun HomeEmptyTravelCardContent(onGameScheduleClick: () -> Unit, enabled: Boolean = true) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
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
            enabled = enabled,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .fillMaxWidth(),
            trailingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                )
            },
        )
    }
}

/**
 * 추천 지역 선택과 새로고침 작업을 표시합니다.
 */
@Composable
private fun HomeRecommendationHeader(
    selectedRegion: Region,
    isLoading: Boolean,
    filterEnabled: Boolean,
    refreshEnabled: Boolean,
    onRegionSelected: (Region) -> Unit,
    onRefreshClick: () -> Unit,
) {
    FlowRow(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        YadanTravelRegionDropdown(
            selectedRegion = selectedRegion,
            onRegionSelected = onRegionSelected,
            enabled = filterEnabled,
        )

        TextButton(
            onClick = onRefreshClick,
            enabled = refreshEnabled,
            contentPadding = PaddingValues(horizontal = 4.dp),
            colors =
                ButtonDefaults.textButtonColors(
                    contentColor = YadanPrimary,
                ),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = YadanPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
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

@Composable
private fun HomeTravelLoadingContent(cardHeight: Dp) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(cardHeight + HOME_TRAVEL_VERTICAL_PADDING * 2),
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

/** 홈의 여행·인기 관광지 영역에서 오류 안내와 재시도 동작을 재사용합니다. */
@Composable
private fun HomeSectionErrorContent(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    retryEnabled: Boolean = true,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = YadanTypography.bodyMedium,
            color = YadanTextMuted,
            textAlign = TextAlign.Center,
        )

        TextButton(
            onClick = onRetryClick,
            enabled = retryEnabled,
            colors = ButtonDefaults.textButtonColors(contentColor = YadanPrimary),
        ) {
            Text(
                text = "다시 시도",
                style = YadanTypography.labelMedium,
            )
        }
    }
}

@Composable
private fun HomeEmptySpotContent() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
                .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "조건에 맞는 인기 관광지가 없습니다.",
            style = YadanTypography.bodyMedium,
            color = YadanTextMuted,
            textAlign = TextAlign.Center,
        )
    }
}

private class HomeUiStatePreviewProvider :
    PreviewParameterProvider<HomeUiState> {
    private val pagerTravels = previewPagerTravels()

    private val guestTravel =
        previewTravel(
            id = "travel-guest",
            name = "부산 사직 직관 여행",
            isLeader = false,
            startDate = LocalDate(2026, 5, 23),
            endDate = LocalDate(2026, 5, 24),
        )

    private val loaded = HomeUiState(
        travels = pagerTravels,
        popularTravelSpots = previewPopularSpots(),
        selectedRegion = Region.BUSAN,
        isLoading = false,
        hasLoadedTravelList = true,
    )

    private val travelLoadFailure = loaded.copy(
        travels = emptyList(),
        hasLoadedTravelList = false,
        travelErrorMessage = "여행 목록을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.",
    )

    private val popularLoadFailure = loaded.copy(
        travelSpotErrorMessage = "인기 관광지를 불러오지 못했습니다. 인터넷 연결을 확인한 후 다시 시도해주세요.",
    )

    override val values: Sequence<HomeUiState> =
        sequenceOf(
            loaded,
            loaded.copy(travels = pagerTravels.drop(1)),
            loaded.copy(travels = listOf(guestTravel)),
            loaded.copy(travels = emptyList()),
            travelLoadFailure,
            travelLoadFailure.copy(travels = pagerTravels.take(1)),
            travelLoadFailure.copy(travels = pagerTravels, hasLoadedTravelList = true),
            travelLoadFailure.copy(isTravelListLoading = true),
            travelLoadFailure.copy(travels = pagerTravels, hasLoadedTravelList = true, isTravelListLoading = true),
            loaded.copy(isTravelListLoading = true),
            loaded.copy(isPopularSpotLoading = true),
            loaded.copy(isTravelListLoading = true, isPopularSpotLoading = true),
            loaded.copy(isRefreshing = true, isTravelListLoading = true, isPopularSpotLoading = true),
            popularLoadFailure,
            popularLoadFailure.copy(popularTravelSpots = emptyList()),
            popularLoadFailure.copy(isPopularSpotLoading = true),
            popularLoadFailure.copy(popularTravelSpots = emptyList(), isPopularSpotLoading = true),
            loaded.copy(popularTravelSpots = emptyList(), isPopularSpotLoading = true),
            loaded.copy(popularTravelSpots = emptyList()),
            loaded.copy(updatingDibsSpotIds = setOf("spot-1")),
            loaded.copy(isLoading = true, popularTravelSpots = emptyList(), isPopularSpotLoading = true),
            loaded.copy(isLoading = true, travels = emptyList(), hasLoadedTravelList = false, isTravelListLoading = true),
            HomeUiState(isTravelListLoading = true, isPopularSpotLoading = true),
        )
}

private fun previewPagerTravels(): List<TravelSummary> = listOf(
    previewActiveTravel(),
    previewUpcomingTravel(),
    previewSecondUpcomingTravel(),
)

private fun previewActiveTravel(): TravelSummary = previewTravel(
    id = "travel-active",
    name = "부산 사직 직관 여행",
    isLeader = true,
    startDate = LocalDate(2026, 5, 20),
    endDate = LocalDate(2026, 5, 21),
    verifiedSpotsCount = 1,
)

private fun previewUpcomingTravel(): TravelSummary = previewTravel(
    id = "travel-upcoming",
    name = "주말 부산 야구 여행",
    isLeader = true,
    startDate = LocalDate(2026, 5, 23),
    endDate = LocalDate(2026, 5, 24),
)

private fun previewSecondUpcomingTravel(): TravelSummary = previewTravel(
    id = "travel-gwangju",
    name = "광주 야구 원정 여행",
    isLeader = true,
    region = Region.GWANGJU,
    startDate = LocalDate(2026, 6, 6),
    endDate = LocalDate(2026, 6, 7),
)

private fun previewTravel(
    id: String,
    name: String,
    isLeader: Boolean,
    region: Region = Region.BUSAN,
    startDate: LocalDate,
    endDate: LocalDate,
    verifiedSpotsCount: Int = 0,
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
        verifiedSpotsCount = verifiedSpotsCount,
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
    HomeScreenPreviewContent(uiState = uiState)
}

@Preview(
    name = "홈 전체 - 여행 1번째",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun HomeScreenFirstTravelPreview() {
    HomeScreenTravelPagePreviewContent(initialPage = 0)
}

@Preview(
    name = "홈 전체 - 여행 2번째",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun HomeScreenSecondTravelPreview() {
    HomeScreenTravelPagePreviewContent(initialPage = 1)
}

@Preview(
    name = "홈 전체 - 여행 3번째",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun HomeScreenThirdTravelPreview() {
    HomeScreenTravelPagePreviewContent(initialPage = 2)
}

@Composable
private fun HomeScreenTravelPagePreviewContent(initialPage: Int) {
    HomeScreenPreviewContent(
        uiState = HomeUiState(
            travels = previewPagerTravels(),
            popularTravelSpots = previewPopularSpots(),
            selectedRegion = Region.BUSAN,
            isLoading = false,
            hasLoadedTravelList = true,
        ),
        initialTravelPage = initialPage,
    )
}

@Composable
private fun HomeScreenPreviewContent(uiState: HomeUiState, initialTravelPage: Int = 0) {
    YadanbeopseokTheme {
        Scaffold(
            containerColor = YadanBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                Column {
                    HomeBottomNavigationPreview()
                    Spacer(
                        modifier = Modifier.fillMaxWidth()
                            .windowInsetsBottomHeight(WindowInsets.navigationBars)
                            .background(YadanSurface),
                    )
                }
            },
        ) { padding ->
            HomeScreen(
                uiState = uiState,
                currentDate = LocalDate(2026, 5, 20),
                onNotificationClick = {},
                onTravelClick = {},
                onGameScheduleClick = {},
                onRegionSelected = {},
                onCategorySelected = {},
                onRefreshClick = {},
                onTravelRetryClick = {},
                onPopularSpotsRefreshClick = {},
                onTravelSpotClick = {},
                onDibsClick = {},
                initialTravelPage = initialTravelPage,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        }
    }
}

@Composable
private fun HomeBottomNavigationPreview() {
    YadanBottomNavigation(
        centerAction = {
            YadanBottomNavigationCenterAction(onClick = {}) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "새 여행 만들기", modifier = Modifier.fillMaxSize())
            }
        },
        startItems = {
            YadanBottomNavigationItem(selected = true, onClick = {}, label = "홈") {
                Icon(imageVector = Icons.Default.Home, contentDescription = null, modifier = Modifier.fillMaxSize())
            }
            YadanBottomNavigationItem(selected = false, onClick = {}, label = "경기") {
                Icon(imageVector = Icons.Default.SportsBaseball, contentDescription = null, modifier = Modifier.fillMaxSize())
            }
        },
        endItems = {
            YadanBottomNavigationItem(selected = false, onClick = {}, label = "기록") {
                Icon(imageVector = Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.fillMaxSize())
            }
            YadanBottomNavigationItem(selected = false, onClick = {}, label = "마이") {
                Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.fillMaxSize())
            }
        },
    )
}

private data class HomeLayoutPreviewState(val uiState: HomeUiState, val page: Int = 0)

private class HomeLayoutPreviewProvider : PreviewParameterProvider<HomeLayoutPreviewState> {
    private val base = HomeUiState(
        travels = previewPagerTravels().mapIndexed { index, travel ->
            travel.copy(
                name = if (index == 1) "친구들과 함께 떠나는 아주 긴 이름의 서울 잠실 직관 여행" else travel.name,
                isLeader = index != 2,
            )
        },
        popularTravelSpots = previewPopularSpots(),
        selectedRegion = Region.BUSAN,
        isLoading = false,
        hasLoadedTravelList = true,
    )
    override val values = sequenceOf(
        HomeLayoutPreviewState(base.copy(travels = emptyList())),
        HomeLayoutPreviewState(base.copy(travels = base.travels.take(1))),
        HomeLayoutPreviewState(base, page = 0),
        HomeLayoutPreviewState(base, page = 1),
        HomeLayoutPreviewState(base, page = 2),
        HomeLayoutPreviewState(HomeUiState(isTravelListLoading = true, isPopularSpotLoading = true)),
        HomeLayoutPreviewState(
            base.copy(
                popularTravelSpots = emptyList(),
                travelSpotErrorMessage = "인기 관광지를 불러오지 못했습니다. 인터넷 연결을 확인한 후 다시 시도해주세요.",
            ),
        ),
    ) + HomeUiStatePreviewProvider().values
        .filter { state ->
            state.travelErrorMessage != null || state.travelSpotErrorMessage != null ||
                state.isTravelListLoading || state.isPopularSpotLoading || state.isRefreshing
        }
        .map { state -> HomeLayoutPreviewState(state) }
}

@Preview(name = "홈 전체 - 좁은 화면", showBackground = true, widthDp = 320, heightDp = 640)
@Preview(name = "홈 전체 - 큰 글꼴", showBackground = true, widthDp = 320, heightDp = 640, fontScale = 1.5f)
@Preview(name = "홈 전체 - 최대 글꼴", showBackground = true, widthDp = 320, heightDp = 640, fontScale = 2f)
@Preview(name = "홈 전체 - 낮은 가로 화면", showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun HomeScreenLayoutPreview(@PreviewParameter(HomeLayoutPreviewProvider::class) state: HomeLayoutPreviewState) {
    HomeScreenPreviewContent(uiState = state.uiState, initialTravelPage = state.page)
}
