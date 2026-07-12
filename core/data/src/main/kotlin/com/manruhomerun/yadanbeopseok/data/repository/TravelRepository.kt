package com.manruhomerun.yadanbeopseok.data.repository

import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.Travel
import com.manruhomerun.yadanbeopseok.model.TravelStatus
import com.manruhomerun.yadanbeopseok.model.TravelTheme
import kotlinx.datetime.LocalDate

interface TravelRepository {
    /**
     * 내 여행 목록을 조회합니다.
     *
     * status가 null이면 진행 예정, 진행 중, 완료 여행을 모두 조회합니다.
     */
    suspend fun getTravels(
        status: TravelStatus? = null,
    ): List<Travel>

    /**
     * 특정 여행의 상세 일정을 조회합니다.
     *
     * 여행 상세 화면, 여행 편집 화면, 완료된 여행 기록 상세 화면에서 사용합니다.
     */
    suspend fun getTravel(
        travelId: String,
    ): Travel

    /**
     * 선택한 경기, 여행 기간, 동행자, 테마, 필수 방문지를 기준으로
     * 추천 여행 코스를 생성합니다.
     *
     * 아직 저장된 여행이 없는 상태에서 서버에 추천 코스 생성을 요청하는 기능입니다.
     */
    suspend fun generateTravelCourse(
        params: TravelCourseGenerateParams,
    ): Travel

    /**
     * 추천받은 여행 코스 또는 사용자가 편집한 여행 코스를 최종 저장합니다.
     */
    suspend fun saveTravelCourse(
        travel: Travel,
    ): Travel

    /**
     * 현재 여행지 목록을 기준으로 서버에 최적 방문 순서 재정렬을 요청합니다.
     */
    suspend fun alignTravelCourse(
        params: TravelCourseAlignParams,
    ): Travel

    /**
     * 기존 여행의 이름, 날짜, 여행지 구성, 방문 순서 등을 수정합니다.
     */
    suspend fun updateTravel(
        travelId: String,
        travel: Travel,
    ): Travel

    /**
     * 여행 생성 화면에서 선택 가능한 여행 테마 목록을 조회합니다.
     */
    suspend fun getTravelThemes(): List<TravelTheme>
}

/**
 * 여행 코스 최초 생성에 필요한 앱 내부 입력 모델입니다.
 *
 * API request DTO가 아니라 Repository 호출용 params입니다.
 */
data class TravelCourseGenerateParams(
    val baseballGameId: String,
    val region: Region,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val companionUserIds: List<String>,
    val themeIds: List<String>,
    val requiredSpotIds: List<String> = emptyList(),
)

/**
 * 여행 코스 재정렬에 필요한 앱 내부 입력 모델입니다.
 *
 * spotIds는 현재 코스에 담긴 여행지 id 목록입니다.
 */
data class TravelCourseAlignParams(
    val travelId: String,
    val spotIds: List<String>,
)
