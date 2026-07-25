package com.manruhomerun.yadanbeopseok.data.mapper

import com.manruhomerun.yadanbeopseok.datastore.AuthTokens
import com.manruhomerun.yadanbeopseok.model.LoginResult
import com.manruhomerun.yadanbeopseok.network.auth.dto.LoginResponseDto
import com.manruhomerun.yadanbeopseok.network.auth.dto.TokenRefreshResponseDto

/**
 * 로그인 응답의 야단법석 서비스 토큰을
 * DataStore에 저장할 인증 정보로 변환합니다.
 *
 * 서버가 반환하는 expiresIn은 남은 유효 시간(초)이므로,
 * 응답을 받은 현재 시각을 더해 절대 만료 시각으로 변환합니다.
 *
 * @param currentEpochSeconds 응답을 받은 현재 Unix epoch 시간(초)
 */
internal fun LoginResponseDto.toAuthTokens(
    currentEpochSeconds: Long,
): AuthTokens =
    createAuthTokens(
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
 * 재발급 문서에 따라 access token과 refresh token,
 * 두 토큰의 만료 시각을 모두 새로운 값으로 교체합니다.
 *
 * @param currentEpochSeconds 응답을 받은 현재 Unix epoch 시간(초)
 */
internal fun TokenRefreshResponseDto.toAuthTokens(
    currentEpochSeconds: Long,
): AuthTokens =
    createAuthTokens(
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
 * 서비스 토큰은 [AuthTokens]로 별도 저장하므로 [LoginResult]에 포함하지 않습니다.
 */
internal fun LoginResponseDto.toLoginResult(): LoginResult =
    LoginResult(
        userId = userId.toString(),
        isNewUser = isNewUser,
        onboardingCompleted = onboardingCompleted,
    )

/**
 * 로그인과 토큰 재발급 응답이 공유하는 토큰 변환 로직입니다.
 */
private fun createAuthTokens(
    accessToken: String,
    refreshToken: String,
    tokenType: String,
    accessTokenExpiresIn: Long,
    refreshTokenExpiresIn: Long,
    currentEpochSeconds: Long,
): AuthTokens =
    AuthTokens(
        accessToken = accessToken,
        refreshToken = refreshToken,
        tokenType = tokenType,
        accessTokenExpiresAtEpochSeconds =
            currentEpochSeconds + accessTokenExpiresIn,
        refreshTokenExpiresAtEpochSeconds =
            currentEpochSeconds + refreshTokenExpiresIn,
    )
