package com.manruhomerun.yadanbeopseok.record.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.TravelStickerPhotoNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.TravelVerificationNavKey

/**
 * 기록 기능에서 사용하는 NavKey와 실제 Route를 연결합니다.
 *
 * D02 방문 인증에서 D03 스티커 획득 화면으로 전환하고,
 * D03의 사진에 붙이기 버튼을 D04 사진 꾸미기 화면에 연결합니다.
 */
fun EntryProviderScope<NavKey>.recordEntryProvider(
    navigator: Navigator,
) {
    entry<TravelVerificationNavKey> { key ->
        TravelVerificationRoute(
            travelId = key.travelId,
            spotId = key.travelSpotId,
            navigator = navigator,
            onDecoratePhotoClick = {
                navigator.navigate(
                    TravelStickerPhotoNavKey(
                        travelId = key.travelId,
                    ),
                )
            },
        )
    }

    entry<TravelStickerPhotoNavKey> { key ->
        TravelStickerPhotoRoute(
            travelId = key.travelId,
            navigator = navigator,
        )
    }
}
