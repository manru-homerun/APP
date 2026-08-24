package com.manruhomerun.yadanbeopseok.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * 여행 상세 조회 결과를 앱 내부에서 사용하는 모델입니다.
 *
 * 서버 응답에 포함되지 않는 값은 임의로 생성하지 않습니다.
 * [id]는 상세 조회에 사용한 Path Variable의 travelId를 사용합니다.
 *
 * @property id 여행 고유 식별자
 * @property startDate 여행 시작일
 * @property endDate 여행 종료일
 * @property baseballGame 여행에 포함된 야구 경기 정보
 * @property name 여행 이름
 * @property region 여행 지역
 * @property friends 함께 여행하는 사용자들의 닉네임
 * @property isLeader 현재 사용자가 여행의 방장인지 여부
 * @property themeIds 여행에 적용된 테마 ID 목록
 * @property certificationTargetCount 방문 인증 대상 관광지 수
 * @property certifiedSpotsCount 현재 사용자가 인증한 관광지 수
 * @property days 일차별 여행 일정
 * @property status 날짜를 기준으로 계산한 여행 상태
 */
data class Travel(
    val id: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val baseballGame: TravelBaseballGame,
    val name: String?,
    val region: Region,
    val friends: List<String>,
    val isLeader: Boolean,
    val themeIds: List<String>,
    val certificationTargetCount: Int,
    val certifiedSpotsCount: Int,
    val days: List<TravelDay>,
    val status: TravelStatus,
)

/**
 * 여행 상세 응답에 포함되는 야구 경기 정보입니다.
 *
 * 완전한 경기 정보가 필요한 화면에서는 [id]를 사용해
 * 경기 상세 API를 별도로 조회합니다.
 *
 * @property id 야구 경기 고유 식별자
 * @property day 여행 일정에서 경기가 포함된 일차
 * @property baseballGameAfterIdx 일차별 일정에서 야구 경기 배치 위치를 나타내는 인덱스
 */
data class TravelBaseballGame(
    val id: String,
    val day: Int,
    val baseballGameAfterIdx: Int,
)

/**
 * 여행의 특정 일차와 해당 일차에 방문할 장소 목록입니다.
 *
 * @property day 여행 일차
 * @property places 해당 일차의 방문 장소 목록
 */
data class TravelDay(
    val day: Int,
    val places: List<TravelPlace>,
)

/**
 * 여행 일정에 포함된 개별 방문 장소입니다.
 *
 * 별도의 여행 일정 항목 ID는 API가 제공하지 않으므로
 * 실제 관광지 정보는 [spot]을 통해 식별합니다.
 *
 * [order]는 서버 응답 배열의 순서를 기준으로 Mapper에서 부여합니다.
 *
 * @property spot 일정에 포함된 관광지
 * @property order 해당 일차 안에서의 표시 순서
 * @property isCertificationTarget 방문 인증 대상인지 여부
 * @property isCertified 현재 사용자가 방문 인증을 완료했는지 여부
 */
data class TravelPlace(
    val spot: TravelSpot,
    val order: Int,
    val isCertificationTarget: Boolean = false,
    val isCertified: Boolean = false,
)

/**
 * 여행지 방문 인증 결과입니다.
 *
 * 방문 인증 API가 성공했을 때 인증된 사용자와 시각을 보관합니다.
 */
data class TravelCertification(
    val id: String,
    val userId: String,
    val certificatedAt: LocalDateTime,
)

/**
 * 앱 화면에서 사용하는 여행 진행 상태입니다.
 *
 * 서버의 PLANNED 목록과 여행 상세 응답은 시작일, 종료일을 기준으로
 * [UPCOMING], [ACTIVE], [COMPLETED] 상태로 변환합니다.
 */
enum class TravelStatus {
    UPCOMING,
    ACTIVE,
    COMPLETED,
}

/**
 * 생성 또는 재정렬된 저장 전 여행 코스입니다.
 *
 * 기존 여행 일정 하위 모델을 재사용하며, 서버에 저장되기 전이므로
 * 여행 ID, 진행 상태와 방문 인증 정보는 포함하지 않습니다.
 *
 * @property baseballGame 코스에 포함된 야구 경기의 배치 정보
 * @property days 일차별 추천 일정
 */
data class TravelCourse(
    val baseballGame: TravelBaseballGame,
    val days: List<TravelDay>,
)

/**
 * 여행 코스 생성에 사용하는 여행 테마입니다.
 *
 * @property id 서버에서 사용하는 여행 테마 ID
 * @property name 화면에 표시할 여행 테마 이름
 */
data class TravelTheme(
    val id: String,
    val name: String,
)

/**
 * 여행 코스를 만들 때 고려할 동행 조건입니다.
 *
 * 서버 요청에서는 각 Enum 이름을 동행 조건 문자열로 사용합니다.
 */
enum class TravelCompanionCondition {
    /** 유아차와 수유실 등 아이 동반 조건을 고려합니다. */
    CHILD,

    /** 계단이 적고 이동이 완만한 어르신 동반 조건을 고려합니다. */
    SENIOR,

    /** 휠체어 접근이 가능한 동선과 관광지를 고려합니다. */
    WHEELCHAIR,
}

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
