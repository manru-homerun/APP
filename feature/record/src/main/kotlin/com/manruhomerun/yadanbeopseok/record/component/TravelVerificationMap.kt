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
        modifier = modifier.background(YadanPrimaryTint),
        previewContent = {
            Text(
                text = "현재 위치 지도",
                color = YadanPrimaryInk,
                modifier = Modifier.align(Alignment.Center),
            )
        },
    )

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
