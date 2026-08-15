package com.manruhomerun.yadanbeopseok.navigation.route

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 알림 목록 화면으로 이동할 때 사용하는 NavKey입니다.
 */
@Serializable
data object NotificationNavKey : NavKey

/**
 * 선택한 여행의 상세 일정 화면으로 이동할 때 사용하는 NavKey입니다.
 *
 * @property travelId 조회할 여행의 고유 식별자
 */
@Serializable
data class TravelDetailNavKey(
    val travelId: String,
) : NavKey

/**
 * 선택한 관광지의 상세 화면으로 이동할 때 사용하는 NavKey입니다.
 *
 * @property travelSpotId 조회할 관광지의 고유 식별자
 */
@Serializable
data class TravelSpotDetailNavKey(
    val travelSpotId: String,
) : NavKey
