package com.manruhomerun.yadanbeopseok.network.auth.dto

import kotlinx.serialization.Serializable

/**
 * 야단법석 백엔드 로그인 성공 응답의 data를 변환하는 DTO입니다.
 *
 * @property userId 야단법석 서버에서 사용하는 사용자 ID
 * @property accessToken 야단법석 API 인증에 사용할 access token
 * @property refreshToken access token 재발급에 사용할 refresh token
 * @property tokenType 인증 헤더에 사용하는 토큰 타입
 * @property accessTokenExpiresIn access token의 남은 유효 시간(초)
 * @property refreshTokenExpiresIn refresh token의 남은 유효 시간(초)
 * @property isNewUser 이번 로그인 과정에서 새로 생성된 회원인지 여부
 * @property onboardingCompleted 온보딩 완료 여부
 * @property provider 로그인에 사용한 소셜 로그인 제공자
 */
@Serializable
data class LoginResponseDto(
    val userId: Long,
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long,
    val isNewUser: Boolean,
    val onboardingCompleted: Boolean,
    val provider: String,
)
