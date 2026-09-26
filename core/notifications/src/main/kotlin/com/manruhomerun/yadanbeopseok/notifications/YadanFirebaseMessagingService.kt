package com.manruhomerun.yadanbeopseok.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** 포그라운드에서 수신한 FCM 메시지를 시스템 알림으로 표시합니다. */
@AndroidEntryPoint
class YadanFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var pushRegistrationManager: FirebasePushRegistrationManager

    @Inject
    lateinit var notificationRefreshNotifier: NotificationRefreshNotifier

    override fun onRegistered(installationId: String) {
        pushRegistrationManager.notifyRegistered(installationId)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val payload = message.data.toNotificationPayload()
        val title = message.notification?.title ?: "야단법석"
        val body = message.notification?.body ?: "새로운 알림이 도착했습니다."

        notificationRefreshNotifier.notifyNotificationReceived()

        YadanSystemNotificationManager.show(
            context = this,
            payload = payload,
            title = title,
            body = body,
        )
    }
}
