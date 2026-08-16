package com.manruhomerun.yadanbeopseok.data.repository.impl

import com.manruhomerun.yadanbeopseok.data.mapper.toBaseballGame
import com.manruhomerun.yadanbeopseok.data.mapper.toBaseballGameSummaries
import com.manruhomerun.yadanbeopseok.data.repository.BaseballRepository
import com.manruhomerun.yadanbeopseok.model.BaseballGame
import com.manruhomerun.yadanbeopseok.model.BaseballGameSummary
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.network.baseball.api.BaseballApi
import com.manruhomerun.yadanbeopseok.network.common.error.ApiCallExecutor
import javax.inject.Inject

/**
 * KBO 경기 상세와 경기 일정을 제공하는 Repository 구현체입니다.
 */
internal class BaseballRepositoryImpl @Inject constructor(
    private val baseballApi: BaseballApi,
    private val apiCallExecutor: ApiCallExecutor,
) : BaseballRepository {
    /**
     * 경기 ID에 해당하는 경기 상세 정보를 조회합니다.
     */
    override suspend fun getGame(gameId: String): BaseballGame {
        val response = apiCallExecutor.execute {
            baseballApi.getGame(gameId = gameId)
        }

        return response.toBaseballGame()
    }

    /**
     * 선택한 구단의 요청일 기준 최대 2주 경기 일정을 조회합니다.
     */
    override suspend fun getTeamGameSchedule(team: KboTeam): List<BaseballGameSummary> {
        val response = apiCallExecutor.execute {
            baseballApi.getTeamGameSchedule(teamId = team.serverId)
        }

        return response.toBaseballGameSummaries()
    }

    /**
     * 선택한 구장의 요청일 기준 최대 2주 경기 일정을 조회합니다.
     */
    override suspend fun getStadiumGameSchedule(stadiumId: String): List<BaseballGameSummary> {
        val response = apiCallExecutor.execute {
            baseballApi.getStadiumGameSchedule(
                stadiumId = stadiumId.toBaseballStadiumId(),
            )
        }

        return response.toBaseballGameSummaries()
    }
}

/**
 * 앱 내부의 문자열 구장 ID를 서버 요청에 사용하는 숫자 ID로 변환합니다.
 */
private fun String.toBaseballStadiumId(): Long =
    toLongOrNull()
        ?: throw IllegalArgumentException(
            "Baseball stadium ID must be numeric.",
        )
