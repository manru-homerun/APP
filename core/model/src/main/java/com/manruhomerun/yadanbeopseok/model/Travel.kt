package com.manruhomerun.yadanbeopseok.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * 앱 내부에서 사용하는 여행 상세 모델입니다.
 *
 * 서버 ERD에서는 travel, travel_user_mapping, travel_travel_spot_mapping,
 * travel_certi 등이 나뉘어 있지만, 앱에서는 한 여행 화면을 구성하기 쉽게
 * 경기, 참여자, 일차별 장소와 테마를 하나로 묶어서 사용합니다.
 */
data class Travel(
    val id: String,
    val name: String?,
    val baseballGame: BaseballGame,
    val region: Region,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val participants: List<TravelParticipant>,
    val days: List<TravelDay>,
    val themes: List<TravelTheme> = emptyList(),
    val status: TravelStatus,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)

/**
 * 여행에 참여하는 사용자 정보입니다.
 *
 * [isLeader]를 통해 현재 참여자가 여행의 방장인지 구분합니다.
 */
data class TravelParticipant(
    val user: UserProfile,
    val isLeader: Boolean,
)

/**
 * 여행의 특정 일차와 해당 날짜에 방문할 장소 목록입니다.
 */
data class TravelDay(
    val day: Int,
    val places: List<TravelPlace>,
)

/**
 * 여행 일정에 포함된 개별 방문 장소입니다.
 *
 * 같은 관광지가 여러 일차에 포함될 수 있으므로 여행 일정상의 ID와
 * 실제 관광지 정보인 [spot]을 분리해서 관리합니다.
 */
data class TravelPlace(
    val id: String,
    val spot: TravelSpot,
    val day: Int,
    val order: Int,
    val certifications: List<TravelCertification> = emptyList(),
) {
    /**
     * 특정 사용자가 이 장소를 방문 인증했는지 확인합니다.
     */
    fun isCertifiedBy(userId: String): Boolean =
        certifications.any { certification ->
            certification.userId == userId
        }
}

/**
 * 여행지 방문 인증 정보입니다.
 *
 * ERD의 travel_certi를 앱에서 사용하기 좋은 형태로 표현합니다.
 */
data class TravelCertification(
    val id: String,
    val userId: String,
    val certificatedAt: LocalDateTime,
)

/**
 * 앱 화면에서 사용하는 여행 진행 상태입니다.
 *
 * 서버의 PLANNED 목록은 날짜를 기준으로 [UPCOMING]과 [ACTIVE]로 구분합니다.
 */
enum class TravelStatus {
    UPCOMING,
    ACTIVE,
    COMPLETED,
}

/**
 * 여행 코스 생성에 사용하는 여행 테마입니다.
 */
data class TravelTheme(
    val id: String,
    val code: String,
    val name: String,
    val displayOrder: Int,
)

/**
 * 여행 목록 API의 개별 여행을 표현하는 앱 내부 요약 모델입니다.
 *
 * 상세 일정용 [Travel]과 달리 홈 여행 카드와 완료된 여행 목록을
 * 구성하는 데 필요한 요약 정보만 보관합니다.
 *
 * @property id 여행 고유 식별자
 * @property name 여행 이름
 * @property startDate 여행 시작일
 * @property endDate 여행 종료일
 * @property baseballGameId 야구 경기 고유 식별자
 * @property homeTeam 경기의 홈팀
 * @property awayTeam 경기의 원정팀
 * @property region 여행이 진행되는 야구 여행 지역
 * @property isLeader 현재 사용자가 해당 여행의 방장인지 여부
 * @property spotsCount 여행 일정에 포함된 전체 장소 수
 * @property certificationTargetCount 방문 인증 대상 관광지 수
 * @property certifiedSpotsCount 현재 사용자가 인증한 관광지 수
 * @property hasSticker 완료된 여행에서 획득한 스티커가 있는지 여부
 */
data class TravelSummary(
    val id: String,
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val baseballGameId: String,
    val homeTeam: KboTeam,
    val awayTeam: KboTeam,
    val region: Region,
    val isLeader: Boolean,
    val spotsCount: Int,
    val certificationTargetCount: Int,
    val certifiedSpotsCount: Int,
    val hasSticker: Boolean,
)

/**
 * 여행 목록과 서버의 페이지 정보를 함께 보관하는 앱 내부 모델입니다.
 *
 * 네트워크 DTO를 화면에 직접 노출하지 않고 Repository를 통해 변환한
 * 여행 목록과 페이지 정보를 전달합니다.
 *
 * @property travels 현재 페이지에 포함된 여행 목록
 * @property pageNumber 현재 페이지 번호
 * @property pageSize 한 페이지에 포함되는 최대 여행 수
 * @property totalElements 조회 조건에 해당하는 전체 여행 수
 * @property totalPages 전체 페이지 수
 */
data class TravelListPage(
    val travels: List<TravelSummary>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
)
