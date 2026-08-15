package com.manruhomerun.yadanbeopseok.data.repository

import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSpot

/**
 * 홈에서 사용하는 인기 관광지 조회와 찜 상태 변경을 담당합니다.
 *
 * 검색, 상세 조회와 맞춤 추천 기능은 해당 기능을 개발할 때 추가합니다.
 */
interface TravelSpotRepository {
    /** 홈 화면에 노출할 지역별 인기 관광지를 조회합니다. */
    suspend fun getPopularTravelSpots(
        region: Region,
    ): List<TravelSpot>

    /**
     * 현재 사용자가 찜한 관광지 목록을 조회합니다.
     *
     * @param region 특정 지역으로 필터링하며, null이면 전체 찜 목록을 조회합니다.
     */
    suspend fun getTravelSpotDibs(
        region: Region? = null,
    ): List<TravelSpot>

    /** 지정한 관광지를 찜합니다. */
    suspend fun addTravelSpotDibs(spotId: String)

    /** 지정한 관광지의 찜을 취소합니다. */
    suspend fun deleteTravelSpotDibs(spotId: String)
}
