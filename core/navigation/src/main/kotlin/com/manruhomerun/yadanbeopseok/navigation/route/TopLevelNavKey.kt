package com.manruhomerun.yadanbeopseok.navigation.route

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 하단 내비게이션에서 선택할 수 있는 최상위 화면의 공통 계약입니다.
 *
 * 일반 상세 화면과 최상위 화면을 구분하고,
 * 하단 내비게이션의 선택 상태와 탭별 백스택을 관리할 때 사용합니다.
 */
sealed interface TopLevelNavKey : NavKey {
    /** 알림에서 연 최상위 화면이 이전 화면으로 돌아갈 수 있는지 나타냅니다. */
    val returnToPrevious: Boolean
}

/**
 * 홈 탭의 시작 화면입니다.
 */
@Serializable
data object HomeNavKey : TopLevelNavKey {
    override val returnToPrevious: Boolean = false
}

/**
 * 경기 탭의 경기 일정 화면입니다.
 *
 * [initialTeamId]가 있으면 알림에서 전달된 구단을 최초 선택합니다.
 */
@Serializable
data class GameScheduleNavKey(val initialTeamId: Long? = null, override val returnToPrevious: Boolean = false) :
    TopLevelNavKey

/**
 * 기록 탭의 완료된 여행 기록 화면입니다.
 */
@Serializable
data object TravelRecordNavKey : TopLevelNavKey {
    override val returnToPrevious: Boolean = false
}

/**
 * 마이 탭의 마이페이지 화면입니다.
 */
@Serializable
data class MyPageNavKey(override val returnToPrevious: Boolean = false) : TopLevelNavKey
