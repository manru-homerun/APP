package com.manruhomerun.yadanbeopseok.record.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import com.kakao.vectormap.GestureType
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.manruhomerun.yadanbeopseok.designsystem.R
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTint
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTintStrong
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.Region
import kotlin.math.roundToInt

/**
 * D01 여행 기록 화면에서 도시별 완료 여행 횟수를 지도에 표시합니다.
 *
 * 여행 목록에서 계산한 지역별 방문 횟수를 전달받으며,
 * 각 지역의 대표 행정 중심 위치에 원형 마커를 표시합니다.
 */
@Composable
fun TravelRecordMap(
    regionVisitCounts: Map<Region, Int>,
    onError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mapPaddingPx = with(LocalDensity.current) {
        MAP_PADDING.roundToPx()
    }
    val latestOnError by rememberUpdatedState(onError)

    var map by remember { mutableStateOf<KakaoMap?>(null) }
    var regionLabels by remember { mutableStateOf<List<Label>>(emptyList()) }

    val markerTypeface = remember(context) {
        ResourcesCompat.getFont(
            context,
            R.font.wanted_sans_bold,
        ) ?: Typeface.DEFAULT_BOLD
    }

    KakaoMapContainer(
        initialPosition = INITIAL_MAP_POSITION,
        initialZoomLevel = INITIAL_ZOOM_LEVEL,
        onMapReady = { kakaoMap ->
            kakaoMap.setPoiVisible(false)
            kakaoMap.setPoiClickable(false)

            GestureType.values()
                .filterNot { gestureType ->
                    gestureType == GestureType.Unknown
                }
                .forEach { gestureType ->
                    kakaoMap.setGestureEnable(gestureType, false)
                }

            val supportedRegionPositions = Region.entries
                .map { region ->
                    region.mapPosition
                }
                .toTypedArray()

            kakaoMap.moveCamera(
                CameraUpdateFactory.fitMapPoints(
                    supportedRegionPositions,
                    mapPaddingPx,
                ),
            )

            map = kakaoMap
        },
        onError = {
            map = null
            regionLabels = emptyList()
            latestOnError()
        },
        onMapReleased = {
            map = null
            regionLabels = emptyList()
        },
        modifier = modifier.background(YadanPrimaryTint),
        previewContent = {
            TravelRecordMapPreviewContent(
                regionVisitCounts = regionVisitCounts,
                modifier = Modifier.fillMaxSize(),
            )
        },
    )

    LaunchedEffect(map, regionVisitCounts) {
        val currentMap = map ?: return@LaunchedEffect

        try {
            regionLabels.forEach { label ->
                label.remove()
            }
            regionLabels = emptyList()

            val visibleRegionCounts = regionVisitCounts
                .filterValues { visitCount ->
                    visitCount > 0
                }
                .toList()
                .sortedBy { (region, _) ->
                    region.ordinal
                }

            if (visibleRegionCounts.isEmpty()) {
                return@LaunchedEffect
            }

            val labelLayer = checkNotNull(
                currentMap.labelManager?.layer,
            )

            val labelOptions = visibleRegionCounts.map { (region, visitCount) ->
                val markerBitmap = createRegionVisitMarker(
                    context = context,
                    region = region,
                    visitCount = visitCount,
                    typeface = markerTypeface,
                )

                val markerStyle = LabelStyle
                    .from(markerBitmap)
                    .setApplyDpScale(false)
                    .setAnchorPoint(
                        0.5f,
                        markerCircleAnchorY(visitCount),
                    )

                LabelOptions
                    .from(region.mapPosition)
                    .setStyles(markerStyle)
            }

            val createdLabels = labelLayer.addLabels(labelOptions)
            regionLabels = createdLabels?.toList().orEmpty()

            check(regionLabels.size == labelOptions.size)
        } catch (_: Exception) {
            map = null
            regionLabels = emptyList()
            latestOnError()
        }
    }
}

@Composable
private fun TravelRecordMapPreviewContent(
    regionVisitCounts: Map<Region, Int>,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.background(YadanPrimaryTint),
    ) {
        regionVisitCounts
            .filterValues { visitCount ->
                visitCount > 0
            }
            .forEach { (region, visitCount) ->
                val circleSize = visitCircleDiameterDp(visitCount).dp
                val xFraction = region.previewXFraction()
                val yFraction = region.previewYFraction()

                Column(
                    modifier = Modifier
                        .width(MARKER_BITMAP_WIDTH_DP.dp)
                        .offset(
                            x = maxWidth * xFraction -
                                MARKER_BITMAP_WIDTH_DP.dp / 2,
                            y = maxHeight * yFraction -
                                circleSize / 2,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(circleSize)
                            .clip(CircleShape)
                            .background(YadanPrimaryTintStrong)
                            .border(
                                width = 2.dp,
                                color = YadanPrimary,
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = visitCount.toString(),
                            style = YadanTypography.labelMedium,
                            color = YadanPrimaryInk,
                            textAlign = TextAlign.Center,
                        )
                    }

                    Text(
                        text = region.mapLabel,
                        style = YadanTypography.labelSmall,
                        color = YadanTextPrimary,
                        maxLines = 1,
                    )
                }
            }
    }
}

@Preview(
    name = "D01 지역별 여행 기록 지도",
    showBackground = true,
    widthDp = 360,
)
@Composable
private fun TravelRecordMapPreview() {
    YadanbeopseokTheme {
        TravelRecordMap(
            regionVisitCounts = mapOf(
                Region.BUSAN to 4,
                Region.DAEGU to 3,
                Region.GWANGJU to 1,
            ),
            onError = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
        )
    }
}

/**
 * 방문 횟수와 지역명이 포함된 카카오맵 마커 이미지를 생성합니다.
 */
private fun createRegionVisitMarker(
    context: Context,
    region: Region,
    visitCount: Int,
    typeface: Typeface,
): Bitmap {
    val density = context.resources.displayMetrics.density
    val circleDiameterDp = visitCircleDiameterDp(visitCount)
    val bitmapHeightDp = markerBitmapHeightDp(visitCount)

    val bitmapWidthPx = dpToPx(
        dp = MARKER_BITMAP_WIDTH_DP,
        density = density,
    )
    val bitmapHeightPx = dpToPx(
        dp = bitmapHeightDp,
        density = density,
    )
    val circleDiameterPx = circleDiameterDp * density
    val circleRadiusPx = circleDiameterPx / 2f
    val circleCenterX = bitmapWidthPx / 2f
    val circleCenterY = MARKER_TOP_PADDING_DP * density + circleRadiusPx

    val bitmap = createBitmap(
        width = bitmapWidthPx,
        height = bitmapHeightPx,
    )
    bitmap.density = Bitmap.DENSITY_NONE

    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    paint.style = Paint.Style.FILL
    paint.color = YadanPrimaryTintStrong.toArgb()
    paint.setShadowLayer(
        MARKER_SHADOW_RADIUS_DP * density,
        0f,
        MARKER_SHADOW_OFFSET_Y_DP * density,
        YadanPrimary.copy(alpha = 0.35f).toArgb(),
    )

    canvas.drawCircle(
        circleCenterX,
        circleCenterY,
        circleRadiusPx,
        paint,
    )

    paint.clearShadowLayer()
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = MARKER_BORDER_WIDTH_DP * density
    paint.color = YadanPrimary.toArgb()

    canvas.drawCircle(
        circleCenterX,
        circleCenterY,
        circleRadiusPx - paint.strokeWidth / 2f,
        paint,
    )

    paint.style = Paint.Style.FILL
    paint.typeface = typeface
    paint.textAlign = Paint.Align.CENTER
    paint.textSize = visitCountTextSizeDp(visitCount) * density
    paint.color = YadanPrimaryInk.toArgb()

    val countBaseline = circleCenterY -
        (paint.fontMetrics.ascent + paint.fontMetrics.descent) / 2f

    canvas.drawText(
        visitCount.toString(),
        circleCenterX,
        countBaseline,
        paint,
    )

    paint.textSize = REGION_LABEL_TEXT_SIZE_DP * density
    paint.color = YadanTextPrimary.toArgb()
    paint.setShadowLayer(
        REGION_LABEL_SHADOW_RADIUS_DP * density,
        0f,
        REGION_LABEL_SHADOW_OFFSET_Y_DP * density,
        YadanSurface.copy(alpha = 0.9f).toArgb(),
    )

    val regionLabelTop = MARKER_TOP_PADDING_DP * density +
        circleDiameterPx +
        REGION_LABEL_GAP_DP * density
    val regionLabelBaseline = regionLabelTop - paint.fontMetrics.ascent

    canvas.drawText(
        region.mapLabel,
        circleCenterX,
        regionLabelBaseline,
        paint,
    )

    return bitmap
}

private fun visitCircleDiameterDp(visitCount: Int): Int =
    when {
        visitCount >= 4 -> 56
        visitCount >= 2 -> 44
        else -> 32
    }

private fun visitCountTextSizeDp(visitCount: Int): Float =
    when {
        visitCount >= 100 -> 14f
        visitCount >= 4 -> 17f
        visitCount >= 2 -> 13f
        else -> 11f
    }

private fun markerBitmapHeightDp(visitCount: Int): Float =
    MARKER_TOP_PADDING_DP +
        visitCircleDiameterDp(visitCount) +
        REGION_LABEL_GAP_DP +
        REGION_LABEL_HEIGHT_DP +
        MARKER_BOTTOM_PADDING_DP

private fun markerCircleAnchorY(visitCount: Int): Float {
    val circleRadiusDp = visitCircleDiameterDp(visitCount) / 2f
    val circleCenterY = MARKER_TOP_PADDING_DP + circleRadiusDp

    return circleCenterY / markerBitmapHeightDp(visitCount)
}

private fun dpToPx(dp: Float, density: Float): Int =
    (dp * density).roundToInt().coerceAtLeast(1)

/**
 * D01 지도에서 사용하는 도시별 대표 행정 중심 위치입니다.
 */
private val Region.mapPosition: LatLng
    get() = when (this) {
        Region.SEOUL -> LatLng.from(37.5665, 126.9780)
        Region.SUWON -> LatLng.from(37.2636, 127.0286)
        Region.INCHEON -> LatLng.from(37.4563, 126.7052)
        Region.DAEJEON -> LatLng.from(36.3504, 127.3845)
        Region.DAEGU -> LatLng.from(35.8714, 128.6014)
        Region.GWANGJU -> LatLng.from(35.1595, 126.8526)
        Region.BUSAN -> LatLng.from(35.1796, 129.0756)
        Region.CHANGWON -> LatLng.from(35.2279, 128.6811)
    }

private val Region.mapLabel: String
    get() = when (this) {
        Region.SEOUL -> "서울"
        Region.SUWON -> "수원"
        Region.INCHEON -> "인천"
        Region.DAEJEON -> "대전"
        Region.DAEGU -> "대구"
        Region.GWANGJU -> "광주"
        Region.BUSAN -> "부산"
        Region.CHANGWON -> "창원"
    }

private fun Region.previewXFraction(): Float {
    val longitude = mapPosition.longitude

    return (
        (longitude - MAP_MIN_LONGITUDE) /
            (MAP_MAX_LONGITUDE - MAP_MIN_LONGITUDE)
        )
        .toFloat()
        .coerceIn(0.1f, 0.9f)
}

private fun Region.previewYFraction(): Float {
    val latitude = mapPosition.latitude

    return (
        (MAP_MAX_LATITUDE - latitude) /
            (MAP_MAX_LATITUDE - MAP_MIN_LATITUDE)
        )
        .toFloat()
        .coerceIn(0.12f, 0.78f)
}

private val INITIAL_MAP_POSITION = LatLng.from(36.35, 127.75)

private val MAP_PADDING = 28.dp

private const val INITIAL_ZOOM_LEVEL = 7

private const val MARKER_BITMAP_WIDTH_DP = 72f
private const val MARKER_TOP_PADDING_DP = 4f
private const val MARKER_BOTTOM_PADDING_DP = 2f
private const val MARKER_BORDER_WIDTH_DP = 2f
private const val MARKER_SHADOW_RADIUS_DP = 6f
private const val MARKER_SHADOW_OFFSET_Y_DP = 2f

private const val REGION_LABEL_GAP_DP = 3f
private const val REGION_LABEL_HEIGHT_DP = 14f
private const val REGION_LABEL_TEXT_SIZE_DP = 10f
private const val REGION_LABEL_SHADOW_RADIUS_DP = 3f
private const val REGION_LABEL_SHADOW_OFFSET_Y_DP = 1f

private const val MAP_MIN_LONGITUDE = 126.55
private const val MAP_MAX_LONGITUDE = 129.25
private const val MAP_MIN_LATITUDE = 35.0
private const val MAP_MAX_LATITUDE = 37.75
