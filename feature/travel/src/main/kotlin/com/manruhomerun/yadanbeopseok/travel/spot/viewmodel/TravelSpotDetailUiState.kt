package com.manruhomerun.yadanbeopseok.travel.spot.viewmodel

import com.manruhomerun.yadanbeopseok.model.TravelSpotDetail

/**
 * 관광지 상세 화면에서 사용하는 UI 상태입니다.
 *
 * 본문과 이미지 목록의 조회 상태를 분리하고 찜 변경 상태를 관리합니다.
 */
data class TravelSpotDetailUiState(
    /** 서버에서 조회한 관광지 상세 정보입니다. */
    val detail: TravelSpotDetail? = null,

    /** 관광지 상세 본문을 불러오는 중인지 나타냅니다. */
    val isLoading: Boolean = true,

    /** 본문과 별도로 갤러리 이미지 URL 목록을 조회하는 중인지 나타냅니다. */
    val isLoadingImages: Boolean = false,

    /** 사진 영역에 표시할 이미지 목록 조회 오류이며 상세 본문을 숨기지 않습니다. */
    val imageErrorMessage: String? = null,

    /** 관광지 찜 상태를 변경하는 중인지 나타냅니다. */
    val isUpdatingDibs: Boolean = false,

    /** 상세 조회 또는 찜 변경 실패를 안내할 오류 메시지입니다. */
    val errorMessage: String? = null,
) {
    /** 화면에 표시할 수 있는 관광지 상세 정보가 있는지 나타냅니다. */
    val hasDetail: Boolean
        get() = detail != null
}
