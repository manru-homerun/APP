package com.manruhomerun.yadanbeopseok.data.repository

import com.manruhomerun.yadanbeopseok.model.LoginResult

interface AuthRepository {
    /**
     * 카카오 액세스 토큰으로 야단법석 서비스에 로그인합니다.
     *
     * 로그인 성공 시 서버에서 받은 서비스 토큰을 로컬에 저장하고,
     * 화면 이동에 필요한 로그인 결과를 반환합니다.
     */
    suspend fun loginWithKakao(
        kakaoAccessToken: String,
        fcmToken: String?,
    ): LoginResult

    /**
     * 저장된 야단법석 refresh token으로 서비스 토큰을 재발급합니다.
     */
    suspend fun refreshAccessToken()

    /**
     * 저장된 야단법석 인증 정보를 삭제하여 로그아웃합니다.
     */
    suspend fun logout()

    /**
     * 현재 로그인한 사용자의 회원 탈퇴를 요청합니다.
     */
    suspend fun withdraw()
}
