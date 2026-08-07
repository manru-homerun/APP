package com.manruhomerun.yadanbeopseok.network.auth.dto

import kotlinx.serialization.Serializable

/**
 * 야단법석 백엔드에 로그아웃을 요청할 때 사용하는 DTO입니다.
 *
 * @property providerAccessToken 소셜 로그인 제공자가 발급한 액세스 토큰
 * @property deviceType 로그아웃하는 기기 타입
 * @property deviceId 로그아웃하는 기기의 식별자
 * @property fcmToken 비활성화할 기기의 FCM 토큰
 */
@Serializable
data class LogoutRequestDto(
    val providerAccessToken: String,
    val deviceType: String,
    val deviceId: String? = null,
    val fcmToken: String? = null,
)
