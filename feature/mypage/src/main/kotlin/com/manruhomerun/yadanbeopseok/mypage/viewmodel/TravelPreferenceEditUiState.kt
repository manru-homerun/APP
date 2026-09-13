package com.manruhomerun.yadanbeopseok.mypage.viewmodel

import com.manruhomerun.yadanbeopseok.model.ProfileRegion
import com.manruhomerun.yadanbeopseok.model.TravelPreference
import com.manruhomerun.yadanbeopseok.model.TravelStyleScore

/** H·03 취향 수정 화면의 입력값과 요청 상태입니다. */
data class TravelPreferenceEditUiState(
    val originalPreference: TravelPreference? = null,
    val residenceRegion: ProfileRegion? = null,
    val travelStyleScore: TravelStyleScore = TravelStyleScore(4),
    val preferredTravelRegions: List<ProfileRegion> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    /** 현재 입력값을 서버에 저장할 수 있는지 나타냅니다. */
    val isSaveEnabled: Boolean
        get() =
            originalPreference != null &&
                residenceRegion != null &&
                preferredTravelRegions.isNotEmpty() &&
                !isLoading &&
                !isSaving
}
