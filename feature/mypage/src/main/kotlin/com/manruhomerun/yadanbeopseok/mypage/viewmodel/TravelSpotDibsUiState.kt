package com.manruhomerun.yadanbeopseok.mypage.viewmodel

import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotFilterCategory

/**
 * H·04 찜한 관광지 화면의 상태입니다.
 *
 * @property dibsSpots 선택한 지역과 카테고리에서 현재 사용자가 찜한 관광지
 * @property selectedRegion 서버 조회에 사용할 지역
 * @property selectedCategory 서버 조회에 사용할 관광지 카테고리이며 null이면 전체
 * @property pageNumber 마지막으로 불러온 페이지 번호
 * @property totalPages 전체 페이지 수
 * @property isLoading 첫 페이지를 조회하는 중인지 여부
 * @property isLoadingMore 다음 페이지를 조회하는 중인지 여부
 * @property updatingDibsSpotIds 찜 취소 요청을 처리 중인 관광지 ID
 * @property errorMessage 사용자에게 표시할 안전한 오류 문구
 * @property loadMoreErrorMessage 다음 페이지 조회 실패 문구
 */
data class TravelSpotDibsUiState(
    val dibsSpots: List<TravelSpot> = emptyList(),
    val selectedRegion: Region = Region.entries.first(),
    val selectedCategory: TravelSpotFilterCategory? = null,
    val pageNumber: Int = 0,
    val totalPages: Int = 0,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val updatingDibsSpotIds: Set<String> = emptySet(),
    val errorMessage: String? = null,
    val loadMoreErrorMessage: String? = null,
) {
    val hasNextPage: Boolean
        get() = pageNumber < totalPages
}
