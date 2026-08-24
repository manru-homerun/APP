package com.manruhomerun.yadanbeopseok.network.travel.api

import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.network.common.dto.ApiResponseDto
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelSpotDetailResponseDto
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelSpotDibsRequestDto
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelSpotListResponseDto
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelSpotResponseDto
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelSpotSearchResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 야단법석 백엔드의 관광지 조회 및 찜 API를 정의합니다.
 *
 * 인증이 필요한 요청에는 AuthInterceptor가 야단법석 Access Token을
 * Authorization 헤더에 자동으로 추가합니다.
 */
interface TravelSpotApi {
    /**
     * 선택한 지역을 기준으로 사용자 맞춤 추천 관광지를 조회합니다.
     */
    @GET("travel/spots/suggestion")
    suspend fun getSuggestedTravelSpots(
        @Query("regionCode") region: Region,
    ): ApiResponseDto<List<TravelSpotResponseDto>>

    /**
     * 입력한 검색어와 일치하는 관광지를 검색합니다.
     */
    @GET("travel/spots")
    suspend fun searchTravelSpots(
        @Query("searchKeyword") searchKeyword: String,
    ): ApiResponseDto<TravelSpotSearchResponseDto>

    /**
     * 관광지 ID에 해당하는 상세 정보를 조회합니다.
     */
    @GET("travel/spots/{spotId}")
    suspend fun getTravelSpotDetail(
        @Path("spotId") spotId: Long,
    ): ApiResponseDto<TravelSpotDetailResponseDto>

    /**
     * 관광지 상세 화면의 갤러리에 표시할 이미지 목록을 조회합니다.
     */
    @GET("travel/spots/{spotId}/images")
    suspend fun getTravelSpotImages(
        @Path("spotId") spotId: Long,
    ): ApiResponseDto<List<String>>

    /**
     * 현재 사용자가 찜한 관광지 목록을 조회합니다.
     *
     * @param region 필터링할 지역이며, null이면 전체를 조회합니다.
     */
    @GET("travel/spots/dibs")
    suspend fun getTravelSpotDibs(
        @Query("regionCode") region: Region? = null,
    ): ApiResponseDto<List<TravelSpotResponseDto>>

    /**
     * 특정 관광지를 찜합니다.
     *
     * 성공 응답은 201 Created이며 응답 데이터가 없습니다.
     */
    @POST("travel/spots/dibs")
    suspend fun addTravelSpotDibs(
        @Body request: TravelSpotDibsRequestDto,
    )

    /**
     * 특정 관광지의 찜을 취소합니다.
     *
     * 성공 응답은 204 No Content입니다.
     */
    @HTTP(
        method = "DELETE",
        path = "travel/spots/dibs",
        hasBody = true,
    )
    suspend fun deleteTravelSpotDibs(
        @Body request: TravelSpotDibsRequestDto,
    )

    /**
     * 홈 화면에 표시할 지역별 인기 관광지를 조회합니다.
     */
    @GET("travel/spots/popular")
    suspend fun getPopularTravelSpots(
        @Query("regionCode") region: Region,
    ): ApiResponseDto<TravelSpotListResponseDto>
}
