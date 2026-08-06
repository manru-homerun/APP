package com.manruhomerun.yadanbeopseok.home.viewmodel

import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.Travel
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import com.manruhomerun.yadanbeopseok.model.TravelStatus

/**
 * 홈 화면 A·01~A·04에서 사용하는 전체 UI 상태입니다.
 *
 * 여행 목록, 인기 관광지, 사용자의 필터 선택과 네트워크 요청 상태를 관리합니다.
 */
data class HomeUiState(
    /** 여행 카드의 방장·동행자 여부를 판단할 현재 사용자 ID입니다. */
    val currentUserId: String? = null,

    /** 서버에서 조회한 전체 여행 목록입니다. */
    val travels: List<Travel> = emptyList(),

    /** 현재 지역과 카테고리를 기준으로 조회한 인기 관광지 목록입니다. */
    val popularTravelSpots: List<TravelSpot> = emptyList(),

    /** 홈 관광지 추천에 적용할 지역입니다. */
    val selectedRegion: Region = Region.BUSAN,

    /** 홈 관광지 추천에 적용할 카테고리입니다. */
    val selectedCategory: TravelSpotCategory = TravelSpotCategory.ACCOMMODATION,

    /** 읽지 않은 알림이 있는지 나타냅니다. */
    val hasUnreadNotifications: Boolean = false,

    /** 홈의 최초 데이터를 불러오는 중인지 나타냅니다. */
    val isLoading: Boolean = true,

    /** 인기 관광지 새로고침이 진행 중인지 나타냅니다. */
    val isRefreshing: Boolean = false,

    /** 찜 상태 변경 요청이 진행 중인 관광지 ID 목록입니다. */
    val updatingFavoriteSpotIds: Set<String> = emptySet(),

    /** 홈 데이터 조회 또는 변경 중 발생한 사용자 안내 문구입니다. */
    val errorMessage: String? = null,
) {
    /**
     * 홈의 내 원정 여행 영역에 표시할 여행 목록입니다.
     *
     * 완료된 여행은 기록 화면에서 표시하므로 홈에서는 제외합니다.
     * 서버가 내려준 여행 순서는 그대로 유지합니다.
     */
    val displayedTravels: List<Travel>
        get() =
            travels.filter { travel ->
                travel.status == TravelStatus.ACTIVE ||
                    travel.status == TravelStatus.UPCOMING
            }

    /** 홈에 표시할 진행 중 또는 예정 여행이 있는지 나타냅니다. */
    val hasTravels: Boolean
        get() = displayedTravels.isNotEmpty()
}
