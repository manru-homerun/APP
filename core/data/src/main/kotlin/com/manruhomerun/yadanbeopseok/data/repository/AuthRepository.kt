package com.manruhomerun.yadanbeopseok.data.repository

import kotlinx.coroutines.flow.Flow

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
     * 로그인 성공 시 서버에서 받은 서비스 토큰과 온보딩 상태를 로컬에 저장합니다.
     */
    suspend fun loginWithKakao(
        kakaoAccessToken: String,
        fcmToken: String?,
    )

    /**
     * 저장된 인증 정보로 앱 시작 시 세션 상태를 복원합니다.
     * access token 만료는 보호된 API의 401 응답에서 처리합니다.
     */
    suspend fun restoreSession(): AuthSessionState

    /**
     * 로컬 인증 정보의 변경을 세션 상태로 관찰합니다.
     *
     * 로그인, 온보딩 완료, 로그아웃 또는 세션 만료로 인증 정보가 변경되면
     * 변경된 상태를 방출합니다.
     */
    fun observeSessionState(): Flow<AuthSessionState>

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
