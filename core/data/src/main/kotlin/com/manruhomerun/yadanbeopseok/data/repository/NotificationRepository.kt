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
    suspend fun updateNotificationSetting(setting: NotificationSetting)

    /** 현재 앱 설치의 FID를 서버의 알림 수신 대상으로 등록합니다. */
    suspend fun registerPushInstallation(installationId: String, appVersion: String? = null)

    /** 현재 앱 설치의 서버 연결과 FCM 등록을 해제합니다. */
    suspend fun unregisterPushInstallation()
}
