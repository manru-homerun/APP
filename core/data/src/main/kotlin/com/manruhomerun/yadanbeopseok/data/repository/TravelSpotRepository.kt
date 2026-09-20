package com.manruhomerun.yadanbeopseok.data.repository

import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelCompanionCondition
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotDetail
import com.manruhomerun.yadanbeopseok.model.TravelSpotFilterCategory
import com.manruhomerun.yadanbeopseok.model.TravelSpotListPage
import kotlinx.datetime.LocalDate

/**
 * 관광지 조회와 찜 상태 변경을 담당합니다.
 *
 * 인기 관광지, 맞춤 추천, 검색, 상세 조회와 찜 기능을 제공합니다.
 */
interface TravelSpotRepository {
    /** 홈 화면에 노출할 지역별 인기 관광지를 조회하며 카테고리가 null이면 전체를 반환합니다. */
    suspend fun getPopularTravelSpots(
        region: Region,
        category: TravelSpotFilterCategory? = null,
    ): List<TravelSpot>

    /** 여행 조건과 현재 일정에 맞는 사용자 맞춤 추천 관광지를 조회합니다. */
    suspend fun getSuggestedTravelSpots(params: SuggestTravelSpotsParams): List<TravelSpot>

    /**
     * 입력한 검색어와 일치하는 관광지를 조회합니다.
     *
     * 서버 기본 페이지에 포함된 선택 지역의 관광지 목록을 반환합니다.
     */
    suspend fun searchTravelSpots(searchKeyword: String, region: Region): List<TravelSpot>

    /**
     * 관광지의 상세 정보와 갤러리 이미지 목록을 조회합니다.
     *
     * @param spotId 조회할 관광지의 고유 식별자
     */
    suspend fun getTravelSpotDetail(spotId: String): TravelSpotDetail

    /**
     * 현재 사용자가 찜한 관광지 목록을 조회합니다.
     *
     * @param region 필터링할 지역
     * @param category 필터링할 관광지 카테고리이며 null이면 전체를 조회함
     */
    suspend fun getTravelSpotDibs(
        region: Region,
        category: TravelSpotFilterCategory? = null,
        pageNumber: Int = 1,
        pageSize: Int = 10,
    ): TravelSpotListPage

    /** 지정한 관광지를 찜합니다. */
    suspend fun addTravelSpotDibs(spotId: String)

    /** 지정한 관광지의 찜을 취소합니다. */
    suspend fun deleteTravelSpotDibs(spotId: String)
}

/**
 * 맞춤 관광지 추천 요청에 필요한 앱 내부 입력값입니다.
 *
 * @property startDate 여행 시작일
 * @property endDate 여행 종료일
 * @property region 여행 지역
 * @property companionConditions 추천에 반영할 동행 조건 목록
 * @property companionCount 본인을 제외한 동행자 수
 * @property themeId 선택한 여행 테마 ID
 * @property travelSpotIds 이미 선택했거나 일정에 포함된 관광지 ID 목록
 */
data class SuggestTravelSpotsParams(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val region: Region,
    val companionConditions: List<TravelCompanionCondition>,
    val companionCount: Int,
    val themeId: String,
    val travelSpotIds: List<String>,
)
