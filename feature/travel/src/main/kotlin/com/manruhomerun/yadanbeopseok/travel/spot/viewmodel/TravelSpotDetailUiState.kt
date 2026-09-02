package com.manruhomerun.yadanbeopseok.travel.spot.viewmodel

import com.manruhomerun.yadanbeopseok.model.TravelSpotDetail

/**
 * 관광지 상세 화면에서 사용하는 UI 상태입니다.
 *
 * 조회한 관광지 상세 정보와 로딩, 찜 변경 및 오류 상태를 관리합니다.
 */
data class TravelSpotDetailUiState(
    /** 서버에서 조회한 관광지 상세 정보입니다. */
    val detail: TravelSpotDetail? = null,

    /** 관광지 상세 정보를 불러오는 중인지 나타냅니다. */
    val isLoading: Boolean = true,

    /** 관광지 찜 상태를 변경하는 중인지 나타냅니다. */
    val isUpdatingDibs: Boolean = false,

    /** 사용자에게 안내할 오류 메시지입니다. */
    val errorMessage: String? = null,
) {
    /** 화면에 표시할 수 있는 관광지 상세 정보가 있는지 나타냅니다. */
    val hasDetail: Boolean
        get() = detail != null
}
