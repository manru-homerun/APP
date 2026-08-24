package com.manruhomerun.yadanbeopseok.data.repository

import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.Travel
import com.manruhomerun.yadanbeopseok.model.TravelCompanionCondition
import com.manruhomerun.yadanbeopseok.model.TravelCourse
import com.manruhomerun.yadanbeopseok.model.TravelListPage
import com.manruhomerun.yadanbeopseok.model.TravelTheme
import kotlinx.datetime.LocalDate

/**
 * 여행 조회와 여행 생성에 필요한 데이터를 제공하는 Repository입니다.
 *
 * Network DTO를 외부에 노출하지 않고 앱 내부 여행 모델로 변환해 제공합니다.
 */
interface TravelRepository {
    /**
     * 진행 중 여행과 진행 예정 여행 목록을 함께 조회합니다.
     *
     * 구현체는 여행 목록 API에 PLANNED 상태를 전달합니다.
     * 각 여행의 진행 중·예정 여부는 시작일과 종료일을 기준으로
     * 홈 계층에서 구분합니다.
     */
    suspend fun getPlannedTravels(): TravelListPage

    /**
     * 여행 ID에 해당하는 상세 일정과 방문 인증 상태를 조회합니다.
     *
     * @param travelId 조회할 여행의 고유 식별자
     */
    suspend fun getTravel(travelId: String): Travel

    /**
     * 여행 만들기에서 선택할 수 있는 여행 테마 목록을 조회합니다.
     */
    suspend fun getTravelThemes(): List<TravelTheme>

    /**
     * 여행 만들기에서 입력한 조건을 바탕으로 최초 여행 코스를 생성합니다.
     *
     * @param params 경기, 날짜, 지역, 동행자, 테마와 관광지 선택 정보
     */
    suspend fun generateTravelCourse(params: GenerateTravelCourseParams): TravelCourse

    /**
     * 사용자가 최종 확정한 여행 코스를 서버에 저장합니다.
     *
     * 서버는 저장 성공 시 201 Created를 반환하며 별도 응답 데이터는 없습니다.
     *
     * @param params 여행 기본 정보와 최종 여행 코스
     */
    suspend fun createTravel(params: CreateTravelParams)
}

/**
 * 여행 코스 생성 요청에 필요한 앱 내부 입력값입니다.
 *
 * Repository 구현체가 서버 요청 DTO로 변환합니다.
 *
 * @property startDate 여행 시작일
 * @property endDate 여행 종료일
 * @property baseballGameId 선택한 야구 경기 ID
 * @property region 여행 지역
 * @property friendNicknames 동행하는 사용자의 고유 닉네임 목록
 * @property companionConditions AI 코스 생성에 반영할 동행 조건 목록
 * @property themeIds 선택한 여행 테마 ID 목록
 * @property travelSpotIds 일정에 반드시 포함할 관광지 ID 목록
 */
data class GenerateTravelCourseParams(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val baseballGameId: String,
    val region: Region,
    val friendNicknames: List<String>,
    val companionConditions: List<TravelCompanionCondition>,
    val themeIds: List<String>,
    val travelSpotIds: List<String>,
)

/**
 * 생성된 여행 코스를 최종 저장할 때 사용하는 앱 내부 입력값입니다.
 *
 * [course]의 경기 배치 정보와 일차별 관광지 일정을 서버 요청 DTO로 변환합니다.
 *
 * @property startDate 여행 시작일
 * @property endDate 여행 종료일
 * @property name 최종 여행 이름
 * @property region 여행 지역
 * @property friendNicknames 동행하는 사용자의 고유 닉네임 목록
 * @property themeIds 선택한 여행 테마 ID 목록
 * @property course 최종 확정한 여행 코스
 */
data class CreateTravelParams(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val name: String,
    val region: Region,
    val friendNicknames: List<String>,
    val themeIds: List<String>,
    val course: TravelCourse,
)
