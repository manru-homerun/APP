package com.manruhomerun.yadanbeopseok.network.auth.dto

import kotlinx.serialization.Serializable

/**
 * 야단법석 토큰 재발급 성공 응답을 변환하는 DTO입니다.
 *
 * 백엔드는 새로운 access token만 반환하므로 기존 refresh token은 유지합니다.
 *
 * @property accessToken 새로 발급된 야단법석 access token
 */
@Serializable
data class TokenRefreshResponseDto(
    val accessToken: String,
)
