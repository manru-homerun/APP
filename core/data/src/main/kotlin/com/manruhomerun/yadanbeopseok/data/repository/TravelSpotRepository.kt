package com.manruhomerun.yadanbeopseok.data.repository

import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotDetail

/**
 * 관광지 조회와 찜 상태 변경을 담당합니다.
 *
 * 인기 관광지, 맞춤 추천, 검색, 상세 조회와 찜 기능을 제공합니다.
 */
interface TravelSpotRepository {
    /** 홈 화면에 노출할 지역별 인기 관광지를 조회합니다. */
    suspend fun getPopularTravelSpots(region: Region): List<TravelSpot>

    /** 선택한 지역을 기준으로 사용자 맞춤 추천 관광지를 조회합니다. */
    suspend fun getSuggestedTravelSpots(region: Region): List<TravelSpot>

    /**
     * 입력한 검색어와 일치하는 관광지를 조회합니다.
     *
     * 현재 API에는 페이지 요청 파라미터가 없으므로 응답에 포함된
     * 현재 페이지의 관광지 목록을 반환합니다.
     */
    suspend fun searchTravelSpots(searchKeyword: String): List<TravelSpot>

    /**
     * 관광지의 상세 정보와 갤러리 이미지 목록을 조회합니다.
     *
     * @param spotId 조회할 관광지의 고유 식별자
     */
    suspend fun getTravelSpotDetail(spotId: String): TravelSpotDetail

    /**
     * 현재 사용자가 찜한 관광지 목록을 조회합니다.
     *
     * @param region 특정 지역으로 필터링하며, null이면 전체 찜 목록을 조회합니다.
     */
    suspend fun getTravelSpotDibs(region: Region? = null): List<TravelSpot>

    /** 지정한 관광지를 찜합니다. */
    suspend fun addTravelSpotDibs(spotId: String)

    /** 지정한 관광지의 찜을 취소합니다. */
    suspend fun deleteTravelSpotDibs(spotId: String)
}
