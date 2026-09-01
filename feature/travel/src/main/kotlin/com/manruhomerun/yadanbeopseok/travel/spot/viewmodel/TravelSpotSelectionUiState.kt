package com.manruhomerun.yadanbeopseok.travel.spot.viewmodel

import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory

/**
 * 관광지 목록에서 선택할 수 있는 조회 유형입니다.
 */
enum class TravelSpotSelectionTab {
    SUGGESTED,
    DIBS,
}

/**
 * 관광지 추천·찜·검색 화면에서 공통으로 사용하는 조회 상태입니다.
 *
 * 실제로 선택한 관광지 목록은 각 기능의 ViewModel에서 별도로 관리합니다.
 * B06에서는 여행 코스 생성에 포함할 관광지를 관리하고,
 * C01b에서는 특정 일차에 추가할 관광지를 관리합니다.
 */
data class TravelSpotSelectionUiState(
    /** 기본 관광지 목록에서 현재 선택한 탭입니다. */
    val selectedTab: TravelSpotSelectionTab = TravelSpotSelectionTab.SUGGESTED,

    /** 선택한 여행 지역을 기준으로 조회한 맞춤 추천 관광지입니다. */
    val suggestedSpots: List<TravelSpot> = emptyList(),

    /** 선택한 여행 지역을 기준으로 조회한 찜한 관광지입니다. */
    val dibsSpots: List<TravelSpot> = emptyList(),

    /** 현재 검색어와 일치하는 관광지 검색 결과입니다. */
    val searchResults: List<TravelSpot> = emptyList(),

    /** 관광지 검색창에 입력한 검색어입니다. */
    val searchQuery: String = "",

    /** 검색 결과에 적용할 카테고리이며 null이면 전체를 표시합니다. */
    val selectedCategory: TravelSpotCategory? = null,

    /** 맞춤 추천 관광지를 불러오고 있는지 나타냅니다. */
    val isSuggestedSpotsLoading: Boolean = false,

    /** 찜한 관광지를 불러오고 있는지 나타냅니다. */
    val isDibsSpotsLoading: Boolean = false,

    /** 관광지를 검색하고 있는지 나타냅니다. */
    val isSearchLoading: Boolean = false,

    /** 관광지 조회 또는 검색 중 표시할 안내 문구입니다. */
    val errorMessage: String? = null,
) {
    /** 검색어가 입력되어 검색 결과 화면을 표시해야 하는지 나타냅니다. */
    val isSearchMode: Boolean
        get() = searchQuery.isNotBlank()

    /** 현재 카테고리를 적용한 검색 결과입니다. */
    val filteredSearchResults: List<TravelSpot>
        get() = selectedCategory?.let { category ->
            searchResults.filter { spot -> spot.category == category }
        } ?: searchResults

    /** 현재 검색 모드와 선택한 탭에 따라 표시할 관광지 목록입니다. */
    val displayedSpots: List<TravelSpot>
        get() = when {
            isSearchMode -> filteredSearchResults
            selectedTab == TravelSpotSelectionTab.SUGGESTED -> suggestedSpots
            else -> dibsSpots
        }

    /** 현재 표시 대상 데이터를 불러오고 있는지 나타냅니다. */
    val isLoading: Boolean
        get() = when {
            isSearchMode -> isSearchLoading
            selectedTab == TravelSpotSelectionTab.SUGGESTED -> isSuggestedSpotsLoading
            else -> isDibsSpotsLoading
        }
}
