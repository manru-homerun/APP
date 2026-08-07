package com.manruhomerun.yadanbeopseok.data.repository

import com.manruhomerun.yadanbeopseok.model.LoginResult

/**
 * 저장된 인증 정보를 확인한 결과입니다.
 */
enum class AuthSessionState {
    /**
     * 유효한 인증 정보가 없어 로그인이 필요합니다.
     */
    LOGGED_OUT,

    /**
     * 로그인은 유지되지만 온보딩을 완료해야 합니다.
     */
    ONBOARDING_REQUIRED,

    /**
     * 로그인과 온보딩이 모두 완료된 상태입니다.
     */
    AUTHENTICATED,
}

interface AuthRepository {
    /**
     * 카카오 액세스 토큰으로 야단법석 서비스에 로그인합니다.
     *
     * 로그인 성공 시 서버에서 받은 서비스 토큰과 사용자 ID를 로컬에 저장하고,
     * 화면 이동에 필요한 로그인 결과를 반환합니다.
     */
    suspend fun loginWithKakao(
        kakaoAccessToken: String,
        fcmToken: String?,
    ): LoginResult

    /**
     * 저장된 인증 정보와 토큰 만료 상태를 검사하여 세션을 복원합니다.
     *
     * access token이 만료되고 refresh token이 유효하면 토큰을 재발급합니다.
     * 네트워크 오류처럼 재시도할 수 있는 문제는 호출자에게 전달합니다.
     */
    suspend fun restoreSession(): AuthSessionState

    /**
     * 현재 로컬 인증 정보에 저장된 야단법석 사용자 ID를 조회합니다.
     *
     * 로그인하지 않았거나 저장된 인증 정보가 불완전하면 null을 반환합니다.
     */
    suspend fun getCurrentUserId(): String?

    /**
     * 저장된 야단법석 refresh token으로 서비스 토큰을 재발급합니다.
     */
    suspend fun refreshAccessToken()

    /**
     * 백엔드에 로그아웃을 요청하고 저장된 인증 정보를 삭제합니다.
     *
     * 카카오 액세스 토큰과 기기 정보를 백엔드에 전달하여
     * 현재 기기의 FCM 토큰을 비활성화합니다.
     *
     * @param kakaoAccessToken Kakao SDK에서 발급받은 카카오 액세스 토큰
     * @param deviceId 로그아웃하는 기기의 식별자
     * @param fcmToken 비활성화할 기기의 FCM 토큰
     */
    suspend fun logout(
        kakaoAccessToken: String,
        deviceId: String? = null,
        fcmToken: String? = null,
    )

    /**
     * 현재 로그인한 사용자의 회원 탈퇴를 요청합니다.
     */
    suspend fun withdraw()
}
