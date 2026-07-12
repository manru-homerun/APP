package com.manruhomerun.yadanbeopseok.data.repository

import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory

interface TravelSpotRepository {
    /**
     * 지역, 검색어, 카테고리를 기준으로 관광지를 검색합니다.
     */
    suspend fun searchTravelSpots(
        region: Region,
        keyword: String,
        category: TravelSpotCategory? = null,
    ): List<TravelSpot>

    /**
     * 특정 관광지의 상세 정보를 조회합니다.
     */
    suspend fun getTravelSpot(
        spotId: String,
    ): TravelSpot

    /**
     * 특정 관광지의 이미지 목록을 조회합니다.
     */
    suspend fun getTravelSpotImages(
        spotId: String,
    ): List<String>

    /**
     * 홈 화면에 노출할 지역별 인기 관광지를 조회합니다.
     */
    suspend fun getPopularTravelSpots(
        region: Region,
    ): List<TravelSpot>

    /**
     * 여행 만들기 화면에서 사용할 사용자 맞춤 추천 관광지를 조회합니다.
     */
    suspend fun getSuggestedTravelSpots(
        region: Region,
    ): List<TravelSpot>

    /**
     * 내가 찜한 관광지 목록을 조회합니다.
     */
    suspend fun getLikedTravelSpots(): List<TravelSpot>

    /**
     * 특정 관광지를 찜합니다.
     */
    suspend fun likeTravelSpot(
        spotId: String,
    ): TravelSpot

    /**
     * 특정 관광지 찜을 취소합니다.
     */
    suspend fun unlikeTravelSpot(
        spotId: String,
    ): TravelSpot
}
