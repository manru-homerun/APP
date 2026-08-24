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

/**
 * 여행 만들기 흐름으로 이동할 때 사용하는 NavKey입니다.
 *
 * [baseballGameId]가 있으면 A·05에서 선택한 경기를 확정하고 B·02부터 시작합니다.
 * null이면 하단 내비게이션의 추가 버튼에서 진입한 것으로 보고 B·01부터 시작합니다.
 *
 * @property baseballGameId 미리 선택된 야구 경기 ID
 */
@Serializable
data class TravelCreationNavKey(
    val baseballGameId: String? = null,
) : NavKey
