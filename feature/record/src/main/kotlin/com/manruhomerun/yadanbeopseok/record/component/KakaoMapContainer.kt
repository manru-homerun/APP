package com.manruhomerun.yadanbeopseok.record.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView

/**
 * 카카오맵의 생성, 생명주기 연결과 오류 처리를 공통으로 담당합니다.
 *
 * 지도별 카메라 설정과 마커 추가는 [onMapReady]에서 처리합니다.
 */
@Composable
internal fun KakaoMapContainer(
    initialPosition: LatLng,
    initialZoomLevel: Int,
    onMapReady: (KakaoMap) -> Unit,
    onError: () -> Unit,
    modifier: Modifier = Modifier,
    onMapReleased: () -> Unit = {},
    previewContent: @Composable BoxScope.() -> Unit = {},
) {
    if (LocalInspectionMode.current) {
        Box(
            modifier = modifier,
            content = previewContent,
        )
        return
    }

    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val latestPosition by rememberUpdatedState(initialPosition)
    val latestZoomLevel by rememberUpdatedState(initialZoomLevel)
    val latestOnMapReady by rememberUpdatedState(onMapReady)
    val latestOnError by rememberUpdatedState(onError)
    val latestOnMapReleased by rememberUpdatedState(onMapReleased)

    val mapView = remember(context, lifecycle) {
        MapView(context)
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
    )

    DisposableEffect(mapView, lifecycle) {
        var isClosed = false
        var hasFailed = false

        fun releaseMap() {
            latestOnMapReleased()
        }

        fun reportError() {
            if (isClosed || hasFailed) return

            hasFailed = true
            releaseMap()
            latestOnError()
        }

        fun closeMap() {
            if (isClosed) return

            isClosed = true
            releaseMap()
            mapView.pause()
            mapView.finish()
        }

        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (isClosed) return@LifecycleEventObserver

            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.resume()
                Lifecycle.Event.ON_PAUSE -> mapView.pause()
                Lifecycle.Event.ON_DESTROY -> closeMap()
                else -> Unit
            }
        }

        if (lifecycle.currentState != Lifecycle.State.DESTROYED) {
            val lifeCycleCallback = object : MapLifeCycleCallback() {
                override fun onMapDestroy() {
                    context.mainExecutor.execute {
                        reportError()
                    }
                }

                override fun onMapError(error: Exception) {
                    context.mainExecutor.execute {
                        reportError()
                    }
                }
            }

            val readyCallback = object : KakaoMapReadyCallback() {
                override fun getPosition(): LatLng = latestPosition

                override fun getZoomLevel(): Int = latestZoomLevel

                override fun onMapReady(kakaoMap: KakaoMap) {
                    context.mainExecutor.execute {
                        if (isClosed || hasFailed) return@execute

                        try {
                            latestOnMapReady(kakaoMap)
                        } catch (_: Exception) {
                            reportError()
                            return@execute
                        }

                        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                            mapView.pause()
                        }
                    }
                }
            }

            try {
                mapView.start(
                    lifeCycleCallback,
                    readyCallback,
                )
                mapView.setFinishManually(true)
                lifecycle.addObserver(lifecycleObserver)
            } catch (_: Exception) {
                reportError()
            }
        }

        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
            closeMap()
        }
    }
}
