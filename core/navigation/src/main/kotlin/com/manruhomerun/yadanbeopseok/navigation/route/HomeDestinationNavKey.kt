package com.manruhomerun.yadanbeopseok.navigation.route

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 알림 목록 화면으로 이동할 때 사용하는 NavKey입니다.
 */
@Serializable
data object NotificationNavKey : NavKey

/**
 * H·04 찜한 관광지 목록 화면으로 이동할 때 사용하는 NavKey입니다.
 */
@Serializable
data object TravelSpotDibsNavKey : NavKey

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

/**
 * 여행 중 인증 대상 관광지의 방문 인증 화면으로 이동합니다.
 *
 * @property travelId 인증할 관광지가 포함된 여행 ID
 * @property travelSpotId 방문 인증 대상 관광지 ID
 */
@Serializable
data class TravelVerificationNavKey(
    val travelId: String,
    val travelSpotId: String,
) : NavKey

/**
 * 획득한 스티커로 사진을 꾸미는 D04 화면으로 이동합니다.
 *
 * D04에서는 [travelId]로 스티커팩을 다시 조회합니다.
 *
 * @property travelId 스티커를 획득한 여행의 고유 식별자
 */
@Serializable
data class TravelStickerPhotoNavKey(
    val travelId: String,
) : NavKey

/**
 * 완료된 여행의 D·01b 지난 여행 상세 화면으로 이동합니다.
 *
 * @property travelId 조회할 완료 여행의 고유 식별자
 */
@Serializable
data class TravelRecordDetailNavKey(val travelId: String) : NavKey
