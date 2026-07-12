package com.manruhomerun.yadanbeopseok.data.repository

import com.manruhomerun.yadanbeopseok.model.AppNotification
import com.manruhomerun.yadanbeopseok.model.NotificationSetting

interface NotificationRepository {
    /**
     * 현재 로그인한 사용자의 알림 목록을 조회합니다.
     */
    suspend fun getNotifications(): List<AppNotification>

    /**
     * 현재 로그인한 사용자의 알림 설정을 조회합니다.
     */
    suspend fun getNotificationSetting(): NotificationSetting

    /**
     * 현재 로그인한 사용자의 알림 설정을 수정합니다.
     */
    suspend fun updateNotificationSetting(
        setting: NotificationSetting,
    ): NotificationSetting
}
