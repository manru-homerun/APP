package com.manruhomerun.yadanbeopseok.network.travel.dto

import kotlinx.serialization.Serializable

/**
 * 관광지를 찜하거나 찜을 취소할 때 사용하는 요청 DTO입니다.
 *
 * 찜 등록과 찜 취소 API가 동일한 요청 Body를 사용하므로
 * 두 요청에서 이 DTO를 함께 재사용합니다.
 *
 * @property contentId 찜 상태를 변경할 관광지의 고유 식별자
 */
@Serializable
data class TravelSpotDibsRequestDto(
    val contentId: Long,
)
