package com.manruhomerun.yadanbeopseok.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/** 야단법석 FCM 알림 채널 생성과 포그라운드 알림 표시를 담당합니다. */
object YadanSystemNotificationManager {
    /** 시스템 알림 채널을 한 번 생성합니다. */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "야단법석 알림",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "친구 소식과 응원 구단의 주간 경기 일정 알림"
        }

        notificationManager.createNotificationChannel(channel)
    }

    /** Android 13 이상에서 알림 런타임 권한이 허용되어 있는지 확인합니다. */
    fun hasPostNotificationsPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    /** 권한, 앱 전체 알림과 야단법석 채널이 모두 활성화되어 있는지 확인합니다. */
    fun canPostNotifications(context: Context): Boolean {
        if (!hasPostNotificationsPermission(context)) return false
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return true

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val channel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
        return channel == null || channel.importance != NotificationManager.IMPORTANCE_NONE
    }

    /** 사용자가 앱과 알림 채널의 차단 상태를 변경할 수 있는 시스템 설정을 엽니다. */
    fun openNotificationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    }

    /** 포그라운드에서 수신한 FCM 메시지를 시스템 알림으로 표시합니다. */
    fun show(context: Context, payload: NotificationPayload, title: String, body: String) {
        if (!canPostNotifications(context)) return

        val launchIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putNotificationPayload(payload)
            }
            ?: return

        val requestCode = payload.notificationId?.hashCode() ?: payload.hashCode()
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.core_notifications_ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(requestCode, notification)
    }
}

const val NOTIFICATION_CHANNEL_ID = "yadan_notifications"
