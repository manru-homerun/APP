package com.manruhomerun.yadanbeopseok.notification.viewmodel

import com.manruhomerun.yadanbeopseok.model.AppNotification
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/** E·01 알림 목록 화면에 표시할 개별 항목입니다. */
data class NotificationListItem(val notification: AppNotification, val timeText: String)

/** E·01 알림 목록 화면의 상태입니다. */
data class NotificationUiState(
    val notifications: List<NotificationListItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

/** 서버의 알림 생성 시각을 알림 목록용 상대 시간으로 변환합니다. */
internal fun AppNotification.toNotificationListItem(now: Instant = Clock.System.now()): NotificationListItem {
    val createdInstant = createdAt.toInstant(SEOUL_TIME_ZONE)
    val elapsed = now - createdInstant
    val elapsedMinutes = elapsed.inWholeMinutes.coerceAtLeast(0)

    val timeText = when {
        elapsedMinutes < 1 -> "방금 전"
        elapsedMinutes < MINUTES_PER_HOUR -> "${elapsedMinutes}분 전"
        elapsedMinutes < MINUTES_PER_DAY -> "${elapsed.inWholeHours}시간 전"
        elapsed.inWholeDays < DAYS_PER_WEEK -> "${elapsed.inWholeDays}일 전"
        else -> "${createdAt.month.ordinal + 1}.${createdAt.day}"
    }

    return NotificationListItem(
        notification = this,
        timeText = timeText,
    )
}

private val SEOUL_TIME_ZONE = TimeZone.of("Asia/Seoul")
private const val MINUTES_PER_HOUR = 60
private const val MINUTES_PER_DAY = 1_440
private const val DAYS_PER_WEEK = 7
