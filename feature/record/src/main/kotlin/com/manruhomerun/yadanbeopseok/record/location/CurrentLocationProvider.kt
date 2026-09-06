package com.manruhomerun.yadanbeopseok.record.location

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.location.LocationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

sealed interface CurrentLocationResult {
    data class Success(val location: Location) : CurrentLocationResult
    data object PermissionRequired : CurrentLocationResult
    data object LocationDisabled : CurrentLocationResult
    data object Unavailable : CurrentLocationResult
}

/**
 * 방문 인증에 사용할 기기의 현재 위치를 한 번 조회합니다.
 *
 * 위치 권한과 기기 위치 서비스 상태를 확인한 뒤,
 * 사용 가능한 위치 제공자를 통해 좌표와 정확도 등의 정보를 가져옵니다.
 * 지속적인 위치 추적이나 위치 저장은 하지 않습니다.
 *
 * 결과는 위치 조회 성공, 권한 부족, 위치 서비스 꺼짐, 조회 불가로 구분합니다.
 * 위치 조회 성공은 방문 인증 성공을 의미하지 않습니다.
 *
 * 권한 요청 화면, 서버 방문 인증과 화면 이동은 이 클래스에서 처리하지 않습니다.
 */
class CurrentLocationProvider @Inject constructor(@ApplicationContext private val context: Context) {
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): CurrentLocationResult {
        val hasFineLocation = hasPermission(ACCESS_FINE_LOCATION)
        if (!hasFineLocation && !hasPermission(ACCESS_COARSE_LOCATION)) return CurrentLocationResult.PermissionRequired
        val manager = context.getSystemService(LocationManager::class.java)
            ?: return CurrentLocationResult.Unavailable
        return try {
            if (!manager.isLocationEnabled) return CurrentLocationResult.LocationDisabled

            // Android 12 이상에서는 시스템 통합 위치(FUSED)를 우선 시도합니다.
            // 이후 권한과 OS 버전에 따라 GPS, 네트워크 위치를 후보에 넣습니다.
            // 아래 반복문에서 활성화된 제공자만 조회하고, 첫 유효 위치를 반환합니다.
            val providers = buildList {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(LocationManager.FUSED_PROVIDER)
                if (hasFineLocation || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(LocationManager.GPS_PROVIDER)
                add(LocationManager.NETWORK_PROVIDER)
            }
            for (provider in providers.filter(manager::isProviderEnabled)) {
                val location = suspendCancellableCoroutine<Location?> { continuation ->
                    val signal = CancellationSignal()
                    LocationManagerCompat.getCurrentLocation(
                        manager, provider, signal, context.mainExecutor,
                    ) { location -> continuation.resume(location) }

                    // 호출한 코루틴이 취소되면 Android 위치 요청도 함께 취소합니다.
                    // 요청 등록 중 코루틴이 이미 취소됐더라도 이 핸들러는 즉시 실행됩니다.
                    continuation.invokeOnCancellation { signal.cancel() }
                }
                if (!manager.isLocationEnabled) return CurrentLocationResult.LocationDisabled
                if (location != null) return CurrentLocationResult.Success(location)
            }
            CurrentLocationResult.Unavailable
        } catch (_: SecurityException) {
            CurrentLocationResult.PermissionRequired
        } catch (_: IllegalArgumentException) {
            CurrentLocationResult.Unavailable
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }
}
