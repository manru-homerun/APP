package com.manruhomerun.yadanbeopseok.data.repository.impl

import com.manruhomerun.yadanbeopseok.data.mapper.toAppNotification
import com.manruhomerun.yadanbeopseok.data.mapper.toNotificationSetting
import com.manruhomerun.yadanbeopseok.data.mapper.toUpdateRequest
import com.manruhomerun.yadanbeopseok.data.repository.NotificationRepository
import com.manruhomerun.yadanbeopseok.model.AppNotification
import com.manruhomerun.yadanbeopseok.model.NotificationSetting
import com.manruhomerun.yadanbeopseok.network.common.error.ApiCallExecutor
import com.manruhomerun.yadanbeopseok.network.notification.api.NotificationApi
import com.manruhomerun.yadanbeopseok.network.notification.dto.PushRegistrationRequestDto
import com.manruhomerun.yadanbeopseok.notifications.FirebasePushRegistrationManager
import javax.inject.Inject
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** 알림 조회·설정과 FCM 앱 설치 등록을 제공하는 Repository 구현체입니다. */
internal class NotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi,
    private val pushRegistrationManager: FirebasePushRegistrationManager,
    private val apiCallExecutor: ApiCallExecutor,
) : NotificationRepository {
    private val pushRegistrationMutex = Mutex()

    override suspend fun getNotifications(): List<AppNotification> {
        val response = apiCallExecutor.execute {
            notificationApi.getNotifications()
        }

        return response.map { notification ->
            notification.toAppNotification()
        }
    }

    override suspend fun getNotificationSetting(): NotificationSetting {
        val response = apiCallExecutor.execute {
            notificationApi.getNotificationSetting()
        }

        return response.toNotificationSetting()
    }

    override suspend fun updateNotificationSetting(setting: NotificationSetting) {
        apiCallExecutor.execute {
            notificationApi.updateNotificationSetting(setting.toUpdateRequest())
        }
    }

    override suspend fun registerPushInstallation(installationId: String, appVersion: String?) {
        pushRegistrationMutex.withLock {
            if (!pushRegistrationManager.isRegistrationEnabled) return@withLock

            apiCallExecutor.execute {
                notificationApi.registerPushInstallation(
                    PushRegistrationRequestDto(
                        installationId = installationId,
                        appVersion = appVersion,
                    ),
                )
            }
        }
    }

    override suspend fun unregisterPushInstallation() {
        pushRegistrationManager.disableRegistration()

        try {
            pushRegistrationMutex.withLock {
                val installationId = pushRegistrationManager.getInstallationId()

                apiCallExecutor.execute {
                    notificationApi.unregisterPushInstallation(installationId)
                }
            }
        } finally {
            withContext(NonCancellable) {
                pushRegistrationManager.unregister()
            }
        }
    }
}
