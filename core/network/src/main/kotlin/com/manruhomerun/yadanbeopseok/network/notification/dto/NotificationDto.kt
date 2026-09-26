package com.manruhomerun.yadanbeopseok.network.notification.dto

import kotlinx.serialization.Serializable

/** 알림 센터에 표시할 개별 알림 응답입니다. */
@Serializable
data class NotificationResponseDto(
    val notificationId: Long,
    val type: String,
    val title: String,
    val body: String,
    val referenceId: String? = null,
    val createdAt: String,
)

/** 현재 사용자의 알림 설정 응답입니다. */
@Serializable
data class NotificationSettingResponseDto(
    val friendNotificationEnabled: Boolean,
    val weeklyTeamScheduleNotificationEnabled: Boolean,
)

/** 친구 알림과 주간 경기 일정 알림 설정 수정 요청입니다. */
@Serializable
data class NotificationSettingUpdateRequestDto(
    val friendNotificationEnabled: Boolean,
    val weeklyTeamScheduleNotificationEnabled: Boolean,
)

/** FCM 수신을 위한 앱 설치 등록 요청입니다. */
@Serializable
data class PushRegistrationRequestDto(val installationId: String, val appVersion: String? = null)
