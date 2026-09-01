package com.manruhomerun.yadanbeopseok.travel.util

import com.manruhomerun.yadanbeopseok.model.TravelSpot

/**
 * 관광지 ID를 기준으로 선택 상태를 전환한 목록을 반환합니다.
 *
 * B06의 필수 포함 관광지와 C01b/C01c의 임시 선택 목록에서 재사용합니다.
 * 이미 선택한 관광지는 제거하고, 새 관광지는 목록 마지막에 추가합니다.
 * 원본 목록과 기존 관광지의 상대적인 순서는 변경하지 않습니다.
 *
 * 기존 일정의 중복 추가 제한과 선택 결과 반영은 각 ViewModel에서 처리합니다.
 */
internal fun List<TravelSpot>.toggleTravelSpotSelection(travelSpot: TravelSpot): List<TravelSpot> {
    val isSelected = any { it.id == travelSpot.id }

    return if (isSelected) {
        filterNot { it.id == travelSpot.id }
    } else {
        this + travelSpot
    }
}
