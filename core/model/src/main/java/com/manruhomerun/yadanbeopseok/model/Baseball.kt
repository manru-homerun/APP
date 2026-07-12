package com.manruhomerun.yadanbeopseok.model

import kotlinx.datetime.LocalDateTime

/**
 * 앱 내부에서 사용하는 KBO 구단 모델입니다.
 *
 * 서버의 baseball_team 테이블을 그대로 옮긴 모델이 아니라,
 * 화면, 경기 일정, 여행 생성 로직에서 안정적으로 쓰기 위한 도메인 enum입니다.
 * 팀 컬러는 공식 출처 확인 후 designsystem에서 별도로 관리합니다.
 */
enum class KboTeam(
    val displayName: String,
    val fullName: String,
) {
    LOTTE("롯데", "롯데 자이언츠"),
    KIA("KIA", "KIA 타이거즈"),
    SAMSUNG("삼성", "삼성 라이온즈"),
    LG("LG", "LG 트윈스"),
    DOOSAN("두산", "두산 베어스"),
    KIWOOM("키움", "키움 히어로즈"),
    SSG("SSG", "SSG 랜더스"),
    NC("NC", "NC 다이노스"),
    HANWHA("한화", "한화 이글스"),
    KT("KT", "KT 위즈"),
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
