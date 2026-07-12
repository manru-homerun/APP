package com.manruhomerun.yadanbeopseok.network.auth.dto

import kotlinx.serialization.Serializable

/**
 * 야단법석 토큰 재발급 성공 응답의 data를 변환하는 DTO입니다.
 *
 * 재발급 응답으로 access token과 refresh token을 모두 받으며,
 * 두 토큰은 기존 값과 함께 교체해서 저장해야 합니다.
 *
 * @property accessToken 새로 발급된 야단법석 access token
 * @property refreshToken 새로 발급된 야단법석 refresh token
 * @property tokenType 인증 헤더에 사용하는 토큰 타입
 * @property accessTokenExpiresIn access token의 남은 유효 시간(초)
 * @property refreshTokenExpiresIn refresh token의 남은 유효 시간(초)
 */
@Serializable
data class TokenRefreshResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long,
)
