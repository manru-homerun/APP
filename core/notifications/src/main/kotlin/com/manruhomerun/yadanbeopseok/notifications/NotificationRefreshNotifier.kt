package com.manruhomerun.yadanbeopseok.notifications

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** 포그라운드에서 새 알림을 수신했음을 알림 목록에 전달합니다. */
@Singleton
class NotificationRefreshNotifier @Inject constructor() {
    private val _events = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    /** FCM 메시지 수신으로 알림 목록을 다시 조회해야 함을 알립니다. */
    fun notifyNotificationReceived() {
        _events.tryEmit(Unit)
    }
}
