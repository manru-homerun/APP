package com.manruhomerun.yadanbeopseok.network.auth.dto

import kotlinx.serialization.Serializable

/**
 * 야단법석 백엔드 로그인 성공 응답을 변환하는 DTO입니다.
 *
 * @property accessToken 야단법석 API 인증에 사용할 access token
 * @property refreshToken access token 재발급에 사용할 refresh token
 * @property onboardingCompleted 온보딩 완료 여부
 */
@Serializable
data class LoginResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val onboardingCompleted: Boolean,
)
