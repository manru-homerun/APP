package com.manruhomerun.yadanbeopseok.model

import kotlinx.datetime.LocalDateTime

/**
 * 앱 내부에서 사용하는 KBO 구단 모델입니다.
 *
 * 서버의 baseball_team 테이블을 그대로 옮긴 모델이 아니라,
 * 화면, 경기 일정, 여행 생성 로직에서 안정적으로 쓰기 위한 도메인 enum입니다.
 * 팀 컬러는 공식 출처 확인 후 designsystem에서 별도로 관리합니다.
 *
 * @property serverId 서버 baseball_team 테이블에서 사용하는 구단 ID
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
         * 서버에서 전달받은 구단 ID와 일치하는 KBO 구단을 반환합니다.
         *
         * 앱에서 지원하지 않는 구단 ID라면 null을 반환하며,
         * 호출 계층에서 잘못된 서버 응답으로 처리할 수 있습니다.
         */
        fun findByServerId(serverId: Long): KboTeam? =
            entries.firstOrNull { team ->
                team.serverId == serverId
            }
    }
}

/**
 * 앱 내부에서 사용하는 야구장 모델입니다.
 *
 * ERD의 baseball_stadium을 참고하되,
 * region_code 문자열은 앱에서 바로 쓰기 좋은 Region으로 변환해서 사용합니다.
 */
data class BaseballStadium(
    val id: String,
    val name: String,
    val region: Region,
    val latitude: Double,
    val longitude: Double,
)

/**
 * 앱 내부에서 사용하는 야구 경기 모델입니다.
 *
 * ERD의 baseball_game과 API 응답을 화면에서 쓰기 좋은 형태로 정리한 모델입니다.
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

enum class BaseballGameType {
    REGULAR,
    EXHIBITION,
    POSTSEASON,
    ALL_STAR,
    UNKNOWN,
}
