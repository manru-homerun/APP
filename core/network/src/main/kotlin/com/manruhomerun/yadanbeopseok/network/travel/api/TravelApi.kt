package com.manruhomerun.yadanbeopseok.network.travel.api

import com.manruhomerun.yadanbeopseok.network.common.dto.ApiResponseDto
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelCourseGenerateRequestDto
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelCourseResponseDto
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelCreateRequestDto
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelDetailResponseDto
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelListResponseDto
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelThemeResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 야단법석 백엔드의 여행 조회 및 여행 생성 API를 정의합니다.
 *
 * 인증이 필요한 요청에는 AuthInterceptor가 야단법석 Access Token을
 * Authorization 헤더에 자동으로 추가합니다.
 */
interface TravelApi {
    /**
     * 상태를 기준으로 현재 사용자의 여행 목록을 조회합니다.
     *
     * 홈에서는 PLANNED를 전달하여 진행 중 여행과 예정 여행을
     * 함께 조회합니다.
     */
    @GET("travel")
    suspend fun getTravels(
        @Query("status") status: String,
    ): ApiResponseDto<TravelListResponseDto>

    /**
     * 여행 ID에 해당하는 일차별 상세 일정을 조회합니다.
     */
    @GET("travel/{travelId}")
    suspend fun getTravel(
        @Path("travelId") travelId: String,
    ): ApiResponseDto<TravelDetailResponseDto>

    /**
     * 여행 만들기에서 선택할 수 있는 여행 테마 목록을 조회합니다.
     */
    @GET("travel/themes")
    suspend fun getTravelThemes(): ApiResponseDto<List<TravelThemeResponseDto>>

    /**
     * 선택한 경기, 날짜, 테마, 동행자와 관광지를 바탕으로
     * 최초 여행 코스를 생성합니다.
     */
    @POST("travel/generate")
    suspend fun generateTravelCourse(
        @Body request: TravelCourseGenerateRequestDto,
    ): ApiResponseDto<TravelCourseResponseDto>

    /**
     * 생성된 여행 코스를 최종 저장합니다.
     *
     * 성공하면 서버는 201 Created를 반환하며 응답 Body는 없습니다.
     */
    @POST("travel")
    suspend fun createTravel(
        @Body request: TravelCreateRequestDto,
    )
}
