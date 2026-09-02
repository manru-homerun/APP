package com.manruhomerun.yadanbeopseok.travel.creation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanSectionHeader
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanSectionMetaText
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOutline
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import com.manruhomerun.yadanbeopseok.travel.component.travelSpotSelectionContent
import com.manruhomerun.yadanbeopseok.travel.spot.viewmodel.TravelSpotSelectionTab
import com.manruhomerun.yadanbeopseok.travel.spot.viewmodel.TravelSpotSelectionUiState
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelSpotAction
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelSpotCard

/**
 * B·06 여행 만들기의 필수 관광지 선택 화면입니다.
 *
 * 사용자는 맞춤 추천·찜·검색 결과에서 관광지를 선택할 수 있습니다.
 * 선택하지 않고 AI가 전체 코스를 구성하도록 진행하는 것도 허용합니다.
 *
 * 화면 이동과 실제 코스 생성은 Screen에서 처리하지 않고 콜백으로 전달합니다.
 */
@Composable
fun TravelSpotSelectionScreen(
    uiState: TravelSpotSelectionUiState,
    selectedTravelSpots: List<TravelSpot>,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSearchDoneClick: () -> Unit,
    onTabSelected: (TravelSpotSelectionTab) -> Unit,
    onCategorySelected: (TravelSpotCategory?) -> Unit,
    onTravelSpotClick: (TravelSpot) -> Unit,
    onTravelSpotToggle: (TravelSpot) -> Unit,
    onBackClick: () -> Unit,
    onGenerateClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedSpotIds = remember(selectedTravelSpots) {
        selectedTravelSpots.mapTo(mutableSetOf()) { spot -> spot.id }
    }

    val selectedCount = selectedTravelSpots.size
    val screenTitle = if (uiState.isSearchMode) {
        "‘${uiState.searchQuery}’ 검색 결과"
    } else {
        "꼭 가고 싶은 곳이 있나요?"
    }

    val screenDescription = if (uiState.isSearchMode) {
        null
    } else {
        "담은 곳은 코스에 꼭 포함돼요. 나머지는 AI가 채워드려요."
    }

    TravelCreationScaffold(
        currentStep = TravelCreationStep.SPOT_SELECTION,
        title = screenTitle,
        description = screenDescription,
        onNavigationClick = onBackClick,
        modifier = modifier,
        bottomBar = {
            TravelSpotSelectionBottomButton(
                selectedCount = selectedCount,
                isSearchMode = uiState.isSearchMode,
                isLoading = uiState.isLoading,
                onSearchDoneClick = onSearchDoneClick,
                onGenerateClick = onGenerateClick,
            )
        },
    ) {
        travelSpotSelectionContent(
            uiState = uiState,
            selectedSpotIds = selectedSpotIds,
            onSearchQueryChange = onSearchQueryChange,
            onSearch = onSearch,
            onTabSelected = onTabSelected,
            onCategorySelected = onCategorySelected,
            onTravelSpotClick = onTravelSpotClick,
            onTravelSpotToggle = onTravelSpotToggle,
            onRetryClick = onRetryClick,
            selectedSpotsContent = {
                item(key = "selected_spot_header") {
                    YadanSectionHeader(title = "꼭 가고 싶은 곳 $selectedCount")
                }

                if (selectedTravelSpots.isEmpty()) {
                    item(key = "selected_spot_empty") {
                        TravelSpotSelectionEmptyCard()
                    }
                } else {
                    items(
                        items = selectedTravelSpots,
                        key = { spot -> "selected_${spot.id}" },
                    ) { spot ->
                        YadanTravelSpotCard(
                            spot = spot,
                            action = YadanTravelSpotAction.REMOVE,
                            onClick = { onTravelSpotClick(spot) },
                            onActionClick = { onTravelSpotToggle(spot) },
                        )
                    }
                }
            },
            searchResultHeader = {
                item(key = "search_result_header") {
                    YadanSectionHeader(
                        title = "검색 결과",
                        trailingContent = {
                            YadanSectionMetaText(text = "${uiState.filteredSearchResults.size}곳")
                        },
                    )
                }
            },
        )
    }
}

/**
 * B·06 하단 버튼을 검색 완료 또는 AI 코스 생성 동작으로 구성합니다.
 */
@Composable
private fun TravelSpotSelectionBottomButton(
    selectedCount: Int,
    isSearchMode: Boolean,
    isLoading: Boolean,
    onSearchDoneClick: () -> Unit,
    onGenerateClick: () -> Unit,
) {
    val text = when {
        isSearchMode && selectedCount > 0 -> "${selectedCount}곳 선택 · 완료"
        isSearchMode -> "완료"
        selectedCount > 0 -> "AI 코스 만들기 · ${selectedCount}곳 포함"
        else -> "AI 코스 만들기"
    }

    YadanButton(
        text = text,
        onClick = if (isSearchMode) {
            onSearchDoneClick
        } else {
            onGenerateClick
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 24.dp,
                top = 11.dp,
                end = 24.dp,
                bottom = 20.dp,
            ),
        enabled = !isLoading,
        trailingIcon = {
            Icon(
                imageVector = if (isSearchMode) {
                    Icons.Default.Check
                } else {
                    Icons.Outlined.AutoAwesome
                },
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        },
    )
}

/**
 * 사용자가 관광지를 담지 않아도 진행할 수 있음을 안내합니다.
 */
@Composable
private fun TravelSpotSelectionEmptyCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = YadanShapes.large,
            )
            .background(
                color = YadanSurface,
                shape = YadanShapes.large,
            ),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = 1.5.dp.toPx()

            drawRoundRect(
                color = YadanOutline,
                topLeft = Offset(
                    x = strokeWidth / 2f,
                    y = strokeWidth / 2f,
                ),
                size = Size(
                    width = size.width - strokeWidth,
                    height = size.height - strokeWidth,
                ),
                cornerRadius = CornerRadius(
                    x = 18.dp.toPx(),
                    y = 18.dp.toPx(),
                ),
                style = Stroke(
                    width = strokeWidth,
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(
                            5.dp.toPx(),
                            4.dp.toPx(),
                        ),
                    ),
                ),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 22.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = YadanPrimary,
            )

            Text(
                text = "딱히 없으면 비워두세요",
                style = YadanTypography.bodyMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
                color = YadanTextPrimary,
                textAlign = TextAlign.Center,
            )

            Text(
                text = "담은 곳은 코스에 꼭 넣고, 나머지는 AI가 취향·동선에 맞춰 채워요.",
                style = YadanTypography.bodySmall,
                color = YadanTextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(
    name = "B06a 담은 관광지 없음",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelSpotSelectionEmptyPreview() {
    TravelSpotSelectionPreview(
        uiState = TravelSpotSelectionUiState(
            suggestedSpots = previewSuggestedSpots,
        ),
    )
}

@Preview(
    name = "B06c 관광지를 담은 상태",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelSpotSelectionSelectedPreview() {
    TravelSpotSelectionPreview(
        uiState = TravelSpotSelectionUiState(
            suggestedSpots = previewSuggestedSpots,
            dibsSpots = listOf(previewSuggestedSpots.last()),
        ),
        selectedTravelSpots = previewSelectedSpots,
    )
}

@Preview(
    name = "B06b 관광지 검색 결과",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelSpotSelectionSearchPreview() {
    TravelSpotSelectionPreview(
        uiState = TravelSpotSelectionUiState(
            searchQuery = "감천",
            searchResults = previewSearchSpots,
        ),
        selectedTravelSpots = listOf(previewSearchSpots.first()),
    )
}

@Preview(
    name = "B06 관광지 로딩",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelSpotSelectionLoadingPreview() {
    TravelSpotSelectionPreview(
        uiState = TravelSpotSelectionUiState(
            isSuggestedSpotsLoading = true,
        ),
    )
}

@Preview(
    name = "B06 관광지 조회 오류",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelSpotSelectionErrorPreview() {
    TravelSpotSelectionPreview(
        uiState = TravelSpotSelectionUiState(
            errorMessage = "인터넷 연결을 확인한 후 다시 시도해주세요.",
        ),
    )
}

/**
 * B·06의 상태별 Preview에서 공통 화면 구성을 재사용합니다.
 */
@Composable
private fun TravelSpotSelectionPreview(
    uiState: TravelSpotSelectionUiState,
    selectedTravelSpots: List<TravelSpot> = emptyList(),
) {
    YadanbeopseokTheme {
        TravelSpotSelectionScreen(
            uiState = uiState,
            selectedTravelSpots = selectedTravelSpots,
            onSearchQueryChange = {},
            onSearch = {},
            onSearchDoneClick = {},
            onTabSelected = {},
            onCategorySelected = {},
            onTravelSpotClick = {},
            onTravelSpotToggle = {},
            onBackClick = {},
            onGenerateClick = {},
            onRetryClick = {},
        )
    }
}

private val previewSuggestedSpots = listOf(
    TravelSpot(
        id = "spot-haeundae",
        name = "해운대 해수욕장",
        address = "부산광역시 해운대구 해운대해변로 264",
        region = Region.BUSAN,
        category = TravelSpotCategory.NATURE,
    ),
    TravelSpot(
        id = "spot-jagalchi",
        name = "자갈치 시장",
        address = "부산광역시 중구 자갈치해안로 52",
        region = Region.BUSAN,
        category = TravelSpotCategory.SHOPPING,
        dibs = true,
    ),
    TravelSpot(
        id = "spot-jeonpo",
        name = "전포 카페거리",
        address = "부산광역시 부산진구 전포대로",
        region = Region.BUSAN,
        category = TravelSpotCategory.FOOD,
    ),
)

private val previewSelectedSpots = listOf(
    TravelSpot(
        id = "spot-gamcheon",
        name = "감천문화마을",
        address = "부산광역시 사하구 감내2로 203",
        region = Region.BUSAN,
        category = TravelSpotCategory.CULTURE,
    ),
    TravelSpot(
        id = "spot-gwangalli",
        name = "광안리 해변",
        address = "부산광역시 수영구 광안해변로 219",
        region = Region.BUSAN,
        category = TravelSpotCategory.NATURE,
    ),
)

private val previewSearchSpots = listOf(
    previewSelectedSpots.first(),
    TravelSpot(
        id = "spot-gamcheon-cafe",
        name = "감천 카페거리",
        address = "부산광역시 사하구 감천동",
        region = Region.BUSAN,
        category = TravelSpotCategory.FOOD,
    ),
    TravelSpot(
        id = "spot-gamcheon-port",
        name = "감천항 회센터",
        address = "부산광역시 사하구 감천항로",
        region = Region.BUSAN,
        category = TravelSpotCategory.FOOD,
    ),
)
