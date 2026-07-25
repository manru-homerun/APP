package com.manruhomerun.yadanbeopseok.datastore

/**
 * Preferences DataStore에 저장하는 야단법석 서비스 인증 정보입니다.
 *
 * 카카오 액세스 토큰이 아니라 야단법석 백엔드가 로그인 또는
 * 토큰 재발급 응답으로 발급한 서비스 전용 토큰을 나타냅니다.
 *
 * @property accessToken 야단법석 API 인증에 사용할 access token
 * @property refreshToken access token 재발급에 사용할 refresh token
 * @property tokenType Authorization 헤더에 사용할 토큰 타입
 * @property accessTokenExpiresAtEpochSeconds access token 만료 시각(Unix epoch 초)
 * @property refreshTokenExpiresAtEpochSeconds refresh token 만료 시각(Unix epoch 초)
 */
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val accessTokenExpiresAtEpochSeconds: Long,
    val refreshTokenExpiresAtEpochSeconds: Long,
)
