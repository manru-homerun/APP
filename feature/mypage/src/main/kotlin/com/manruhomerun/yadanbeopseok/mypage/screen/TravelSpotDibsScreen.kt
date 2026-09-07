package com.manruhomerun.yadanbeopseok.mypage.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanFilterChip
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import com.manruhomerun.yadanbeopseok.mypage.viewmodel.TravelSpotDibsUiState
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelSpotAction
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelSpotCard

/**
 * H·04 찜한 관광지 화면입니다.
 *
 * 카테고리 필터와 찜한 관광지 목록을 표시합니다.
 * 관광지 카드를 누르면 관광지 상세 화면으로 이동하고,
 * 하트 버튼을 누르면 해당 관광지의 찜을 취소합니다.
 */
@Composable
fun TravelSpotDibsScreen(
    uiState: TravelSpotDibsUiState,
    onBackClick: () -> Unit,
    onCategorySelected: (TravelSpotCategory?) -> Unit,
    onTravelSpotClick: (String) -> Unit,
    onDibsClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YadanBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        YadanTopAppBar(
            title = "찜한 관광지",
            onNavigationClick = onBackClick,
        )

        when {
            uiState.isLoading && uiState.dibsSpots.isEmpty() -> {
                TravelSpotDibsLoadingContent(
                    modifier = Modifier.weight(1f),
                )
            }

            uiState.errorMessage != null && uiState.dibsSpots.isEmpty() -> {
                TravelSpotDibsErrorContent(
                    message = uiState.errorMessage,
                    onRetryClick = onRetryClick,
                    modifier = Modifier.weight(1f),
                )
            }

            else -> {
                TravelSpotDibsContent(
                    uiState = uiState,
                    onCategorySelected = onCategorySelected,
                    onTravelSpotClick = onTravelSpotClick,
                    onDibsClick = onDibsClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * 카테고리 필터와 필터링된 관광지 목록을 표시합니다.
 */
@Composable
private fun TravelSpotDibsContent(
    uiState: TravelSpotDibsUiState,
    onCategorySelected: (TravelSpotCategory?) -> Unit,
    onTravelSpotClick: (String) -> Unit,
    onDibsClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 18.dp,
            top = 2.dp,
            end = 18.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "category_filters") {
            TravelSpotDibsCategoryFilters(
                selectedCategory = uiState.selectedCategory,
                enabled = !uiState.isLoading,
                onCategorySelected = onCategorySelected,
            )
        }

        if (uiState.displayedTravelSpots.isEmpty()) {
            item(key = "empty") {
                TravelSpotDibsEmptyContent(
                    hasCategoryFilter = uiState.selectedCategory != null,
                )
            }
        } else {
            items(
                items = uiState.displayedTravelSpots,
                key = { travelSpot -> travelSpot.id },
            ) { travelSpot ->
                val enabled =
                    !uiState.isLoading &&
                        travelSpot.id !in uiState.updatingDibsSpotIds

                YadanTravelSpotCard(
                    spot = travelSpot,
                    onClick = {
                        onTravelSpotClick(travelSpot.id)
                    },
                    onActionClick = {
                        onDibsClick(travelSpot.id)
                    },
                    action = YadanTravelSpotAction.DIBS,
                    enabled = enabled,
                )
            }
        }
    }
}

/**
 * HTML의 가로 스크롤 카테고리 필터를 표시합니다.
 */
@Composable
private fun TravelSpotDibsCategoryFilters(
    selectedCategory: TravelSpotCategory?,
    enabled: Boolean,
    onCategorySelected: (TravelSpotCategory?) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        contentPadding = PaddingValues(end = 18.dp),
    ) {
        item(key = "all") {
            YadanFilterChip(
                text = "전체",
                selected = selectedCategory == null,
                onClick = {
                    onCategorySelected(null)
                },
                enabled = enabled,
            )
        }

        items(
            items = dibsFilterCategories,
            key = { category -> category.name },
        ) { category ->
            YadanFilterChip(
                text = category.displayName,
                selected = selectedCategory == category,
                onClick = {
                    onCategorySelected(category)
                },
                enabled = enabled,
            )
        }
    }
}

/**
 * 찜 목록을 처음 불러오는 동안 진행 상태를 표시합니다.
 */
@Composable
private fun TravelSpotDibsLoadingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = YadanPrimary)
    }
}

/**
 * 찜 목록 조회 실패 문구와 재시도 버튼을 표시합니다.
 */
@Composable
private fun TravelSpotDibsErrorContent(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 280.dp)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "찜한 관광지를 확인할 수 없습니다",
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

/**
 * 전체 또는 선택한 카테고리에 찜한 관광지가 없음을 표시합니다.
 */
@Composable
private fun TravelSpotDibsEmptyContent(
    hasCategoryFilter: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (hasCategoryFilter) {
                "선택한 카테고리에 찜한 관광지가 없습니다"
            } else {
                "아직 찜한 관광지가 없습니다"
            },
            style = YadanTypography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = YadanTextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

private val dibsFilterCategories = TravelSpotCategory.entries.filterNot { category ->
    category == TravelSpotCategory.STADIUM ||
        category == TravelSpotCategory.UNKNOWN
}

private val previewDibsSpots = listOf(
    TravelSpot(
        id = "1",
        name = "감천문화마을",
        region = Region.BUSAN,
        category = TravelSpotCategory.CULTURE,
        dibs = true,
    ),
    TravelSpot(
        id = "2",
        name = "돼지국밥 거리",
        region = Region.BUSAN,
        category = TravelSpotCategory.FOOD,
        dibs = true,
    ),
    TravelSpot(
        id = "3",
        name = "광안리 해변",
        region = Region.BUSAN,
        category = TravelSpotCategory.NATURE,
        dibs = true,
    ),
    TravelSpot(
        id = "4",
        name = "전포 카페거리",
        region = Region.BUSAN,
        category = TravelSpotCategory.FOOD,
        dibs = true,
    ),
)

@Preview(
    name = "H04 찜한 관광지",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 760,
)
@Composable
private fun TravelSpotDibsScreenPreview() {
    TravelSpotDibsPreview(
        uiState = TravelSpotDibsUiState(
            dibsSpots = previewDibsSpots,
            isLoading = false,
        ),
    )
}

@Preview(
    name = "H04 카테고리 선택",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 760,
)
@Composable
private fun TravelSpotDibsCategoryPreview() {
    TravelSpotDibsPreview(
        uiState = TravelSpotDibsUiState(
            dibsSpots = previewDibsSpots,
            selectedCategory = TravelSpotCategory.FOOD,
            isLoading = false,
        ),
    )
}

@Preview(
    name = "H04 빈 목록",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 760,
)
@Composable
private fun TravelSpotDibsEmptyPreview() {
    TravelSpotDibsPreview(
        uiState = TravelSpotDibsUiState(
            isLoading = false,
        ),
    )
}

@Preview(
    name = "H04 로딩",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 760,
)
@Composable
private fun TravelSpotDibsLoadingPreview() {
    TravelSpotDibsPreview(
        uiState = TravelSpotDibsUiState(),
    )
}

@Preview(
    name = "H04 오류",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 760,
)
@Composable
private fun TravelSpotDibsErrorPreview() {
    TravelSpotDibsPreview(
        uiState = TravelSpotDibsUiState(
            isLoading = false,
            errorMessage = "인터넷 연결을 확인한 후 다시 시도해주세요.",
        ),
    )
}

@Composable
private fun TravelSpotDibsPreview(
    uiState: TravelSpotDibsUiState,
) {
    YadanbeopseokTheme {
        TravelSpotDibsScreen(
            uiState = uiState,
            onBackClick = {},
            onCategorySelected = {},
            onTravelSpotClick = {},
            onDibsClick = {},
            onRetryClick = {},
        )
    }
}
