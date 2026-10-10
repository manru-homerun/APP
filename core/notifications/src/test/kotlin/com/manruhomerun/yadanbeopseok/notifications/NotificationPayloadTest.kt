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

    @Test
    fun `여행 알림 FCM data는 여행 상세 목적지로 변환된다`() {
        val payload = mapOf(
            NOTIFICATION_ID_KEY to "106",
            NOTIFICATION_TYPE_KEY to "TRAVEL_REMINDER_D3",
            NOTIFICATION_REFERENCE_ID_KEY to "travel-106",
        ).toNotificationPayload()

        assertEquals(NotificationType.TRAVEL_REMINDER_D3, payload.type)
        assertEquals("106", payload.notificationId)
        assertEquals("travel-106", payload.referenceId)
        assertEquals(NotificationDestination.TravelDetail("travel-106"), payload.toDestination())
    }

    @Test
    fun `여행 ID는 숫자와 UUID를 포함한 문자열을 그대로 유지한다`() {
        val travelIds = listOf("123", "travel-106", "a21e59da-8a6f-46f7-b3be-5a894fb4dfd3")

        travelIds.forEach { travelId ->
            val destination = resolveNotificationDestination(NotificationType.TRAVEL_REMINDER_D3, travelId)

            assertEquals(NotificationDestination.TravelDetail(travelId), destination)
        }
    }

    @Test
    fun `여행 ID 앞뒤 공백은 제거한다`() {
        val destination = resolveNotificationDestination(NotificationType.TRAVEL_REMINDER_D3, "  travel-106 \t\n")

        assertEquals(NotificationDestination.TravelDetail("travel-106"), destination)
    }

    @Test
    fun `여행 ID가 없거나 공백이면 알림 센터로 이동한다`() {
        val invalidTravelIds = listOf(null, "", "   ", "\t\n")

        invalidTravelIds.forEach { travelId ->
            val destination = resolveNotificationDestination(NotificationType.TRAVEL_REMINDER_D3, travelId)

            assertEquals(NotificationDestination.NotificationCenter, destination)
        }
    }

    @Test
    fun `여행 알림 FCM data에 referenceId가 없으면 알림 센터로 이동한다`() {
        val payload = mapOf(
            NOTIFICATION_TYPE_KEY to "TRAVEL_REMINDER_D3",
        ).toNotificationPayload()

        assertEquals(NotificationType.TRAVEL_REMINDER_D3, payload.type)
        assertEquals(NotificationDestination.NotificationCenter, payload.toDestination())
    }

    @Test
    fun `알 수 없는 알림 타입은 알림 센터로 이동한다`() {
        val payload = mapOf(
            NOTIFICATION_TYPE_KEY to "UNSUPPORTED_TYPE",
            NOTIFICATION_REFERENCE_ID_KEY to "travel-106",
        ).toNotificationPayload()

        assertEquals(NotificationType.UNKNOWN, payload.type)
        assertEquals(NotificationDestination.NotificationCenter, payload.toDestination())
    }

    @Test
    fun `FCM data에 알림 타입이 없으면 알림 센터로 이동한다`() {
        val payload = mapOf(
            NOTIFICATION_REFERENCE_ID_KEY to "travel-106",
        ).toNotificationPayload()

        assertEquals(NotificationType.UNKNOWN, payload.type)
        assertEquals(NotificationDestination.NotificationCenter, payload.toDestination())
    }
}
