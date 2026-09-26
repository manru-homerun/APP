package com.manruhomerun.yadanbeopseok.network.notification.api

import com.manruhomerun.yadanbeopseok.network.notification.dto.NotificationResponseDto
import com.manruhomerun.yadanbeopseok.network.notification.dto.NotificationSettingResponseDto
import com.manruhomerun.yadanbeopseok.network.notification.dto.NotificationSettingUpdateRequestDto
import com.manruhomerun.yadanbeopseok.network.notification.dto.PushRegistrationRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** 현재 사용자의 알림과 FCM 설치 등록 API입니다. */
interface NotificationApi {
    @GET("users/me/notifications")
    suspend fun getNotifications(): List<NotificationResponseDto>

    @GET("users/me/notification-settings")
    suspend fun getNotificationSetting(): NotificationSettingResponseDto

    @PUT("users/me/notification-settings")
    suspend fun updateNotificationSetting(@Body request: NotificationSettingUpdateRequestDto)

    @POST("users/me/push-registrations")
    suspend fun registerPushInstallation(@Body request: PushRegistrationRequestDto)

    @DELETE("users/me/push-registrations/{installationId}")
    suspend fun unregisterPushInstallation(@Path("installationId") installationId: String)
}
