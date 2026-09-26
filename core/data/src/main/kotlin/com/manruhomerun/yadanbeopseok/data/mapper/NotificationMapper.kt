package com.manruhomerun.yadanbeopseok.data.mapper

import com.manruhomerun.yadanbeopseok.model.AppNotification
import com.manruhomerun.yadanbeopseok.model.NotificationSetting
import com.manruhomerun.yadanbeopseok.model.NotificationType
import com.manruhomerun.yadanbeopseok.network.notification.dto.NotificationResponseDto
import com.manruhomerun.yadanbeopseok.network.notification.dto.NotificationSettingResponseDto
import com.manruhomerun.yadanbeopseok.network.notification.dto.NotificationSettingUpdateRequestDto
import kotlinx.datetime.LocalDateTime

/** 알림 응답을 앱 내부 알림 모델로 변환합니다. */
internal fun NotificationResponseDto.toAppNotification(): AppNotification = AppNotification(
    id = notificationId,
    type = NotificationType.fromServerValue(type),
    title = title,
    body = body,
    referenceId = referenceId,
    createdAt = LocalDateTime.parse(createdAt.replace(' ', 'T')),
)

/** 알림 설정 응답을 앱 내부 설정 모델로 변환합니다. */
internal fun NotificationSettingResponseDto.toNotificationSetting(): NotificationSetting = NotificationSetting(
    friendNotificationEnabled = friendNotificationEnabled,
    weeklyTeamScheduleNotificationEnabled = weeklyTeamScheduleNotificationEnabled,
)

/** 앱 내부 알림 설정을 서버 수정 요청으로 변환합니다. */
internal fun NotificationSetting.toUpdateRequest(): NotificationSettingUpdateRequestDto =
    NotificationSettingUpdateRequestDto(
        friendNotificationEnabled = friendNotificationEnabled,
        weeklyTeamScheduleNotificationEnabled = weeklyTeamScheduleNotificationEnabled,
    )
