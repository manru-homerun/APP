package com.manruhomerun.yadanbeopseok.network.travel.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 여행 코스 최초 생성을 요청하는 DTO입니다.
 *
 * @property startDate 여행 시작일(yyyy-MM-dd)
 * @property endDate 여행 종료일(yyyy-MM-dd)
 * @property baseballGameId 선택한 야구 경기 ID
 * @property regionCode 여행 지역의 5자리 법정동 코드
 * @property friends 동행하는 사용자의 고유 닉네임 목록
 * @property companionConditions AI 코스 생성에 반영할 동행 조건 목록
 * @property theme 선택한 여행 테마 ID 목록
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
    val theme: List<Long>,
    val travelSpotIdList: List<Long>,
)

/**
 * 생성된 여행 코스를 최종 저장하는 요청 DTO입니다.
 *
 * @property startDate 여행 시작일(yyyy-MM-dd)
 * @property endDate 여행 종료일(yyyy-MM-dd)
 * @property baseballGame 여행 일정에 포함된 야구 경기 배치 정보
 * @property name 사용자가 확정한 여행 이름
 * @property regionCode 여행 지역의 5자리 법정동 코드
 * @property friends 동행하는 사용자의 고유 닉네임 목록
 * @property theme 선택한 여행 테마 ID 목록
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
    val theme: List<Long>,
    val schedule: List<TravelScheduleDayRequestDto>,
)

/**
 * 여행 저장 요청에 포함되는 야구 경기 배치 정보입니다.
 */
@Serializable
data class TravelBaseballGameRequestDto(
    val id: Long,
    val day: Int,
    val baseballGameAfterIdx: Int,
)

/**
 * 여행 저장 요청에 포함되는 일차별 관광지 일정입니다.
 */
@Serializable
data class TravelScheduleDayRequestDto(
    val day: Int,
    val travelSpotIdList: List<Long>,
)
