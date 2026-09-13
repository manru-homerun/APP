package com.manruhomerun.yadanbeopseok.mypage.viewmodel

import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotFilterCategory

/**
 * H·04 찜한 관광지 화면의 상태입니다.
 *
 * @property dibsSpots 선택한 지역과 카테고리에서 현재 사용자가 찜한 관광지
 * @property selectedRegion 서버 조회에 사용할 지역
 * @property selectedCategory 서버 조회에 사용할 관광지 카테고리
 * @property isLoading 찜 목록을 조회하는 중인지 여부
 * @property updatingDibsSpotIds 찜 취소 요청을 처리 중인 관광지 ID
 * @property errorMessage 사용자에게 표시할 안전한 오류 문구
 */
data class TravelSpotDibsUiState(
    val dibsSpots: List<TravelSpot> = emptyList(),
    val selectedRegion: Region = Region.BUSAN,
    val selectedCategory: TravelSpotFilterCategory = TravelSpotFilterCategory.ACCOMMODATION,
    val isLoading: Boolean = true,
    val updatingDibsSpotIds: Set<String> = emptySet(),
    val errorMessage: String? = null,
)
