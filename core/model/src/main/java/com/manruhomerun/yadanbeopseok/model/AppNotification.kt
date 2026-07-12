package com.manruhomerun.yadanbeopseok.model

import kotlinx.datetime.LocalDateTime

/**
 * 앱 내부에서 사용하는 알림 모델입니다.
 *
 * ERD의 notification 테이블을 참고합니다.
 */
data class AppNotification(
    val id: String,
    val userId: String,
    val type: NotificationType,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime,
)

enum class NotificationType {
    TICKET_OPEN,
    VISIT_CERTIFICATION_REMINDER,
    NEARBY_GAME,
    FRIEND_REQUEST,
    FRIEND_ACCEPTED,
    TRAVEL_RECOMMENDATION,
    UNKNOWN,
}

/**
 * 앱 내부에서 사용하는 알림 설정 모델입니다.
 *
 * ERD의 notification_setting에 대응됩니다.
 */
data class NotificationSetting(
    val userId: String,
    val ticketOpenNotificationEnabled: Boolean,
    val visitCertificationReminderEnabled: Boolean,
    val nearbyGameNotificationEnabled: Boolean,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)
