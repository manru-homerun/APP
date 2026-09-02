package com.manruhomerun.yadanbeopseok.travel.course.viewmodel

import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.travel.spot.viewmodel.TravelSpotSelectionUiState

/**
 * C01b/C01c에서 특정 일차에 추가할 관광지를 선택하는 상태입니다.
 *
 * 추천·찜·검색 결과는 공용 [TravelSpotSelectionUiState]를 재사용하고,
 * C01 편집 흐름에 필요한 대상 일차와 임시 선택 목록만 별도로 관리합니다.
 *
 * 선택한 관광지는 완료 버튼을 누르기 전까지 실제 여행 일정에 반영하지 않습니다.
 */
data class TravelCourseSpotSelectionUiState(
    /** 관광지를 추가할 대상 일차이며, 선택 화면이 닫혀 있으면 null입니다. */
    val targetDay: Int? = null,

    /** 추천·찜·검색 결과와 조회 상태입니다. */
    val selectionState: TravelSpotSelectionUiState = TravelSpotSelectionUiState(),

    /** 현재 C01b/C01c에서 임시로 선택한 관광지입니다. */
    val selectedTravelSpots: List<TravelSpot> = emptyList(),
) {
    /** 현재 관광지 추가 화면이 열려 있는지 나타냅니다. */
    val isActive: Boolean
        get() = targetDay != null

    /** 완료 버튼에 표시할 임시 선택 관광지 수입니다. */
    val selectedCount: Int
        get() = selectedTravelSpots.size
}
