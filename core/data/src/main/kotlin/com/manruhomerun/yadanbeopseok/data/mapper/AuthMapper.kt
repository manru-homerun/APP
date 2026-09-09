package com.manruhomerun.yadanbeopseok.data.mapper

import com.manruhomerun.yadanbeopseok.datastore.AuthTokens
import com.manruhomerun.yadanbeopseok.network.auth.dto.LoginResponseDto
import com.manruhomerun.yadanbeopseok.network.auth.dto.TokenRefreshResponseDto

/**
 * 로그인 응답을 DataStore에 저장할 인증 정보로 변환합니다.
 */
internal fun LoginResponseDto.toAuthTokens(): AuthTokens =
    AuthTokens(
        onboardingCompleted = onboardingCompleted,
        accessToken = accessToken,
        refreshToken = refreshToken,
    )

/**
 * 토큰 재발급 응답의 access token을 기존 인증 정보에 반영합니다.
 *
 * 재발급 응답에 포함되지 않는 refresh token과 온보딩 상태는 유지합니다.
 */
internal fun TokenRefreshResponseDto.toAuthTokens(currentTokens: AuthTokens): AuthTokens =
    currentTokens.copy(
        accessToken = accessToken,
    )
