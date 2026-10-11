package com.manruhomerun.yadanbeopseok.record.component

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.animation.Interpolation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.CompetitionType
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelAnimator
import com.kakao.vectormap.label.LabelLayer
import com.kakao.vectormap.label.LabelLayerOptions
import com.kakao.vectormap.label.LabelManager
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.animation.ScaleAlphaAnimation
import com.kakao.vectormap.label.animation.ScaleAlphaAnimations
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTint
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * D02에서 조회한 현재 위치를 지도와 마커로 표시합니다.
 *
 * 좌표가 변경되면 마커와 지도 중심을 갱신합니다.
 * 강조 원은 현재 위치 마커를 따라가며, 정확도나 인증 가능 거리를 나타내지 않습니다.
 * 지도 준비는 방문 인증 성공을 의미하지 않습니다.
 * 크기는 호출하는 Screen에서 modifier로 지정합니다.
 */
@Composable
fun TravelVerificationMap(
    latitude: Double,
    longitude: Double,
    onError: () -> Unit,
    modifier: Modifier = Modifier,
    onReady: () -> Unit = {},
) {
    val latestOnError by rememberUpdatedState(onError)
    val latestOnReady by rememberUpdatedState(onReady)

    val isValidPosition = latitude.isFinite() &&
        longitude.isFinite() &&
        latitude in -90.0..90.0 &&
        longitude in -180.0..180.0

    if (!isValidPosition) {
        LaunchedEffect(latitude, longitude) {
            latestOnError()
        }

        Box(
            modifier = modifier.background(YadanPrimaryTint),
        )
        return
    }

    val position = LatLng.from(latitude, longitude)
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    var map by remember {
        mutableStateOf<KakaoMap?>(null)
    }

    var marker by remember {
        mutableStateOf<Label?>(null)
    }
    var pulse by remember { mutableStateOf<LocationPulse?>(null) }

    val markerBitmap = remember {
        createLocationMarker()
    }
    val pulseBitmap = remember { createLocationPulseBitmap() }

    fun releaseMap() {
        val currentPulse = pulse
        val currentMarker = marker
        pulse = null
        marker = null
        map = null

        currentPulse?.release()
        runCatching { currentMarker?.remove() }
    }

    Box(modifier = modifier.background(YadanPrimaryTint)) {
        KakaoMapContainer(
            initialPosition = position,
            initialZoomLevel = LOCATION_ZOOM_LEVEL,
            onMapReady = { kakaoMap ->
                releaseMap()
                val labelManager = checkNotNull(kakaoMap.labelManager)
                val labelLayer = checkNotNull(labelManager.layer)

                val markerStyle = LabelStyle
                    .from(markerBitmap)
                    .setAnchorPoint(0.5f, 0.5f)

                val markerOptions = LabelOptions
                    .from(position)
                    .setStyles(markerStyle)
                    .setClickable(false)

                val currentMarker = checkNotNull(labelLayer.addLabel(markerOptions))
                marker = currentMarker
                map = kakaoMap
                pulse = createLocationPulse(labelManager, currentMarker, position, pulseBitmap)

                latestOnReady()
            },
            onError = {
                releaseMap()
                latestOnError()
            },
            onMapReleased = {
                releaseMap()
            },
            modifier = Modifier.matchParentSize(),
            previewContent = {
                Text(
                    text = "현재 위치 지도",
                    color = YadanPrimaryInk,
                    modifier = Modifier.align(Alignment.Center),
                )
            },
        )
    }

    DisposableEffect(pulse, lifecycle) {
        val currentPulse = pulse
        currentPulse?.observe(lifecycle)

        onDispose {
            currentPulse?.release()
        }
    }

    LaunchedEffect(map, latitude, longitude) {
        val currentMap = map ?: return@LaunchedEffect
        val currentMarker = marker ?: return@LaunchedEffect
        val currentPosition = LatLng.from(latitude, longitude)

        try {
            currentMarker.moveTo(currentPosition)
            currentMap.moveCamera(
                CameraUpdateFactory.newCenterPosition(currentPosition),
            )
        } catch (_: Exception) {
            releaseMap()
            latestOnError()
        }
    }
}

@Preview(name = "D02 현재 위치 지도", showBackground = true, widthDp = 360)
@Composable
private fun TravelVerificationMapPreview() {
    YadanbeopseokTheme {
        TravelVerificationMap(
            latitude = 35.0975,
            longitude = 129.0106,
            onError = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
        )
    }
}

/** 지도 SDK가 밀도를 적용하는 현재 위치 마커 이미지입니다. */
private fun createLocationMarker(): Bitmap {
    val bitmap = createBitmap(32, 32)
    bitmap.density = Bitmap.DENSITY_NONE

    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    paint.color = YadanSurface.toArgb()
    canvas.drawCircle(16f, 16f, 12f, paint)

    paint.color = YadanPrimary.toArgb()
    canvas.drawCircle(16f, 16f, 9f, paint)

    return bitmap
}

/** SDK가 한 번만 밀도를 적용하는 현재 위치 강조 원입니다. */
private fun createLocationPulseBitmap(): Bitmap {
    val bitmap = createBitmap(LOCATION_PULSE_SIZE, LOCATION_PULSE_SIZE)
    bitmap.density = Bitmap.DENSITY_NONE

    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val center = LOCATION_PULSE_SIZE / 2f

    paint.color = YadanPrimary.copy(alpha = 0.15f).toArgb()
    canvas.drawCircle(center, center, center, paint)

    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 2f
    paint.color = YadanPrimary.copy(alpha = 0.45f).toArgb()
    canvas.drawCircle(center, center, center - paint.strokeWidth / 2f, paint)

    return bitmap
}

/** 강조 효과 생성 실패는 정상 지도와 현재 위치 마커를 숨기지 않습니다. */
private fun createLocationPulse(
    labelManager: LabelManager,
    marker: Label,
    position: LatLng,
    bitmap: Bitmap,
): LocationPulse? {
    var layer: LabelLayer? = null
    var label: Label? = null

    return try {
        val pulseLayer = checkNotNull(
            labelManager.addLayer(
                LabelLayerOptions.from()
                    .setCompetitionType(CompetitionType.None)
                    .setClickable(false)
                    .setZOrder(LabelManager.DEFAULT_Z_ORDER - 1),
            ),
        )
        layer = pulseLayer

        val pulseStyle = LabelStyle.from(bitmap)
            .setAnchorPoint(0.5f, 0.5f)
            .setApplyDpScale(true)
        val pulseLabel = checkNotNull(
            pulseLayer.addLabel(
                LabelOptions.from(position)
                    .setStyles(pulseStyle)
                    .setClickable(false)
                    .setVisible(false),
            ),
        )
        label = pulseLabel

        marker.addSharePosition(pulseLabel)
        LocationPulse(labelManager, marker, pulseLayer, pulseLabel)
    } catch (_: Exception) {
        label?.let { pulseLabel ->
            runCatching { marker.removeSharePosition(pulseLabel) }
            runCatching { pulseLabel.remove() }
        }
        layer?.let { pulseLayer -> runCatching { labelManager.remove(pulseLayer) } }
        null
    }
}

/** 현재 위치 강조 효과의 반복과 소유한 SDK 객체의 해제를 관리합니다. */
private class LocationPulse(
    private val labelManager: LabelManager,
    private val marker: Label,
    private val layer: LabelLayer,
    private val label: Label,
) {
    private var lifecycle: Lifecycle? = null
    private var animator: LabelAnimator? = null
    private var activation: Any? = null
    private var isReleased = false

    private val lifecycleObserver = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> start()
            Lifecycle.Event.ON_PAUSE -> stop()
            Lifecycle.Event.ON_DESTROY -> release()
            else -> Unit
        }
    }

    /** 화면이 활성 상태일 때만 강조 효과를 시작합니다. */
    fun observe(lifecycle: Lifecycle) {
        if (isReleased || this.lifecycle === lifecycle) return

        this.lifecycle?.removeObserver(lifecycleObserver)
        this.lifecycle = lifecycle

        if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
            release()
            return
        }

        lifecycle.addObserver(lifecycleObserver)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) start()
    }

    /** 신규 Animator에 한 번만 등록하고, 자연 종료에서는 start만 반복합니다. */
    private fun start() {
        if (isReleased || animator != null) return
        if (lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) != true) return

        val currentActivation = Any()
        activation = currentActivation

        try {
            val animation = ScaleAlphaAnimations.from(
                ScaleAlphaAnimation.from(LOCATION_PULSE_END_SCALE, LOCATION_PULSE_END_SCALE, 0f)
                    .setDuration(LOCATION_PULSE_DURATION_MILLIS)
                    .setInterpolation(Interpolation.CubicOut),
            )
                .setInitScale(LOCATION_PULSE_START_SCALE, LOCATION_PULSE_START_SCALE)
                .setInitAlpha(LOCATION_PULSE_START_ALPHA)
                .setHideLabelAtStop(false)
                .setRemoveLabelAtStop(false)
                .setResetToInitialState(true)
            val currentAnimator = checkNotNull(labelManager.addAnimator(animation))
            animator = currentAnimator

            currentAnimator.setOnAnimationStopListener { finishedAnimator ->
                if (isReleased || activation !== currentActivation) return@setOnAnimationStopListener
                if (animator !== finishedAnimator) return@setOnAnimationStopListener
                if (lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) != true) {
                    return@setOnAnimationStopListener
                }

                // SDK 2.15.1은 자연 종료 시 등록 목록을 유지하므로 다시 추가하지 않습니다.
                finishedAnimator.start()
            }
            currentAnimator.addLabels(label)
            label.show()
            currentAnimator.start()
        } catch (_: Exception) {
            release()
        }
    }

    /** 대기 중인 이전 종료 콜백을 차단한 뒤 해당 Animator만 제거합니다. */
    private fun stop() {
        val currentAnimator = animator
        activation = null
        animator = null

        currentAnimator?.let {
            it.setOnAnimationStopListener(null)
            runCatching { it.stop() }
            runCatching { labelManager.removeAnimator(it.id) }
        }
        runCatching { label.hide() }
    }

    /** 지도 종료와 Composition 해제에서 중복 호출되어도 한 번만 정리합니다. */
    fun release() {
        if (isReleased) return

        isReleased = true
        lifecycle?.removeObserver(lifecycleObserver)
        lifecycle = null
        stop()

        // 지도 엔진이 먼저 종료된 경우에도 나머지 정리와 참조 해제를 계속합니다.
        runCatching { marker.removeSharePosition(label) }
        runCatching { label.remove() }
        runCatching { labelManager.remove(layer) }
    }
}

private const val LOCATION_ZOOM_LEVEL = 17
private const val LOCATION_PULSE_SIZE = 120
private const val LOCATION_PULSE_DURATION_MILLIS = 2_400
private const val LOCATION_PULSE_START_SCALE = 0.65f
private const val LOCATION_PULSE_END_SCALE = 1.3f
private const val LOCATION_PULSE_START_ALPHA = 0.9f
