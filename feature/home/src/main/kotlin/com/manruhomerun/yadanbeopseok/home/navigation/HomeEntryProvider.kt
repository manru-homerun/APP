package com.manruhomerun.yadanbeopseok.home.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.HomeNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.NotificationNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.TravelDetailNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.TravelSpotDetailNavKey

/**
 * 홈에서 사용하는 NavKey와 실제 화면 Route를 연결합니다.
 *
 * 홈에서 발생하는 알림, 여행 상세 및 관광지 상세 이동을
 * 각 화면에 해당하는 NavKey로 변환합니다.
 */
fun EntryProviderScope<NavKey>.homeEntryProvider(
    navigator: Navigator,
) {
    entry<HomeNavKey> {
        HomeRoute(
            navigator = navigator,
            onNotificationClick = {
                navigator.navigate(NotificationNavKey)
            },
            onTravelClick = { travelId ->
                navigator.navigate(
                    TravelDetailNavKey(
                        travelId = travelId,
                    ),
                )
            },
            onTravelSpotClick = { travelSpotId ->
                navigator.navigate(
                    TravelSpotDetailNavKey(
                        travelSpotId = travelSpotId,
                    ),
                )
            },
        )
    }
}


