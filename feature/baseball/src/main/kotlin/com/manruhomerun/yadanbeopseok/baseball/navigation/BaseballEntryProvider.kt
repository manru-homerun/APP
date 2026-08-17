package com.manruhomerun.yadanbeopseok.baseball.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.GameScheduleNavKey

/**
 * 야구 기능에서 사용하는 NavKey와 실제 Route를 연결합니다.
 *
 * @param navigator 화면 이동과 세션 만료 처리를 담당합니다.
 */
fun EntryProviderScope<NavKey>.baseballEntryProvider(navigator: Navigator) {
    entry<GameScheduleNavKey> {
        /*
         * TODO: B·01 여행 생성 화면 구현 후
         * 선택한 경기 ID를 전달하는 이동 콜백을 연결합니다.
         */
        BaseballScheduleRoute(
            navigator = navigator,
        )
    }
}
