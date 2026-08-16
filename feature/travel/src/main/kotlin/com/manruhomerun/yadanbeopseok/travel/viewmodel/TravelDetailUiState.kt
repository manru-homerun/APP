package com.manruhomerun.yadanbeopseok.travel.viewmodel

import com.manruhomerun.yadanbeopseok.model.Travel
import com.manruhomerun.yadanbeopseok.model.TravelDay

/**
 * 여행 상세 화면에서 사용하는 UI 상태입니다.
 *
 * 조회한 여행 정보, 현재 선택한 일차와 화면의 로딩 및 오류 상태를 관리합니다.
 */
data class TravelDetailUiState(
    /** 서버에서 조회한 여행 상세 정보입니다. */
    val travel: Travel? = null,

    /** 현재 화면에 표시할 여행 일차입니다. */
    val selectedDay: Int? = null,

    /** 여행 상세 정보를 불러오는 중인지 나타냅니다. */
    val isLoading: Boolean = true,

    /** 사용자에게 안내할 오류 메시지입니다. */
    val errorMessage: String? = null,
) {
    /** 여행에 포함된 일차 번호 목록입니다. */
    val dayNumbers: List<Int>
        get() = travel?.days?.map { it.day }.orEmpty()

    /** 현재 선택된 일차의 일정입니다. */
    val selectedTravelDay: TravelDay?
        get() = travel?.days?.firstOrNull { it.day == selectedDay }

    /** 화면에 표시할 수 있는 여행 상세 정보가 있는지 나타냅니다. */
    val hasTravel: Boolean
        get() = travel != null
}
