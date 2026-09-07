package com.manruhomerun.yadanbeopseok.record.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.TravelRecordDetailNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.TravelRecordNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.TravelStickerPhotoNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.TravelVerificationNavKey

/**
 * 기록 기능에서 사용하는 NavKey와 실제 Route를 연결합니다.
 *
 * D·01 여행 기록 목록에서 D·01b 지난 여행 상세를 열고,
 * 획득한 스티커가 있는 여행은 D·04 사진 꾸미기로 연결합니다.
 *
 * D·02 방문 인증 완료 후에는 D·03 스티커 획득 화면을 표시하고,
 * 사진에 붙이기 버튼을 D·04 사진 꾸미기 화면에 연결합니다.
 */
fun EntryProviderScope<NavKey>.recordEntryProvider(navigator: Navigator) {
    entry<TravelRecordNavKey> {
        TravelRecordRoute(
            onTravelClick = { travelId ->
                navigator.navigate(
                    TravelRecordDetailNavKey(
                        travelId = travelId,
                    ),
                )
            },
        )
    }

    entry<TravelRecordDetailNavKey> { key ->
        TravelRecordDetailRoute(
            travelId = key.travelId,
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
