package com.manruhomerun.yadanbeopseok.network.auth.dto

import kotlinx.serialization.Serializable

/**
 * 카카오 액세스 토큰으로 야단법석 백엔드 로그인을 요청할 때 사용하는 DTO입니다.
 *
 * @property providerAccessToken Android Kakao SDK에서 발급받은 카카오 액세스 토큰
 * @property deviceType 로그인 요청을 보내는 기기 타입
 * @property fcmToken Firebase에서 발급받은 푸시 알림용 FCM 토큰
 */
@Serializable
data class LoginRequestDto(
    val providerAccessToken: String,
    val deviceType: String,
    val fcmToken: String? = null,
)
