package com.manruhomerun.yadanbeopseok.network.baseball.dto

import kotlinx.serialization.Serializable

/**
 * 특정 경기의 상세 응답 DTO입니다.
 *
 * 경기 종류, 취소 여부, 경기 결과와 여행 생성에 필요한
 * 전체 구장 정보를 포함합니다.
 */
@Serializable
data class BaseballGameResponseDto(
    val gameId: String,
    val dateTime: String,
    val awayTeam: BaseballTeamResponseDto,
    val homeTeam: BaseballTeamResponseDto,
    val stadium: BaseballStadiumResponseDto,
    val type: String,
    val canceled: Boolean? = null,
    val gameResult: BaseballGameResultResponseDto? = null,
)

/**
 * 구단 또는 구장별 일정에 포함되는 경기 요약 DTO입니다.
 *
 * 경기 일정 카드에 필요한 경기 시간, 구단과 구장 요약 정보를 포함합니다.
 */
@Serializable
data class BaseballGameScheduleItemResponseDto(
    val gameId: String,
    val dateTime: String,
    val awayTeam: BaseballTeamResponseDto,
    val homeTeam: BaseballTeamResponseDto,
    val stadium: BaseballStadiumSummaryResponseDto,
)

/**
 * 경기 응답에 포함된 구단 정보입니다.
 */
@Serializable
data class BaseballTeamResponseDto(
    val teamId: Long,
    val teamName: String,
    val teamLogo: String? = null,
)

/**
 * 경기 상세 응답에 포함된 전체 구장 정보입니다.
 *
 * regionCode는 SEOUL, BUSAN과 같은 서버 지역 enum 문자열입니다.
 */
@Serializable
data class BaseballStadiumResponseDto(
    val stadiumId: Long,
    val stadiumName: String,
    val regionCode: String,
    val latitude: Double,
    val longitude: Double,
)

/**
 * 경기 일정 응답에 포함된 구장 요약 정보입니다.
 */
@Serializable
data class BaseballStadiumSummaryResponseDto(
    val stadiumId: Long,
    val stadiumName: String,
)

/**
 * 종료된 경기의 최종 점수입니다.
 *
 * 경기 예정 상태에서는 상세 응답의 gameResult가 null입니다.
 */
@Serializable
data class BaseballGameResultResponseDto(
    val awayTeamScore: Int,
    val homeTeamScore: Int,
)

/**
 * 구단 또는 구장별 요청일 기준 최대 2주 경기 일정 응답입니다.
 *
 * 두 일정 API 모두 경기 목록 필드명을 contents로 반환합니다.
 */
@Serializable
data class BaseballGameScheduleResponseDto(
    val contents: List<BaseballGameScheduleItemResponseDto>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
)
