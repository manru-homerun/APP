package com.manruhomerun.yadanbeopseok.network.travel.dto

import kotlinx.serialization.Serializable

/**
 * 여행 목록에 포함되는 야구 경기 요약 응답 DTO입니다.
 *
 * @property id 야구 경기 고유 식별자
 * @property homeTeamId 서버에서 사용하는 홈팀 ID
 * @property awayTeamId 서버에서 사용하는 원정팀 ID
 */
@Serializable
data class TravelBaseballGameResponseDto(
    val id: Long,
    val homeTeamId: Long,
    val awayTeamId: Long,
)
