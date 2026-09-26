package com.manruhomerun.yadanbeopseok.notifications

import android.content.Intent
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.NotificationType

/** FCM data와 앱 실행 Intent가 공유하는 알림 이동 정보입니다. */
data class NotificationPayload(val notificationId: String?, val type: NotificationType, val referenceId: String?)

/** 알림 종류와 참조 ID를 검증한 앱 내부 이동 목적지입니다. */
sealed interface NotificationDestination {
    data object FriendRequests : NotificationDestination

    data object FriendList : NotificationDestination

    data class TeamSchedule(val teamId: Long) : NotificationDestination

    data object NotificationCenter : NotificationDestination
}

/** FCM data payload를 앱 내부 알림 정보로 변환합니다. */
fun Map<String, String>.toNotificationPayload(): NotificationPayload = NotificationPayload(
    notificationId = get(NOTIFICATION_ID_KEY),
    type = NotificationType.fromServerValue(get(NOTIFICATION_TYPE_KEY)),
    referenceId = get(NOTIFICATION_REFERENCE_ID_KEY),
)

/** 시스템 알림을 통해 전달된 Intent extra를 앱 내부 알림 정보로 변환합니다. */
fun Intent.toNotificationPayloadOrNull(): NotificationPayload? {
    val typeValue = getStringExtra(NOTIFICATION_TYPE_KEY) ?: return null

    return NotificationPayload(
        notificationId = getStringExtra(NOTIFICATION_ID_KEY),
        type = NotificationType.fromServerValue(typeValue),
        referenceId = getStringExtra(NOTIFICATION_REFERENCE_ID_KEY),
    )
}

/** Intent의 알림 정보를 한 번 읽고 재생성 시 중복 처리되지 않도록 제거합니다. */
fun Intent.consumeNotificationPayloadOrNull(): NotificationPayload? {
    val payload = toNotificationPayloadOrNull() ?: return null

    removeExtra(NOTIFICATION_ID_KEY)
    removeExtra(NOTIFICATION_TYPE_KEY)
    removeExtra(NOTIFICATION_REFERENCE_ID_KEY)
    return payload
}

/** 알림 정보를 MainActivity 실행 Intent에 추가합니다. */
fun Intent.putNotificationPayload(payload: NotificationPayload): Intent = apply {
    putExtra(NOTIFICATION_ID_KEY, payload.notificationId)
    putExtra(NOTIFICATION_TYPE_KEY, payload.type.name)
    putExtra(NOTIFICATION_REFERENCE_ID_KEY, payload.referenceId)
}

/** 알림을 눌렀을 때 이동할 화면을 안전하게 결정합니다. */
fun NotificationPayload.toDestination(): NotificationDestination = resolveNotificationDestination(
    type = type,
    referenceId = referenceId,
)

/** 알림 목록과 FCM 알림이 공유하는 화면 이동 규칙입니다. */
fun resolveNotificationDestination(type: NotificationType, referenceId: String?): NotificationDestination =
    when (type) {
        NotificationType.FRIEND_REQUEST ->
            NotificationDestination.FriendRequests

        NotificationType.FRIEND_REQUEST_ACCEPTED ->
            NotificationDestination.FriendList

        NotificationType.WEEKLY_TEAM_SCHEDULE -> {
            val teamId = referenceId?.toLongOrNull()
            if (teamId != null && KboTeam.findByServerId(teamId) != null) {
                NotificationDestination.TeamSchedule(teamId)
            } else {
                NotificationDestination.NotificationCenter
            }
        }

        NotificationType.UNKNOWN ->
            NotificationDestination.NotificationCenter
    }

const val NOTIFICATION_ID_KEY = "notificationId"
const val NOTIFICATION_TYPE_KEY = "type"
const val NOTIFICATION_REFERENCE_ID_KEY = "referenceId"
