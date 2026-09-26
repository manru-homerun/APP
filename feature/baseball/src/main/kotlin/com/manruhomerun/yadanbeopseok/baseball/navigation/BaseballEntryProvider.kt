package com.manruhomerun.yadanbeopseok.baseball.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.GameScheduleNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.TravelCreationNavKey

/**
 * 야구 기능에서 사용하는 NavKey와 실제 Route를 연결합니다.
 *
 * @param navigator 알림 진입 시 뒤로가기와 여행 생성 화면 이동을 처리합니다.
 */
fun EntryProviderScope<NavKey>.baseballEntryProvider(navigator: Navigator) {
    entry<GameScheduleNavKey> { key ->
        BaseballScheduleRoute(
            initialTeamId = key.initialTeamId,
            onBackClick = if (key.returnToPrevious) navigator::navigateBack else null,
            onPlanClick = { gameId ->
                navigator.navigate(TravelCreationNavKey(baseballGameId = gameId))
            },
        )
    }
}
