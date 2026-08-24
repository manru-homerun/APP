package com.manruhomerun.yadanbeopseok.travel.viewmodel

import com.manruhomerun.yadanbeopseok.model.UserProfile

/**
 * B·04 동행자 선택 화면에서 사용하는 상태입니다.
 *
 * 친구 목록 조회 결과만 관리하며, 사용자가 선택한 동행자는
 * 공유 [TravelCreationUiState]에 저장합니다.
 */
data class TravelCompanionSelectionUiState(
    /** 동행자로 선택할 수 있는 현재 사용자의 친구 목록입니다. */
    val friends: List<UserProfile> = emptyList(),

    /** 친구 목록을 불러오고 있는지 나타냅니다. */
    val isLoading: Boolean = false,

    /** 친구 목록 조회 중 표시할 사용자 안내 문구입니다. */
    val errorMessage: String? = null,
)
