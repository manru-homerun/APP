package com.manruhomerun.yadanbeopseok.data.mapper

import com.manruhomerun.yadanbeopseok.datastore.AuthTokens
import com.manruhomerun.yadanbeopseok.model.LoginResult
import com.manruhomerun.yadanbeopseok.network.auth.dto.LoginResponseDto
import com.manruhomerun.yadanbeopseok.network.auth.dto.TokenRefreshResponseDto

/**
 * 로그인 응답을 DataStore에 저장할 인증 정보로 변환합니다.
 *
 * 로그인 응답의 사용자 ID를 인증 토큰과 함께 저장합니다.
 * 서버가 반환하는 expiresIn은 남은 유효 시간(초)이므로,
 * 응답을 받은 현재 시각을 더해 절대 만료 시각으로 변환합니다.
 *
 * @param currentEpochSeconds 응답을 받은 현재 Unix epoch 시간(초)
 */
internal fun LoginResponseDto.toAuthTokens(
    currentEpochSeconds: Long,
): AuthTokens =
    createAuthTokens(
        userId = userId.toString(),
        accessToken = accessToken,
        refreshToken = refreshToken,
        tokenType = tokenType,
        accessTokenExpiresIn = accessTokenExpiresIn,
        refreshTokenExpiresIn = refreshTokenExpiresIn,
        currentEpochSeconds = currentEpochSeconds,
    )

/**
 * 토큰 재발급 응답을 DataStore에 저장할 인증 정보로 변환합니다.
 *
 * 재발급 응답에는 사용자 ID가 없으므로 기존 인증 정보에 저장된
 * 사용자 ID를 전달받아 유지합니다. access token과 refresh token,
 * 두 토큰의 만료 시각은 모두 새로운 값으로 교체합니다.
 *
 * @param userId 기존 인증 정보에 저장된 야단법석 사용자 ID
 * @param currentEpochSeconds 응답을 받은 현재 Unix epoch 시간(초)
 */
internal fun TokenRefreshResponseDto.toAuthTokens(
    userId: String,
    currentEpochSeconds: Long,
): AuthTokens =
    createAuthTokens(
        userId = userId,
        accessToken = accessToken,
        refreshToken = refreshToken,
        tokenType = tokenType,
        accessTokenExpiresIn = accessTokenExpiresIn,
        refreshTokenExpiresIn = refreshTokenExpiresIn,
        currentEpochSeconds = currentEpochSeconds,
    )

/**
 * 로그인 응답을 화면 이동 판단에 사용하는 앱 내부 결과로 변환합니다.
 *
 * 서비스 인증 정보는 [AuthTokens]로 별도 저장하므로
 * [LoginResult]에는 화면 이동에 필요한 값만 포함합니다.
 */
internal fun LoginResponseDto.toLoginResult(): LoginResult =
    LoginResult(
        userId = userId.toString(),
        isNewUser = isNewUser,
        onboardingCompleted = onboardingCompleted,
    )

/**
 * 로그인과 토큰 재발급 응답이 공유하는 인증 정보 변환 로직입니다.
 */
private fun createAuthTokens(
    userId: String,
    accessToken: String,
    refreshToken: String,
    tokenType: String,
    accessTokenExpiresIn: Long,
    refreshTokenExpiresIn: Long,
    currentEpochSeconds: Long,
): AuthTokens =
    AuthTokens(
        userId = userId,
        accessToken = accessToken,
        refreshToken = refreshToken,
        tokenType = tokenType,
        accessTokenExpiresAtEpochSeconds = currentEpochSeconds + accessTokenExpiresIn,
        refreshTokenExpiresAtEpochSeconds = currentEpochSeconds + refreshTokenExpiresIn,
    )
