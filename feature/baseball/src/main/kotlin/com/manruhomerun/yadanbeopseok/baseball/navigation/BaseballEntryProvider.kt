package com.manruhomerun.yadanbeopseok.baseball.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.GameScheduleNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.TravelCreationNavKey

/**
 * 야구 기능에서 사용하는 NavKey와 실제 Route를 연결합니다.
 *
 * @param navigator 화면 이동과 세션 만료 처리를 담당합니다.
 */
fun EntryProviderScope<NavKey>.baseballEntryProvider(navigator: Navigator) {
    entry<GameScheduleNavKey> {
        BaseballScheduleRoute(
            navigator = navigator,
            onPlanClick = { gameId ->
                navigator.navigate(TravelCreationNavKey(baseballGameId = gameId))
            },
        )
    }
}
