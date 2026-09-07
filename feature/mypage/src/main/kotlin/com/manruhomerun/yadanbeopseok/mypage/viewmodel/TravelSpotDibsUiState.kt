package com.manruhomerun.yadanbeopseok.mypage.viewmodel

import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory

/**
 * H·04 찜한 관광지 화면의 상태입니다.
 *
 * @property dibsSpots 현재 사용자가 찜한 전체 관광지
 * @property selectedCategory 선택한 카테고리이며, null이면 전체
 * @property isLoading 찜 목록을 조회하는 중인지 여부
 * @property updatingDibsSpotIds 찜 취소 요청을 처리 중인 관광지 ID
 * @property errorMessage 사용자에게 표시할 안전한 오류 문구
 */
data class TravelSpotDibsUiState(
    val dibsSpots: List<TravelSpot> = emptyList(),
    val selectedCategory: TravelSpotCategory? = null,
    val isLoading: Boolean = true,
    val updatingDibsSpotIds: Set<String> = emptySet(),
    val errorMessage: String? = null,
) {
    /** 선택한 카테고리에 맞춰 화면에 표시할 관광지를 반환합니다. */
    val displayedTravelSpots: List<TravelSpot>
        get() {
            val category = selectedCategory ?: return dibsSpots

            return dibsSpots.filter { travelSpot ->
                travelSpot.category == category
            }
        }
}
