package com.manruhomerun.yadanbeopseok.model

import kotlinx.datetime.LocalDateTime

/**
 * 앱 내부에서 사용하는 KBO 구단 모델입니다.
 *
 * @property serverId 서버에서 사용하는 구단 ID
 * @property displayName 화면에 표시하는 짧은 구단명
 * @property fullName 화면에 표시하는 전체 구단명
 */
enum class KboTeam(
    val serverId: Long,
    val displayName: String,
    val fullName: String,
) {
    LOTTE(
        serverId = 9L,
        displayName = "롯데",
        fullName = "롯데 자이언츠",
    ),
    KIA(
        serverId = 1L,
        displayName = "KIA",
        fullName = "KIA 타이거즈",
    ),
    SAMSUNG(
        serverId = 7L,
        displayName = "삼성",
        fullName = "삼성 라이온즈",
    ),
    LG(
        serverId = 2L,
        displayName = "LG",
        fullName = "LG 트윈스",
    ),
    DOOSAN(
        serverId = 3L,
        displayName = "두산",
        fullName = "두산 베어스",
    ),
    KIWOOM(
        serverId = 6L,
        displayName = "키움",
        fullName = "키움 히어로즈",
    ),
    SSG(
        serverId = 8L,
        displayName = "SSG",
        fullName = "SSG 랜더스",
    ),
    NC(
        serverId = 10L,
        displayName = "NC",
        fullName = "NC 다이노스",
    ),
    HANWHA(
        serverId = 4L,
        displayName = "한화",
        fullName = "한화 이글스",
    ),
    KT(
        serverId = 5L,
        displayName = "KT",
        fullName = "KT 위즈",
    );

    companion object {
        /**
         * 서버 구단 ID와 일치하는 KBO 구단을 반환합니다.
         */
        fun findByServerId(serverId: Long): KboTeam? =
            entries.firstOrNull { team ->
                team.serverId == serverId
            }
    }
}

/**
 * 경기 일정에서 사용하는 구장 요약 모델입니다.
 */
data class BaseballStadiumSummary(
    val id: String,
    val name: String,
)

/**
 * 경기 상세에서 사용하는 전체 구장 모델입니다.
 */
data class BaseballStadium(
    val id: String,
    val name: String,
    val region: Region,
    val latitude: Double,
    val longitude: Double,
)

/**
 * 구단 또는 구장별 경기 일정에서 사용하는 경기 요약 모델입니다.
 */
data class BaseballGameSummary(
    val id: String,
    val stadium: BaseballStadiumSummary,
    val homeTeam: KboTeam,
    val awayTeam: KboTeam,
    val gameDateTime: LocalDateTime,
)

/**
 * 특정 경기의 상세 정보를 나타내는 앱 내부 모델입니다.
 */
data class BaseballGame(
    val id: String,
    val stadium: BaseballStadium,
    val homeTeam: KboTeam,
    val awayTeam: KboTeam,
    val gameDateTime: LocalDateTime,
    val gameType: BaseballGameType,
    val homeTeamScore: Int? = null,
    val awayTeamScore: Int? = null,
    val isCanceled: Boolean = false,
)

/**
 * 서버에서 제공하는 경기 종류입니다.
 */
enum class BaseballGameType {
    REGULAR,
    EXHIBITION,
    POSTSEASON,
    ALL_STAR,
    UNKNOWN,
}
