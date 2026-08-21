package com.manruhomerun.yadanbeopseok.travel.viewmodel

import com.manruhomerun.yadanbeopseok.model.TravelTheme

/**
 * B·03 여행 테마 선택 화면에서 사용하는 상태입니다.
 *
 * 서버에서 조회한 선택 가능한 테마 목록만 관리합니다.
 * 사용자가 선택한 테마는 공유 [TravelCreationUiState]에 저장합니다.
 */
data class TravelThemeSelectionUiState(
    /** 서버에서 제공하는 여행 테마 목록입니다. */
    val themes: List<TravelTheme> = emptyList(),

    /** 여행 테마 목록을 불러오고 있는지 나타냅니다. */
    val isLoading: Boolean = false,

    /** 여행 테마 조회 중 표시할 사용자 안내 문구입니다. */
    val errorMessage: String? = null,
)
