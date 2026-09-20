package com.manruhomerun.yadanbeopseok.travel.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanFilterChip
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanSearchBar
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTabItem
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTabRow
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import com.manruhomerun.yadanbeopseok.model.TravelSpotFilterCategory
import com.manruhomerun.yadanbeopseok.travel.spot.viewmodel.TravelSpotSelectionTab
import com.manruhomerun.yadanbeopseok.travel.spot.viewmodel.TravelSpotSelectionUiState
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelSpotAction
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelSpotCard
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelSpotCategoryFilters

/**
 * B06과 C01b/C01c에서 사용하는 관광지 조회·선택 본문입니다.
 *
 * 부모 Screen의 LazyColumn에 항목을 추가하며 별도 스크롤을 만들지 않습니다.
 * 조회와 선택 상태를 직접 변경하지 않고 콜백으로 전달합니다.
 *
 * @param selectedSpotIds 현재 임시로 선택한 관광지 ID입니다.
 * @param disabledSpotIds 표시하되 선택할 수 없는 관광지 ID입니다.
 * @param canAddMoreSpots 선택되지 않은 관광지를 추가할 수 있는지 나타냅니다.
 * @param selectedSpotsContent 검색 중이 아닐 때 검색창과 추천 탭 사이에 표시할 영역입니다.
 * @param searchResultHeader 검색 중 카테고리 필터 아래에 표시할 제목 영역입니다.
 */
internal fun LazyListScope.travelSpotSelectionContent(
    uiState: TravelSpotSelectionUiState,
    selectedSpotIds: Set<String>,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onTabSelected: (TravelSpotSelectionTab) -> Unit,
    onCategorySelected: (TravelSpotCategory?) -> Unit,
    onDibsCategorySelected: (TravelSpotFilterCategory?) -> Unit,
    onTravelSpotClick: (TravelSpot) -> Unit,
    onTravelSpotToggle: (TravelSpot) -> Unit,
    onRetryClick: () -> Unit,
    onLoadNextDibsPage: () -> Unit,
    searchPlaceholder: String = "관광지·음식을 검색해보세요",
    disabledSpotIds: Set<String> = emptySet(),
    canAddMoreSpots: Boolean = true,
    selectedSpotsContent: LazyListScope.() -> Unit = {},
    searchResultHeader: LazyListScope.() -> Unit = {},
) {
    item(key = "search_bar") {
        YadanSearchBar(
            query = uiState.searchQuery,
            onQueryChange = onSearchQueryChange,
            placeholder = searchPlaceholder,
            enabled = !uiState.isLoading,
            onSearch = onSearch,
        )
    }

    if (uiState.isSearchMode) {
        item(key = "category_filters") {
            TravelSpotCategoryFilters(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = onCategorySelected,
            )
        }

        searchResultHeader()
    } else {
        selectedSpotsContent()

        item(key = "spot_tabs") {
            YadanTabRow(
                tabs = listOf(
                    YadanTabItem(label = "맞춤 추천"),
                    YadanTabItem(
                        label = "찜",
                        count = uiState.dibsSpots.size.takeIf { it > 0 },
                    ),
                ),
                selectedIndex = uiState.selectedTab.ordinal,
                onTabSelected = { index ->
                    onTabSelected(TravelSpotSelectionTab.entries[index])
                },
                modifier = Modifier.padding(top = 4.dp),
                enabled = !uiState.isLoading,
            )
        }

        if (uiState.selectedTab == TravelSpotSelectionTab.DIBS) {
            item(key = "dibs_category_filters") {
                YadanTravelSpotCategoryFilters(
                    selectedCategory = uiState.selectedDibsCategory,
                    onCategorySelected = onDibsCategorySelected,
                    modifier = Modifier.padding(top = 4.dp),
                    enabled = !uiState.isLoading,
                )
            }
        }
    }

    val spots = uiState.displayedSpots
    val emptyMessage = when {
        uiState.isSearchMode -> "검색 결과가 없습니다"
        uiState.selectedTab == TravelSpotSelectionTab.SUGGESTED -> "추천 관광지가 없습니다"
        else -> "찜한 관광지가 없습니다"
    }

    if (uiState.isLoading || uiState.errorMessage != null || spots.isEmpty()) {
        item(key = "spot_list_status") {
            TravelSpotListStatus(
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                emptyMessage = emptyMessage,
                onRetryClick = onRetryClick,
            )
        }
    } else {
        items(
            items = spots,
            key = { spot -> "available_${spot.id}" },
        ) { spot ->
            val disabled = spot.id in disabledSpotIds
            val selected = spot.id in selectedSpotIds || disabled
            val actionEnabled = !disabled && (selected || canAddMoreSpots)

            YadanTravelSpotCard(
                spot = spot,
                action = if (selected) {
                    YadanTravelSpotAction.ADDED
                } else {
                    YadanTravelSpotAction.ADD
                },
                enabled = !disabled,
                actionEnabled = actionEnabled,
                onClick = { onTravelSpotClick(spot) },
                onActionClick = { onTravelSpotToggle(spot) },
            )
        }

        if (!uiState.isSearchMode && uiState.selectedTab == TravelSpotSelectionTab.DIBS) {
            when {
                uiState.isDibsSpotsLoadingMore -> {
                    item(key = "dibs_loading_more") {
                        TravelSpotLoadMoreProgress()
                    }
                }

                uiState.dibsLoadMoreErrorMessage != null -> {
                    item(key = "dibs_load_more_error") {
                        TravelSpotLoadMoreError(
                            message = uiState.dibsLoadMoreErrorMessage,
                            onRetryClick = onRetryClick,
                        )
                    }
                }

                uiState.hasNextDibsPage -> {
                    item(key = "dibs_load_next_page") {
                        LaunchedEffect(uiState.dibsPageNumber) {
                            onLoadNextDibsPage()
                        }
                    }
                }
            }
        }
    }
}

/** 찜 목록의 다음 페이지를 조회하는 동안 하단 진행 상태를 표시합니다. */
@Composable
private fun TravelSpotLoadMoreProgress() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = YadanPrimary,
            strokeWidth = 2.dp,
        )
    }
}

/** 찜 목록의 다음 페이지 조회 실패 문구와 재시도 동작을 표시합니다. */
@Composable
private fun TravelSpotLoadMoreError(
    message: String,
    onRetryClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = message,
            style = YadanTypography.bodySmall,
            color = YadanTextSecondary,
            textAlign = TextAlign.Center,
        )

        YadanButton(
            text = "다시 시도",
            onClick = onRetryClick,
            modifier = Modifier.widthIn(min = 120.dp),
        )
    }
}

/** 검색 결과의 카테고리 필터를 가로 스크롤로 표시합니다. */
@Composable
private fun TravelSpotCategoryFilters(
    selectedCategory: TravelSpotCategory?,
    onCategorySelected: (TravelSpotCategory?) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        item(key = "category_all") {
            YadanFilterChip(
                text = "전체",
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
            )
        }

        items(items = SEARCH_FILTER_CATEGORIES, key = { it.name }) { category ->
            YadanFilterChip(
                text = category.displayName,
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
            )
        }
    }
}

private val SEARCH_FILTER_CATEGORIES = listOf(
    TravelSpotCategory.ACCOMMODATION,
    TravelSpotCategory.FESTIVAL,
    TravelSpotCategory.FOOD,
    TravelSpotCategory.LEISURE,
    TravelSpotCategory.TOURIST_ATTRACTION,
    TravelSpotCategory.SHOPPING,
    TravelSpotCategory.CULTURE,
    TravelSpotCategory.TRAVEL_COURSE,
)

/** 로딩, 조회 오류와 빈 목록을 기존 B06의 표시 우선순위로 처리합니다. */
@Composable
private fun TravelSpotListStatus(
    isLoading: Boolean,
    errorMessage: String?,
    emptyMessage: String,
    onRetryClick: () -> Unit,
) {
    val hasError = errorMessage != null
    val minimumHeight = if (isLoading || hasError) 180.dp else 150.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minimumHeight)
            .padding(horizontal = if (hasError) 24.dp else 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(color = YadanPrimary)
            }

            errorMessage != null -> {
                Text(
                    text = "관광지 정보를 확인할 수 없습니다",
                    style = YadanTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = YadanTextPrimary,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = errorMessage,
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

            else -> {
                Text(
                    text = emptyMessage,
                    style = YadanTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = YadanTextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
