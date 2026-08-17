package com.manruhomerun.yadanbeopseok.data.repository

import com.manruhomerun.yadanbeopseok.model.BaseballGame
import com.manruhomerun.yadanbeopseok.model.BaseballGameSummary
import com.manruhomerun.yadanbeopseok.model.KboTeam

/**
 * KBO 경기 상세와 경기 일정을 제공하는 Repository입니다.
 */
interface BaseballRepository {
    /**
     * 특정 야구 경기의 상세 정보를 조회합니다.
     *
     * 여행 생성에 필요한 구장 지역과 좌표를 포함합니다.
     */
    suspend fun getGame(gameId: String): BaseballGame

    /**
     * 특정 구단의 요청일 기준 최대 2주 경기 일정을 조회합니다.
     */
    suspend fun getTeamGameSchedule(team: KboTeam): List<BaseballGameSummary>

    /**
     * 특정 구장의 요청일 기준 최대 2주 경기 일정을 조회합니다.
     */
    suspend fun getStadiumGameSchedule(stadiumId: String): List<BaseballGameSummary>
}
