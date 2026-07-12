package com.manruhomerun.yadanbeopseok.data.repository

import com.manruhomerun.yadanbeopseok.model.BaseballGame
import com.manruhomerun.yadanbeopseok.model.KboTeam
import kotlinx.datetime.LocalDate

interface BaseballRepository {
    /**
     * 특정 야구 경기의 상세 정보를 조회합니다.
     */
    suspend fun getGame(gameId: String): BaseballGame

    /**
     * 특정 구단이 포함된 경기 일정을 조회합니다.
     *
     * from/to가 null이면 구현체에서 기본 조회 기간을 적용합니다.
     */
    suspend fun getTeamGameSchedule(
        team: KboTeam,
        from: LocalDate? = null,
        to: LocalDate? = null,
    ): List<BaseballGame>

    /**
     * 특정 구장에서 열리는 경기 일정을 조회합니다.
     *
     * from/to가 null이면 구현체에서 기본 조회 기간을 적용합니다.
     */
    suspend fun getStadiumGameSchedule(
        stadiumId: String,
        from: LocalDate? = null,
        to: LocalDate? = null,
    ): List<BaseballGame>
}
