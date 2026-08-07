package com.manruhomerun.yadanbeopseok.navigation

import androidx.navigation3.runtime.NavKey
import com.manruhomerun.yadanbeopseok.navigation.route.TopLevelNavKey

/**
 * 앱에서 발생하는 화면 이동 동작을 정의하는 공통 계약입니다.
 *
 * Feature는 Nav3 백스택을 직접 수정하지 않고 이 계약을 통해 이동을 요청합니다.
 * 실제 백스택 관리는 YadanNavigationState에서 담당합니다.
 */
interface Navigator {
    /**
     * 현재 화면 위에 새로운 화면을 추가합니다.
     *
     * 상세 화면, 여행 만들기, 알림 화면처럼 현재 이동 흐름을
     * 유지한 채 새로운 화면으로 이동할 때 사용합니다.
     */
    fun navigate(key: NavKey)

    /**
     * 현재 화면을 백스택에서 제거하고 이전 화면으로 돌아갑니다.
     */
    fun navigateBack()

    /**
     * 하단 내비게이션의 최상위 탭으로 이동합니다.
     *
     * 각 탭이 가지고 있던 백스택은 유지한 채 선택된 탭만 전환합니다.
     */
    fun navigateToTopLevel(key: TopLevelNavKey)

    /**
     * 현재 화면을 제거하고 새로운 화면으로 교체합니다.
     *
     * 완료 화면에서 상세 화면으로 이동하는 것처럼 사용자가
     * 이전 화면으로 다시 돌아가면 안 되는 흐름에 사용합니다.
     */
    fun replaceCurrent(key: NavKey)

    /**
     * 기존 내비게이션 기록을 모두 제거하고 새로운 시작 화면으로 이동합니다.
     *
     * 로그인 완료, 온보딩 완료, 로그아웃 또는 세션 만료처럼
     * 이전 인증 흐름으로 돌아가면 안 되는 경우에 사용합니다.
     */
    fun resetTo(key: NavKey)
}
