package com.manruhomerun.yadanbeopseok.mypage.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.manruhomerun.yadanbeopseok.model.TravelSpotFilterCategory
import com.manruhomerun.yadanbeopseok.mypage.viewmodel.TravelSpotDibsUiState
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelRegionDropdown
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelSpotAction
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelSpotCard
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelSpotCategoryFilters

/**
 * H·04 찜한 관광지 화면입니다.
 *
 * 지역·카테고리 필터와 찜한 관광지 목록을 표시합니다.
 * 관광지 카드를 누르면 관광지 상세 화면으로 이동하고,
 * 하트 버튼을 누르면 해당 관광지의 찜을 취소합니다.
 */
@Composable
fun TravelSpotDibsScreen(
    uiState: TravelSpotDibsUiState,
    onBackClick: () -> Unit,
    onRegionSelected: (Region) -> Unit,
    onCategorySelected: (TravelSpotFilterCategory) -> Unit,
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

        YadanTravelRegionDropdown(
            selectedRegion = uiState.selectedRegion,
            onRegionSelected = onRegionSelected,
            modifier = Modifier.padding(horizontal = 18.dp),
            enabled = !uiState.isLoading,
        )

        YadanTravelSpotCategoryFilters(
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = onCategorySelected,
            enabled = !uiState.isLoading,
            contentPadding = PaddingValues(horizontal = 18.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            uiState.isLoading -> {
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
                    travelSpots = uiState.dibsSpots,
                    updatingDibsSpotIds = uiState.updatingDibsSpotIds,
                    onTravelSpotClick = onTravelSpotClick,
                    onDibsClick = onDibsClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * 선택한 서버 필터로 조회한 찜한 관광지 목록을 표시합니다.
 */
@Composable
private fun TravelSpotDibsContent(
    travelSpots: List<TravelSpot>,
    updatingDibsSpotIds: Set<String>,
    onTravelSpotClick: (String) -> Unit,
    onDibsClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 18.dp,
            top = 0.dp,
            end = 18.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (travelSpots.isEmpty()) {
            item(key = "empty") {
                TravelSpotDibsEmptyContent()
            }
        } else {
            items(
                items = travelSpots,
                key = { travelSpot -> travelSpot.id },
            ) { travelSpot ->
                val enabled = travelSpot.id !in updatingDibsSpotIds

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
 * 선택한 지역과 카테고리에 찜한 관광지가 없음을 표시합니다.
 */
@Composable
private fun TravelSpotDibsEmptyContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "선택한 조건에 찜한 관광지가 없습니다",
            style = YadanTypography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = YadanTextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

private val previewDibsSpots = listOf(
    TravelSpot(
        id = "1",
        name = "스테이 광안",
        region = Region.BUSAN,
        category = TravelSpotCategory.ACCOMMODATION,
        dibs = true,
    ),
    TravelSpot(
        id = "2",
        name = "해운대 오션뷰 호텔",
        region = Region.BUSAN,
        category = TravelSpotCategory.ACCOMMODATION,
        dibs = true,
    ),
    TravelSpot(
        id = "3",
        name = "송정 게스트하우스",
        region = Region.BUSAN,
        category = TravelSpotCategory.ACCOMMODATION,
        dibs = true,
    ),
)

private val previewRestaurantDibsSpots = listOf(
    TravelSpot(
        id = "4",
        name = "돼지국밥 거리",
        region = Region.BUSAN,
        category = TravelSpotCategory.FOOD,
        dibs = true,
    ),
    TravelSpot(
        id = "5",
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
            dibsSpots = previewRestaurantDibsSpots,
            selectedCategory = TravelSpotFilterCategory.RESTAURANT,
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
            onRegionSelected = {},
            onCategorySelected = {},
            onTravelSpotClick = {},
            onDibsClick = {},
            onRetryClick = {},
        )
    }
}
