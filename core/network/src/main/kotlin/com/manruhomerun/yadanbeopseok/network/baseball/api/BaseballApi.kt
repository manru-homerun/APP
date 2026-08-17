package com.manruhomerun.yadanbeopseok.network.baseball.api

import com.manruhomerun.yadanbeopseok.network.baseball.dto.BaseballGameResponseDto
import com.manruhomerun.yadanbeopseok.network.baseball.dto.BaseballGameScheduleResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 야단법석 백엔드의 KBO 경기 API를 정의합니다.
 *
 * 인증이 필요한 요청에는 AuthInterceptor가 야단법석 Access Token을
 * Authorization 헤더에 자동으로 추가합니다.
 */
interface BaseballApi {
    /**
     * 경기 ID에 해당하는 경기 상세 정보를 조회합니다.
     *
     * @param gameId 조회할 경기의 고유 식별자
     */
    @GET("baseball/{gameId}")
    suspend fun getGame(@Path("gameId") gameId: String): BaseballGameResponseDto

    /**
     * 특정 구단의 요청일 기준 최대 2주 경기 일정을 조회합니다.
     *
     * @param teamId 서버에서 사용하는 구단 ID
     */
    @GET("baseball/teams/{teamId}/game-schedule")
    suspend fun getTeamGameSchedule(@Path("teamId") teamId: Long): BaseballGameScheduleResponseDto

    /**
     * 특정 구장의 요청일 기준 최대 2주 경기 일정을 조회합니다.
     *
     * @param stadiumId 서버에서 사용하는 구장 ID
     */
    @GET("baseball/stadiums/{stadiumId}/game-schedule")
    suspend fun getStadiumGameSchedule(@Path("stadiumId") stadiumId: Long): BaseballGameScheduleResponseDto
}
