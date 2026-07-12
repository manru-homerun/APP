package com.manruhomerun.yadanbeopseok.network.auth.dto

import kotlinx.serialization.Serializable

/**
 * 만료된 야단법석 access token을 재발급할 때 사용하는 요청 DTO입니다.
 *
 * 카카오 refresh token이 아니라 야단법석 백엔드가
 * 로그인 응답에서 발급한 refresh token을 전달합니다.
 *
 * @property refreshToken access token 재발급에 사용할 야단법석 refresh token
 */
@Serializable
data class TokenRefreshRequestDto(
    val refreshToken: String,
)
