package com.manruhomerun.yadanbeopseok.notifications

import com.manruhomerun.yadanbeopseok.model.NotificationType
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationPayloadTest {
    @Test
    fun `친구 신청 알림은 받은 친구 요청 화면으로 이동한다`() {
        val destination = NotificationPayload(
            notificationId = "101",
            type = NotificationType.FRIEND_REQUEST,
            referenceId = "25",
        ).toDestination()

        assertEquals(NotificationDestination.FriendRequests, destination)
    }

    @Test
    fun `친구 수락 알림은 친구 목록 화면으로 이동한다`() {
        val destination = NotificationPayload(
            notificationId = "102",
            type = NotificationType.FRIEND_REQUEST_ACCEPTED,
            referenceId = "25",
        ).toDestination()

        assertEquals(NotificationDestination.FriendList, destination)
    }

    @Test
    fun `주간 경기 일정 알림은 referenceId 구단 일정으로 이동한다`() {
        val destination = NotificationPayload(
            notificationId = "103",
            type = NotificationType.WEEKLY_TEAM_SCHEDULE,
            referenceId = "2",
        ).toDestination()

        assertEquals(
            NotificationDestination.TeamSchedule(teamId = 2L),
            destination,
        )
    }

    @Test
    fun `주간 경기 일정의 구단 ID가 유효하지 않으면 알림 센터로 이동한다`() {
        val destination = NotificationPayload(
            notificationId = "104",
            type = NotificationType.WEEKLY_TEAM_SCHEDULE,
            referenceId = "999",
        ).toDestination()

        assertEquals(NotificationDestination.NotificationCenter, destination)
    }

    @Test
    fun `FCM data는 알림 payload로 변환된다`() {
        val payload = mapOf(
            NOTIFICATION_ID_KEY to "105",
            NOTIFICATION_TYPE_KEY to "FRIEND_REQUEST",
            NOTIFICATION_REFERENCE_ID_KEY to "31",
        ).toNotificationPayload()

        assertEquals(
            NotificationPayload(
                notificationId = "105",
                type = NotificationType.FRIEND_REQUEST,
                referenceId = "31",
            ),
            payload,
        )
    }
}
