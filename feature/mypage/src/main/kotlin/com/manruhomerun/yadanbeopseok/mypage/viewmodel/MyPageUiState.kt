package com.manruhomerun.yadanbeopseok.mypage.viewmodel

import com.manruhomerun.yadanbeopseok.model.UserProfile

/**
 * H·01에서 처리 중인 계정 작업입니다.
 */
enum class MyPageAction {
    LOGOUT,
    WITHDRAWAL,
}

/**
 * H·01 마이페이지의 화면 상태입니다.
 *
 * @property userProfile 화면에 표시할 사용자 프로필
 * @property travelPreferenceSummary 여행 취향 요약 문구
 * @property isLoading 프로필과 여행 취향을 처음 불러오는 중인지 여부
 * @property processingAction 현재 처리 중인 계정 작업
 * @property errorMessage 사용자에게 표시할 안전한 오류 문구
 */
data class MyPageUiState(
    val userProfile: UserProfile? = null,
    val travelPreferenceSummary: String? = null,
    val isLoading: Boolean = true,
    val processingAction: MyPageAction? = null,
    val errorMessage: String? = null,
) {
    val isProcessing: Boolean
        get() = processingAction != null
}
