package com.manruhomerun.yadanbeopseok.record.component

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTint
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * D02에서 조회한 현재 위치를 지도와 마커로 표시합니다.
 *
 * 좌표가 변경되면 마커와 지도 중심을 갱신합니다.
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

    var map by remember {
        mutableStateOf<KakaoMap?>(null)
    }

    var marker by remember {
        mutableStateOf<Label?>(null)
    }

    val markerBitmap = remember {
        createLocationMarker()
    }

    Box(modifier = modifier.background(YadanPrimaryTint)) {
        KakaoMapContainer(
            initialPosition = position,
            initialZoomLevel = LOCATION_ZOOM_LEVEL,
            onMapReady = { kakaoMap ->
                val labelLayer = checkNotNull(kakaoMap.labelManager?.layer)

                val markerStyle = LabelStyle
                    .from(markerBitmap)
                    .setAnchorPoint(0.5f, 0.5f)

                val markerOptions = LabelOptions
                    .from(position)
                    .setStyles(markerStyle)

                marker = checkNotNull(
                    labelLayer.addLabel(markerOptions),
                )
                map = kakaoMap

                latestOnReady()
            },
            onError = {
                map = null
                marker = null
                latestOnError()
            },
            onMapReleased = {
                map = null
                marker = null
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

        if (map != null) {
            CurrentLocationPulse(
                modifier = Modifier.align(Alignment.Center),
            )
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
            map = null
            marker = null
            latestOnError()
        }
    }
}

/** 현재 위치 마커 주변에 반복되는 정확도 펄스를 표시합니다. */
@Composable
private fun CurrentLocationPulse(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(
        label = "current_location_pulse",
    )
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = LOCATION_PULSE_DURATION_MILLIS,
                easing = LinearOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "current_location_pulse_progress",
    )
    val scale = LOCATION_PULSE_START_SCALE +
        (LOCATION_PULSE_END_SCALE - LOCATION_PULSE_START_SCALE) * progress
    val alpha = LOCATION_PULSE_START_ALPHA * (1f - progress)

    Box(
        modifier = modifier
            .size(120.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .background(
                color = YadanPrimary.copy(alpha = 0.15f),
                shape = CircleShape,
            )
            .border(
                width = 2.dp,
                color = YadanPrimary.copy(alpha = 0.45f),
                shape = CircleShape,
            ),
    )
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

private const val LOCATION_ZOOM_LEVEL = 17
private const val LOCATION_PULSE_DURATION_MILLIS = 2_400
private const val LOCATION_PULSE_START_SCALE = 0.65f
private const val LOCATION_PULSE_END_SCALE = 1.3f
private const val LOCATION_PULSE_START_ALPHA = 0.9f
