package com.manruhomerun.yadanbeopseok.travel.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.TravelCreationNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.TravelDetailNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.TravelSpotDetailNavKey
import com.manruhomerun.yadanbeopseok.travel.creation.navigation.TravelCreationRoute
import com.manruhomerun.yadanbeopseok.travel.detail.navigation.TravelDetailRoute
import com.manruhomerun.yadanbeopseok.travel.spot.navigation.TravelSpotDetailRoute

/**
 * 여행 기능에서 사용하는 NavKey와 실제 Route를 연결합니다.
 */
fun EntryProviderScope<NavKey>.travelEntryProvider(navigator: Navigator) {
    entry<TravelCreationNavKey> { key ->
        TravelCreationRoute(
            baseballGameId = key.baseballGameId,
            navigator = navigator,
        )
    }

    entry<TravelDetailNavKey> { key ->
        TravelDetailRoute(
            travelId = key.travelId,
            navigator = navigator,
        )
    }

    entry<TravelSpotDetailNavKey> { key ->
        TravelSpotDetailRoute(
            travelSpotId = key.travelSpotId,
            navigator = navigator,
        )
    }
}
