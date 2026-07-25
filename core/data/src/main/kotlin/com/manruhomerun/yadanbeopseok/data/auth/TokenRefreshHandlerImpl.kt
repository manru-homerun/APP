package com.manruhomerun.yadanbeopseok.data.auth

import com.manruhomerun.yadanbeopseok.data.repository.AuthRepository
import com.manruhomerun.yadanbeopseok.network.auth.token.TokenRefreshHandler
import javax.inject.Inject

/**
 * [TokenRefreshHandler]의 Data 계층 구현체입니다.
 *
 * Network에서 전달된 토큰 재발급 요청을
 * 기존 [AuthRepository]의 재발급 기능에 위임합니다.
 */
internal class TokenRefreshHandlerImpl @Inject constructor(
    private val authRepository: AuthRepository,
) : TokenRefreshHandler {
    /**
     * 저장된 Refresh Token으로 서비스 토큰을 재발급합니다.
     *
     * API 호출과 새 토큰 저장은 [AuthRepository] 구현체가 담당합니다.
     */
    override suspend fun refreshToken() {
        authRepository.refreshAccessToken()
    }
}
