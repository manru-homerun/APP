package com.manruhomerun.yadanbeopseok.mypage.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.MyPageNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.TravelSpotDetailNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.TravelSpotDibsNavKey

/**
 * 마이페이지 기능에서 사용하는 NavKey와 실제 Route를 연결합니다.
 */
fun EntryProviderScope<NavKey>.myPageEntryProvider(navigator: Navigator) {
    entry<MyPageNavKey> {
        MyPageRoute(
            onProfileClick = {},
            onDibsClick = {
                navigator.navigate(TravelSpotDibsNavKey)
            },
            onTravelPreferenceClick = {},
            onFriendsClick = {},
            onTermsClick = {},
            onPrivacyPolicyClick = {},
        )
    }

    entry<TravelSpotDibsNavKey> {
        TravelSpotDibsRoute(
            onBackClick = navigator::navigateBack,
            onTravelSpotClick = { travelSpotId ->
                navigator.navigate(TravelSpotDetailNavKey(travelSpotId))
            },
        )
    }
}
