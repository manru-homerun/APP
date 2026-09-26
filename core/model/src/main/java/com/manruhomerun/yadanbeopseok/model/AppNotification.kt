package com.manruhomerun.yadanbeopseok.model

import kotlinx.datetime.LocalDateTime

/**
 * 앱 내부에서 사용하는 알림 모델입니다.
 *
 * ERD의 notification 테이블을 참고합니다.
 */
data class AppNotification(
    val id: Long,
    val type: NotificationType,
    val title: String,
    val body: String,
    val referenceId: String?,
    val createdAt: LocalDateTime,
)

enum class NotificationType {
    FRIEND_REQUEST,
    FRIEND_REQUEST_ACCEPTED,
    WEEKLY_TEAM_SCHEDULE,
    UNKNOWN,
    ;

    companion object {
        /** 서버 또는 FCM payload의 알림 타입을 앱 내부 타입으로 변환합니다. */
        fun fromServerValue(value: String?): NotificationType = entries.firstOrNull { type ->
            type != UNKNOWN && type.name == value
        } ?: UNKNOWN
    }
}

/**
 * 앱 내부에서 사용하는 알림 설정 모델입니다.
 *
 * ERD의 notification_setting에 대응됩니다.
 */
data class NotificationSetting(
    val friendNotificationEnabled: Boolean,
    val weeklyTeamScheduleNotificationEnabled: Boolean,
)
