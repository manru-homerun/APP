package com.manruhomerun.yadanbeopseok.travel.course.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import com.manruhomerun.yadanbeopseok.travel.component.travelSpotSelectionContent
import com.manruhomerun.yadanbeopseok.travel.course.viewmodel.TravelCourseSpotSelectionUiState
import com.manruhomerun.yadanbeopseok.travel.spot.viewmodel.TravelSpotSelectionTab
import com.manruhomerun.yadanbeopseok.travel.spot.viewmodel.TravelSpotSelectionUiState

/**
 * C01b 관광지 추가와 C01c 검색 결과 화면입니다.
 *
 * 추천·찜·검색 본문은 B06과 공유합니다.
 * 임시 선택 반영, 선택 취소와 화면 이동은 콜백으로 전달합니다.
 *
 * @param disabledSpotIds 이미 여행 일정에 포함되어 추가할 수 없는 관광지 ID입니다.
 * @param onDoneClick 선택한 관광지를 대상 일차에 반영하고 C01로 복귀하는 콜백입니다.
 */
@Composable
fun TravelCourseSpotSelectionScreen(
    uiState: TravelCourseSpotSelectionUiState,
    disabledSpotIds: Set<String>,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onTabSelected: (TravelSpotSelectionTab) -> Unit,
    onCategorySelected: (TravelSpotCategory?) -> Unit,
    onTravelSpotClick: (TravelSpot) -> Unit,
    onTravelSpotToggle: (TravelSpot) -> Unit,
    onBackClick: () -> Unit,
    onDoneClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val targetDay = uiState.targetDay ?: return
    val selectionState = uiState.selectionState
    val selectedSpotIds = remember(uiState.selectedTravelSpots) {
        uiState.selectedTravelSpots.mapTo(mutableSetOf()) { it.id }
    }
    val buttonText = when {
        selectionState.isSearchMode -> "완료"
        uiState.selectedCount > 0 -> "${uiState.selectedCount}곳 추가 · 완료"
        else -> "완료"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YadanBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding(),
    ) {
        YadanTopAppBar(
            title = "DAY ${targetDay}에 추가",
            onNavigationClick = onBackClick,
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(
                start = 24.dp,
                top = 12.dp,
                end = 24.dp,
                bottom = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            travelSpotSelectionContent(
                uiState = selectionState,
                selectedSpotIds = selectedSpotIds,
                onSearchQueryChange = onSearchQueryChange,
                onSearch = onSearch,
                onTabSelected = onTabSelected,
                onCategorySelected = onCategorySelected,
                onTravelSpotClick = onTravelSpotClick,
                onTravelSpotToggle = onTravelSpotToggle,
                onRetryClick = onRetryClick,
                searchPlaceholder = "관광지·음식을 검색해서 추가",
                disabledSpotIds = disabledSpotIds,
            )
        }

        YadanButton(
            text = buttonText,
            onClick = onDoneClick,
            enabled = !selectionState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 24.dp,
                    top = 11.dp,
                    end = 24.dp,
                    bottom = 20.dp,
                ),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            },
        )
    }
}

@Preview(
    name = "C01b 맞춤 추천",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelCourseSpotSuggestedPreview() {
    TravelCourseSpotSelectionPreview(
        selectionState = TravelSpotSelectionUiState(
            suggestedSpots = previewCourseSpots,
        ),
    )
}

@Preview(
    name = "C01b 선택 및 기존 일정 포함",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelCourseSpotSelectedPreview() {
    TravelCourseSpotSelectionPreview(
        selectionState = TravelSpotSelectionUiState(
            suggestedSpots = previewCourseSpots,
        ),
        selectedTravelSpots = listOf(previewCourseSpots.first()),
        disabledSpotIds = setOf(previewCourseSpots.last().id),
    )
}

@Preview(
    name = "C01b 찜 목록",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelCourseSpotDibsPreview() {
    TravelCourseSpotSelectionPreview(
        selectionState = TravelSpotSelectionUiState(
            selectedTab = TravelSpotSelectionTab.DIBS,
            dibsSpots = previewCourseSpots.filter { it.dibs },
        ),
    )
}

@Preview(
    name = "C01c 검색 결과",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelCourseSpotSearchPreview() {
    TravelCourseSpotSelectionPreview(
        selectionState = TravelSpotSelectionUiState(
            searchQuery = "전포",
            searchResults = listOf(previewCourseSpots.last()),
        ),
        selectedTravelSpots = listOf(previewCourseSpots.last()),
    )
}

@Preview(
    name = "C01c 검색 결과 없음",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelCourseSpotEmptyPreview() {
    TravelCourseSpotSelectionPreview(
        selectionState = TravelSpotSelectionUiState(searchQuery = "검색어"),
    )
}

@Preview(
    name = "C01b 로딩",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelCourseSpotLoadingPreview() {
    TravelCourseSpotSelectionPreview(
        selectionState = TravelSpotSelectionUiState(
            isSuggestedSpotsLoading = true,
        ),
    )
}

@Preview(
    name = "C01b 조회 오류",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelCourseSpotErrorPreview() {
    TravelCourseSpotSelectionPreview(
        selectionState = TravelSpotSelectionUiState(
            errorMessage = "인터넷 연결을 확인한 후 다시 시도해주세요.",
        ),
    )
}

/** 상태별 Preview에서 동일한 화면과 콜백 구성을 재사용합니다. */
@Composable
private fun TravelCourseSpotSelectionPreview(
    selectionState: TravelSpotSelectionUiState,
    selectedTravelSpots: List<TravelSpot> = emptyList(),
    disabledSpotIds: Set<String> = emptySet(),
) {
    YadanbeopseokTheme {
        TravelCourseSpotSelectionScreen(
            uiState = TravelCourseSpotSelectionUiState(
                targetDay = 1,
                selectionState = selectionState,
                selectedTravelSpots = selectedTravelSpots,
            ),
            disabledSpotIds = disabledSpotIds,
            onSearchQueryChange = {},
            onSearch = {},
            onTabSelected = {},
            onCategorySelected = {},
            onTravelSpotClick = {},
            onTravelSpotToggle = {},
            onBackClick = {},
            onDoneClick = {},
            onRetryClick = {},
        )
    }
}

private val previewCourseSpots = listOf(
    TravelSpot(
        id = "preview-haeundae",
        name = "해운대 해수욕장",
        category = TravelSpotCategory.NATURE,
    ),
    TravelSpot(
        id = "preview-jagalchi",
        name = "자갈치 시장",
        category = TravelSpotCategory.SHOPPING,
        dibs = true,
    ),
    TravelSpot(
        id = "preview-jeonpo",
        name = "전포 카페거리",
        category = TravelSpotCategory.FOOD,
    ),
)
