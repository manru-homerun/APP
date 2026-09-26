package com.manruhomerun.yadanbeopseok.notifications

import com.google.android.gms.tasks.Task
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** FCM의 FID 기반 앱 설치 등록과 해제를 담당합니다. */
@Singleton
class FirebasePushRegistrationManager @Inject constructor() {
    private val registrationEnabled = AtomicBoolean(false)
    private val registrationMutex = Mutex()

    private val _registeredInstallationIds = MutableSharedFlow<String?>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val registeredInstallationIds: SharedFlow<String?> = _registeredInstallationIds.asSharedFlow()

    val isRegistrationEnabled: Boolean
        get() = registrationEnabled.get()

    init {
        _registeredInstallationIds.tryEmit(null)
    }

    /** 인증된 세션에서 FCM 등록과 서버 FID 연결을 허용합니다. */
    fun enableRegistration() {
        registrationEnabled.set(true)
    }

    /** 등록을 비활성화하고 이전 활성 상태를 반환합니다. */
    fun disableRegistration(): Boolean {
        val wasEnabled = registrationEnabled.getAndSet(false)
        _registeredInstallationIds.tryEmit(null)
        return wasEnabled
    }

    /** 현재 앱 설치를 FCM에 등록합니다. */
    suspend fun register() {
        registrationMutex.withLock {
            if (!isRegistrationEnabled) return@withLock

            withContext(NonCancellable) {
                FirebaseMessaging.getInstance().register().awaitCompletion()
            }
        }
    }

    /** Firebase 콜백으로 받은 최신 FID를 인증된 세션에 전달합니다. */
    fun notifyRegistered(installationId: String) {
        if (!isRegistrationEnabled || installationId.isBlank()) return
        _registeredInstallationIds.tryEmit(installationId)
    }

    /** 현재 앱 설치의 FID를 반환합니다. */
    suspend fun getInstallationId(): String = FirebaseInstallations.getInstance().id.awaitResult()

    /** FID는 유지하면서 현재 앱 설치의 FCM 수신 등록을 해제합니다. */
    suspend fun unregister() {
        registrationMutex.withLock {
            if (isRegistrationEnabled) return@withLock

            withContext(NonCancellable) {
                FirebaseMessaging.getInstance().unregister().awaitCompletion()
            }
            _registeredInstallationIds.tryEmit(null)
        }
    }
}

/** Google Task의 성공 또는 실패가 결정될 때까지 코루틴을 중단합니다. */
private suspend fun Task<*>.awaitCompletion() {
    suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (!continuation.isActive) return@addOnCompleteListener

            val exception = task.exception
            if (task.isSuccessful) {
                continuation.resume(Unit)
            } else {
                continuation.resumeWithException(
                    exception ?: IllegalStateException("Firebase task failed."),
                )
            }
        }
    }
}

/** 결과가 있는 Google Task의 완료 값을 코루틴에서 반환합니다. */
private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (!continuation.isActive) return@addOnCompleteListener

        val exception = task.exception
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(
                exception ?: IllegalStateException("Firebase task failed."),
            )
        }
    }
}
