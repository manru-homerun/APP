package com.manruhomerun.yadanbeopseok.data.mapper

import com.manruhomerun.yadanbeopseok.common.error.InvalidResponseException
import com.manruhomerun.yadanbeopseok.model.BaseballGame
import com.manruhomerun.yadanbeopseok.model.BaseballGameSummary
import com.manruhomerun.yadanbeopseok.model.BaseballGameType
import com.manruhomerun.yadanbeopseok.model.BaseballStadium
import com.manruhomerun.yadanbeopseok.model.BaseballStadiumSummary
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.network.baseball.dto.BaseballGameResponseDto
import com.manruhomerun.yadanbeopseok.network.baseball.dto.BaseballGameScheduleItemResponseDto
import com.manruhomerun.yadanbeopseok.network.baseball.dto.BaseballGameScheduleResponseDto
import com.manruhomerun.yadanbeopseok.network.baseball.dto.BaseballStadiumResponseDto
import com.manruhomerun.yadanbeopseok.network.baseball.dto.BaseballStadiumSummaryResponseDto
import kotlinx.datetime.LocalDateTime

/**
 * 경기 상세 응답을 앱 내부 경기 상세 모델로 변환합니다.
 */
internal fun BaseballGameResponseDto.toBaseballGame(): BaseballGame =
    BaseballGame(
        id = gameId,
        stadium = stadium.toBaseballStadium(),
        homeTeam = homeTeam.teamId.toKboTeam("homeTeam.teamId"),
        awayTeam = awayTeam.teamId.toKboTeam("awayTeam.teamId"),
        gameDateTime = dateTime.toBaseballGameDateTime(),
        gameType = type.toBaseballGameType(),
        homeTeamScore = gameResult?.homeTeamScore,
        awayTeamScore = gameResult?.awayTeamScore,
        isCanceled = canceled ?: false,
    )

/**
 * 경기 일정 응답을 앱 내부 경기 요약 목록으로 변환합니다.
 *
 * 페이지 정보는 현재 경기 일정 화면에서 사용하지 않으므로
 * contents에 포함된 경기 목록만 반환합니다.
 */
internal fun BaseballGameScheduleResponseDto.toBaseballGameSummaries(): List<BaseballGameSummary> =
    contents.map { game ->
        game.toBaseballGameSummary()
    }

/**
 * 경기 일정에 포함된 개별 응답을 앱 내부 경기 요약 모델로 변환합니다.
 */
private fun BaseballGameScheduleItemResponseDto.toBaseballGameSummary(): BaseballGameSummary =
    BaseballGameSummary(
        id = gameId,
        stadium = stadium.toBaseballStadiumSummary(),
        homeTeam = homeTeam.teamId.toKboTeam("homeTeam.teamId"),
        awayTeam = awayTeam.teamId.toKboTeam("awayTeam.teamId"),
        gameDateTime = dateTime.toBaseballGameDateTime(),
    )

/**
 * 경기 상세의 구장 정보를 앱 내부 전체 구장 모델로 변환합니다.
 */
private fun BaseballStadiumResponseDto.toBaseballStadium(): BaseballStadium =
    BaseballStadium(
        id = stadiumId.toString(),
        name = stadiumName,
        region = regionCode.toBaseballRegion(),
        latitude = latitude,
        longitude = longitude,
    )

/**
 * 경기 일정의 구장 정보를 앱 내부 구장 요약 모델로 변환합니다.
 */
private fun BaseballStadiumSummaryResponseDto.toBaseballStadiumSummary(): BaseballStadiumSummary =
    BaseballStadiumSummary(
        id = stadiumId.toString(),
        name = stadiumName,
    )

/**
 * 서버의 경기 일시 문자열을 앱 내부 날짜 및 시간 모델로 변환합니다.
 *
 * 서버 형식인 `2026-06-12 14:30`을
 * kotlinx-datetime에서 사용하는 ISO 형식으로 정규화합니다.
 */
private fun String.toBaseballGameDateTime(): LocalDateTime = try {
    LocalDateTime.parse(replace(' ', 'T'))
} catch (exception: IllegalArgumentException) {
    throw InvalidResponseException(
        message = "Invalid baseball game dateTime: $this",
        cause = exception,
    )
}

/**
 * 서버의 경기 종류 문자열을 앱 내부 경기 종류로 변환합니다.
 */
private fun String.toBaseballGameType(): BaseballGameType =
    BaseballGameType.entries.firstOrNull { gameType ->
        gameType.name == this
    } ?: BaseballGameType.UNKNOWN

/**
 * 서버의 구장 지역 enum 문자열을 앱 내부 지역으로 변환합니다.
 */
private fun String.toBaseballRegion(): Region =
    Region.entries.firstOrNull { region ->
        region.name == this
    } ?: throw InvalidResponseException(
        message = "Unsupported baseball stadium regionCode: $this",
    )
