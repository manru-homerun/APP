package com.manruhomerun.yadanbeopseok.network.travel.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 사용자 조건과 현재 일정으로 맞춤 관광지를 추천받는 요청 DTO입니다.
 *
 * @property startDate 여행 시작일(yyyy-MM-dd)
 * @property endDate 여행 종료일(yyyy-MM-dd)
 * @property regionCode 여행 지역의 5자리 법정동 코드
 * @property companionConditions AI 추천에 반영할 동행 조건 목록
 * @property companionCount 본인을 제외한 동행자 수
 * @property theme 선택한 여행 테마 ID
 * @property travelSpotIdList 이미 선택했거나 일정에 포함된 숫자형 관광지 ID 목록
 */
@Serializable
data class TravelSpotSuggestionRequestDto(
    @SerialName("from")
    val startDate: String,
    @SerialName("to")
    val endDate: String,
    val regionCode: String,
    val companionConditions: List<String>,
    val companionCount: Int,
    val theme: Long,
    val travelSpotIdList: List<Long>,
)

/**
 * 여행 코스 최초 생성을 요청하는 DTO입니다.
 *
 * @property startDate 여행 시작일(yyyy-MM-dd)
 * @property endDate 여행 종료일(yyyy-MM-dd)
 * @property baseballGameId 선택한 야구 경기 ID
 * @property regionCode 여행 지역의 5자리 법정동 코드
 * @property friends 동행하는 사용자의 UUID 목록
 * @property companionConditions AI 코스 생성에 반영할 동행 조건 목록
 * @property theme 선택한 여행 테마 ID
 * @property travelSpotIdList 일정에 반드시 포함할 관광지 ID 목록
 */
@Serializable
data class TravelCourseGenerateRequestDto(
    @SerialName("from")
    val startDate: String,
    @SerialName("to")
    val endDate: String,
    val baseballGameId: Long,
    val regionCode: String,
    val friends: List<String>,
    val companionConditions: List<String>,
    val theme: Long,
    val travelSpotIdList: List<String>,
)

/**
 * 생성된 여행 코스를 최종 저장하는 요청 DTO입니다.
 *
 * @property startDate 여행 시작일(yyyy-MM-dd)
 * @property endDate 여행 종료일(yyyy-MM-dd)
 * @property baseballGame 여행 일정에 포함된 야구 경기 배치 정보
 * @property name 사용자가 확정한 여행 이름
 * @property regionCode 여행 지역의 5자리 법정동 코드
 * @property friends 동행하는 사용자의 UUID 목록
 * @property theme 선택한 여행 테마 ID
 * @property schedule 최종 확정된 일차별 관광지 일정
 */
@Serializable
data class TravelCreateRequestDto(
    @SerialName("from")
    val startDate: String,
    @SerialName("to")
    val endDate: String,
    val baseballGame: TravelBaseballGameRequestDto,
    val name: String,
    val regionCode: String,
    val friends: List<String>,
    val theme: Long,
    val schedule: List<TravelScheduleDayRequestDto>,
)

/**
 * 여행 코스의 관광지 순서를 거리 기준으로 재정렬하는 요청 DTO입니다.
 *
 * @property startDate 여행 시작일(yyyy-MM-dd)
 * @property endDate 여행 종료일(yyyy-MM-dd)
 * @property baseballGame 여행 일정에 포함된 야구 경기 배치 정보
 * @property schedule 재정렬할 일차별 관광지 일정
 */
@Serializable
data class TravelCourseAlignRequestDto(
    @SerialName("from")
    val startDate: String,
    @SerialName("to")
    val endDate: String,
    val baseballGame: TravelBaseballGameRequestDto,
    val schedule: List<TravelScheduleDayRequestDto>,
)

/**
 * 저장된 여행의 이름, 경기 위치와 일차별 관광지 일정을 수정하는 요청 DTO입니다.
 *
 * 기존 여행의 경기와 날짜는 변경하지 않습니다. 서버는 [baseballGameAfterIdx]를
 * 기존 경기 일차 안에서의 0부터 시작하는 삽입 위치로 사용합니다.
 *
 * @property name 수정할 여행 이름
 * @property baseballGameAfterIdx 경기 일차 안에서 야구 경기가 삽입될 위치
 * @property schedule 수정할 일차별 관광지 일정
 */
@Serializable
data class TravelUpdateRequestDto(
    val name: String,
    val baseballGameAfterIdx: Int,
    val schedule: List<TravelScheduleDayRequestDto>,
)

/**
 * 여행 생성과 재정렬 요청에 포함되는 야구 경기 배치 정보입니다.
 */
@Serializable
data class TravelBaseballGameRequestDto(
    val id: Long,
    val baseballGameAfterIdx: Int,
)

/**
 * 여행 요청에 포함되는 일차별 관광지 일정입니다.
 *
 * @property day 여행 일차
 * @property travelSpotIdList 방문 순서대로 정렬된 관광지 ID 목록
 */
@Serializable
data class TravelScheduleDayRequestDto(
    val day: Int,
    val travelSpotIdList: List<String>,
)

/**
 * 여행 일정에 포함된 관광지의 방문 인증 요청입니다.
 *
 * 여행 ID와 관광지 ID는 URL 경로로 전달하므로 본문에 포함하지 않습니다.
 *
 * @property latitude 사용자의 현재 위도
 * @property longitude 사용자의 현재 경도
 * @property accuracy GPS 위치 정확도(m)
 * @property day 인증할 관광지가 포함된 여행 일차
 * @property placementOrder 해당 일차 안에서 인증할 관광지의 배치 순서
 */
@Serializable
data class TravelSpotVerifyRequestDto(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double,
    val day: Int,
    val placementOrder: Int,
)
