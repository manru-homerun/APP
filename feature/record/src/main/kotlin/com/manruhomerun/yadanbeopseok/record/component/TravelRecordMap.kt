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
import androidx.compose.ui.unit.Dp
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
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * D01 여행 기록 화면에서 도시별 완료 여행 횟수를 지도에 표시합니다.
 *
 * 선택 시즌에 조회된 완료 여행을 지역별로 집계한 값을 전달받으며,
 * 가까워서 겹치는 수도권 마커는 지도 확대 수준에 따라 하나로 묶습니다.
 */
@Composable
fun TravelRecordMap(
    regionVisitCounts: Map<Region, Int>,
    onError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val mapPaddingPx = with(density) { MAP_PADDING.roundToPx() }
    val metropolitanFocusPaddingPx = with(density) { METROPOLITAN_FOCUS_PADDING.roundToPx() }
    val markerCollisionDistancePx = with(density) { MARKER_COLLISION_DISTANCE.roundToPx().toDouble() }
    val latestOnError by rememberUpdatedState(onError)

    val completedTravelCounts = remember(regionVisitCounts) {
        regionVisitCounts
            .filterValues { completedTravelCount -> completedTravelCount > 0 }
            .toList()
            .sortedBy { (region, _) -> region.ordinal }
    }
    val completedRegions = remember(completedTravelCounts) {
        completedTravelCounts.map { (region, _) -> region }
    }
    val latestCompletedRegions by rememberUpdatedState(completedRegions)

    var map by remember { mutableStateOf<KakaoMap?>(null) }
    var regionLabels by remember { mutableStateOf<List<Label>>(emptyList()) }
    var showMetropolitanCluster by remember { mutableStateOf(false) }

    val markerTypeface = remember(context) {
        ResourcesCompat.getFont(
            context,
            R.font.wanted_sans_bold,
        ) ?: Typeface.DEFAULT_BOLD
    }

    fun reportMapError() {
        map = null
        regionLabels = emptyList()
        showMetropolitanCluster = false
        latestOnError()
    }

    KakaoMapContainer(
        initialPosition = INITIAL_MAP_POSITION,
        initialZoomLevel = INITIAL_ZOOM_LEVEL,
        onMapReady = { kakaoMap ->
            kakaoMap.setPoiVisible(false)
            kakaoMap.setPoiClickable(false)
            kakaoMap.configureRecordMapGestures()

            kakaoMap.setOnCameraMoveEndListener { currentMap, _, _ ->
                try {
                    showMetropolitanCluster = shouldShowMetropolitanCluster(
                        map = currentMap,
                        completedRegions = latestCompletedRegions,
                        collisionDistancePx = markerCollisionDistancePx,
                    )
                } catch (_: Exception) {
                    reportMapError()
                }
            }
            kakaoMap.setOnLabelClickListener { currentMap, _, label ->
                val clusterTag = label.tag as? MetropolitanClusterTag

                if (clusterTag == null) {
                    false
                } else {
                    currentMap.moveCamera(
                        CameraUpdateFactory.fitMapPoints(
                            clusterTag.regions
                                .map { region -> region.mapPosition }
                                .toTypedArray(),
                            metropolitanFocusPaddingPx,
                            METROPOLITAN_FOCUS_MAX_ZOOM_LEVEL,
                        ),
                    )
                    true
                }
            }

            map = kakaoMap
        },
        onError = {
            reportMapError()
        },
        onMapReleased = {
            map?.setOnCameraMoveEndListener(null)
            map?.setOnLabelClickListener(null)
            map = null
            regionLabels = emptyList()
            showMetropolitanCluster = false
        },
        modifier = modifier.background(YadanPrimaryTint),
        previewContent = {
            TravelRecordMapPreviewContent(
                regionVisitCounts = regionVisitCounts,
                modifier = Modifier.fillMaxSize(),
            )
        },
    )

    LaunchedEffect(map, completedRegions) {
        val currentMap = map ?: return@LaunchedEffect

        try {
            when (completedRegions.size) {
                0 -> {
                    currentMap.moveCamera(
                        CameraUpdateFactory.newCenterPosition(
                            INITIAL_MAP_POSITION,
                            INITIAL_ZOOM_LEVEL,
                        ),
                    )
                }

                1 -> {
                    currentMap.moveCamera(
                        CameraUpdateFactory.newCenterPosition(
                            completedRegions.single().mapPosition,
                            SINGLE_REGION_ZOOM_LEVEL,
                        ),
                    )
                }

                else -> {
                    currentMap.moveCamera(
                        CameraUpdateFactory.fitMapPoints(
                            completedRegions
                                .map { region -> region.mapPosition }
                                .toTypedArray(),
                            mapPaddingPx,
                            COMPLETED_REGIONS_MAX_ZOOM_LEVEL,
                        ),
                    )
                }
            }
        } catch (_: Exception) {
            reportMapError()
        }
    }

    LaunchedEffect(map, completedTravelCounts, showMetropolitanCluster) {
        val currentMap = map ?: return@LaunchedEffect

        try {
            regionLabels.forEach { label -> label.remove() }
            regionLabels = emptyList()

            if (completedTravelCounts.isEmpty()) return@LaunchedEffect

            val metropolitanTravelCounts = completedTravelCounts.filter { (region, _) ->
                region in METROPOLITAN_REGIONS
            }
            val shouldUseCluster = showMetropolitanCluster && metropolitanTravelCounts.size >= 2
            val individualTravelCounts = if (shouldUseCluster) {
                completedTravelCounts.filterNot { (region, _) -> region in METROPOLITAN_REGIONS }
            } else {
                completedTravelCounts
            }
            val labelOptions = buildList {
                individualTravelCounts.forEach { (region, completedTravelCount) ->
                    add(
                        createCompletedTravelLabelOptions(
                            context = context,
                            region = region,
                            completedTravelCount = completedTravelCount,
                            typeface = markerTypeface,
                        ),
                    )
                }

                if (shouldUseCluster) {
                    val metropolitanRegions = metropolitanTravelCounts.map { (region, _) -> region }
                    add(
                        createMetropolitanClusterLabelOptions(
                            context = context,
                            regions = metropolitanRegions,
                            typeface = markerTypeface,
                        ),
                    )
                }
            }
            val labelLayer = checkNotNull(currentMap.labelManager?.layer)
            val createdLabels = labelLayer.addLabels(labelOptions)
            regionLabels = createdLabels?.toList().orEmpty()

            check(regionLabels.size == labelOptions.size)
        } catch (_: Exception) {
            reportMapError()
        }
    }
}

private fun KakaoMap.configureRecordMapGestures() {
    setGestureEnable(GestureType.Pan, true)
    setGestureEnable(GestureType.Zoom, true)
    setGestureEnable(GestureType.OneFingerDoubleTap, true)
    setGestureEnable(GestureType.TwoFingerSingleTap, true)
    setGestureEnable(GestureType.Rotate, false)
    setGestureEnable(GestureType.Tilt, false)
    setGestureEnable(GestureType.RotateZoom, false)
    setGestureEnable(GestureType.OneFingerZoom, false)
    setGestureEnable(GestureType.LongTapAndDrag, false)
}

private fun shouldShowMetropolitanCluster(
    map: KakaoMap,
    completedRegions: List<Region>,
    collisionDistancePx: Double,
): Boolean {
    val screenPoints = completedRegions
        .filter { region -> region in METROPOLITAN_REGIONS }
        .mapNotNull { region -> map.toScreenPoint(region.mapPosition) }

    if (screenPoints.size < 2) return false

    return screenPoints.indices.any { firstIndex ->
        ((firstIndex + 1) until screenPoints.size).any { secondIndex ->
            val firstPoint = screenPoints[firstIndex]
            val secondPoint = screenPoints[secondIndex]
            val distance = hypot(
                (firstPoint.x - secondPoint.x).toDouble(),
                (firstPoint.y - secondPoint.y).toDouble(),
            )

            distance < collisionDistancePx
        }
    }
}

private fun createCompletedTravelLabelOptions(
    context: Context,
    region: Region,
    completedTravelCount: Int,
    typeface: Typeface,
): LabelOptions {
    val circleDiameterDp = completedTravelCircleDiameterDp(completedTravelCount)
    val markerBitmap = createMapMarkerBitmap(
        context = context,
        bitmapWidthDp = MARKER_BITMAP_WIDTH_DP,
        circleDiameterDp = circleDiameterDp,
        circleText = completedTravelCount.toString(),
        circleTextSizeDp = completedTravelCountTextSizeDp(completedTravelCount),
        label = region.mapLabel,
        typeface = typeface,
    )
    val markerStyle = LabelStyle
        .from(markerBitmap)
        .setApplyDpScale(false)
        .setAnchorPoint(0.5f, markerCircleAnchorY(circleDiameterDp))

    return LabelOptions
        .from(region.mapPosition)
        .setStyles(markerStyle)
}

private fun createMetropolitanClusterLabelOptions(
    context: Context,
    regions: List<Region>,
    typeface: Typeface,
): LabelOptions {
    val markerBitmap = createMapMarkerBitmap(
        context = context,
        bitmapWidthDp = CLUSTER_MARKER_BITMAP_WIDTH_DP,
        circleDiameterDp = CLUSTER_CIRCLE_DIAMETER_DP,
        circleText = regions.size.toString(),
        circleTextSizeDp = CLUSTER_COUNT_TEXT_SIZE_DP,
        label = "수도권 ${regions.size}지역",
        typeface = typeface,
    )
    val markerStyle = LabelStyle
        .from(markerBitmap)
        .setApplyDpScale(false)
        .setAnchorPoint(0.5f, markerCircleAnchorY(CLUSTER_CIRCLE_DIAMETER_DP))

    return LabelOptions
        .from(regions.averageMapPosition())
        .setStyles(markerStyle)
        .setClickable(true)
        .setTag(MetropolitanClusterTag(regions = regions))
}

@Composable
private fun TravelRecordMapPreviewContent(
    regionVisitCounts: Map<Region, Int>,
    modifier: Modifier = Modifier,
    showMetropolitanCluster: Boolean = false,
) {
    val completedTravelCounts = regionVisitCounts
        .filterValues { completedTravelCount -> completedTravelCount > 0 }
        .toList()
        .sortedBy { (region, _) -> region.ordinal }
    val metropolitanTravelCounts = completedTravelCounts.filter { (region, _) ->
        region in METROPOLITAN_REGIONS
    }
    val shouldUseCluster = showMetropolitanCluster && metropolitanTravelCounts.size >= 2
    val individualTravelCounts = if (shouldUseCluster) {
        completedTravelCounts.filterNot { (region, _) -> region in METROPOLITAN_REGIONS }
    } else {
        completedTravelCounts
    }

    BoxWithConstraints(modifier = modifier.background(YadanPrimaryTint)) {
        individualTravelCounts.forEach { (region, completedTravelCount) ->
            TravelRecordMapPreviewMarker(
                circleText = completedTravelCount.toString(),
                label = region.mapLabel,
                circleDiameterDp = completedTravelCircleDiameterDp(completedTravelCount),
                bitmapWidthDp = MARKER_BITMAP_WIDTH_DP,
                position = region.mapPosition,
                maxWidth = maxWidth,
                maxHeight = maxHeight,
            )
        }

        if (shouldUseCluster) {
            val metropolitanRegions = metropolitanTravelCounts.map { (region, _) -> region }
            TravelRecordMapPreviewMarker(
                circleText = metropolitanRegions.size.toString(),
                label = "수도권 ${metropolitanRegions.size}지역",
                circleDiameterDp = CLUSTER_CIRCLE_DIAMETER_DP,
                bitmapWidthDp = CLUSTER_MARKER_BITMAP_WIDTH_DP,
                position = metropolitanRegions.averageMapPosition(),
                maxWidth = maxWidth,
                maxHeight = maxHeight,
            )
        }
    }
}

@Composable
private fun TravelRecordMapPreviewMarker(
    circleText: String,
    label: String,
    circleDiameterDp: Int,
    bitmapWidthDp: Float,
    position: LatLng,
    maxWidth: Dp,
    maxHeight: Dp,
) {
    val circleSize = circleDiameterDp.dp

    Column(
        modifier = Modifier
            .width(bitmapWidthDp.dp)
            .offset(
                x = maxWidth * position.previewXFraction() - bitmapWidthDp.dp / 2,
                y = maxHeight * position.previewYFraction() - circleSize / 2,
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
                text = circleText,
                style = YadanTypography.labelMedium,
                color = YadanPrimaryInk,
                textAlign = TextAlign.Center,
            )
        }

        Text(
            text = label,
            style = YadanTypography.labelSmall,
            color = YadanTextPrimary,
            maxLines = 1,
        )
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

@Preview(
    name = "D01 수도권 여행 기록 지도 - 축소",
    showBackground = true,
    widthDp = 360,
)
@Composable
private fun TravelRecordMapMetropolitanClusterPreview() {
    YadanbeopseokTheme {
        TravelRecordMapPreviewContent(
            regionVisitCounts = metropolitanPreviewTravelCounts,
            showMetropolitanCluster = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
        )
    }
}

@Preview(
    name = "D01 수도권 여행 기록 지도 - 확대",
    showBackground = true,
    widthDp = 360,
)
@Composable
private fun TravelRecordMapMetropolitanDetailPreview() {
    YadanbeopseokTheme {
        TravelRecordMapPreviewContent(
            regionVisitCounts = metropolitanPreviewTravelCounts,
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
        )
    }
}

/** 완료 여행 횟수와 지역명이 포함된 카카오맵 마커 이미지를 생성합니다. */
private fun createMapMarkerBitmap(
    context: Context,
    bitmapWidthDp: Float,
    circleDiameterDp: Int,
    circleText: String,
    circleTextSizeDp: Float,
    label: String,
    typeface: Typeface,
): Bitmap {
    val density = context.resources.displayMetrics.density
    val bitmapHeightDp = markerBitmapHeightDp(circleDiameterDp)
    val bitmapWidthPx = dpToPx(dp = bitmapWidthDp, density = density)
    val bitmapHeightPx = dpToPx(dp = bitmapHeightDp, density = density)
    val circleDiameterPx = circleDiameterDp * density
    val circleRadiusPx = circleDiameterPx / 2f
    val circleCenterX = bitmapWidthPx / 2f
    val circleCenterY = MARKER_TOP_PADDING_DP * density + circleRadiusPx
    val bitmap = createBitmap(width = bitmapWidthPx, height = bitmapHeightPx)
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
    canvas.drawCircle(circleCenterX, circleCenterY, circleRadiusPx, paint)

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
    paint.textSize = circleTextSizeDp * density
    paint.color = YadanPrimaryInk.toArgb()
    val countBaseline = circleCenterY -
        (paint.fontMetrics.ascent + paint.fontMetrics.descent) / 2f
    canvas.drawText(circleText, circleCenterX, countBaseline, paint)

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
    canvas.drawText(label, circleCenterX, regionLabelBaseline, paint)

    return bitmap
}

private fun completedTravelCircleDiameterDp(completedTravelCount: Int): Int =
    when {
        completedTravelCount >= 4 -> 56
        completedTravelCount >= 2 -> 44
        else -> 32
    }

private fun completedTravelCountTextSizeDp(completedTravelCount: Int): Float =
    when {
        completedTravelCount >= 100 -> 14f
        completedTravelCount >= 4 -> 17f
        completedTravelCount >= 2 -> 13f
        else -> 11f
    }

private fun markerBitmapHeightDp(circleDiameterDp: Int): Float =
    MARKER_TOP_PADDING_DP +
        circleDiameterDp +
        REGION_LABEL_GAP_DP +
        REGION_LABEL_HEIGHT_DP +
        MARKER_BOTTOM_PADDING_DP

private fun markerCircleAnchorY(circleDiameterDp: Int): Float {
    val circleCenterY = MARKER_TOP_PADDING_DP + circleDiameterDp / 2f
    return circleCenterY / markerBitmapHeightDp(circleDiameterDp)
}

private fun List<Region>.averageMapPosition(): LatLng =
    LatLng.from(
        map { region -> region.mapPosition.latitude }.average(),
        map { region -> region.mapPosition.longitude }.average(),
    )

private fun dpToPx(dp: Float, density: Float): Int =
    (dp * density).roundToInt().coerceAtLeast(1)

/** D01 지도에서 사용하는 도시별 대표 행정 중심 위치입니다. */
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

private fun LatLng.previewXFraction(): Float =
    ((longitude - MAP_MIN_LONGITUDE) / (MAP_MAX_LONGITUDE - MAP_MIN_LONGITUDE))
        .toFloat()
        .coerceIn(0.1f, 0.9f)

private fun LatLng.previewYFraction(): Float =
    ((MAP_MAX_LATITUDE - latitude) / (MAP_MAX_LATITUDE - MAP_MIN_LATITUDE))
        .toFloat()
        .coerceIn(0.12f, 0.78f)

private data class MetropolitanClusterTag(val regions: List<Region>)

private val metropolitanPreviewTravelCounts = mapOf(
    Region.SEOUL to 3,
    Region.SUWON to 2,
    Region.INCHEON to 1,
)

private val METROPOLITAN_REGIONS = setOf(
    Region.SEOUL,
    Region.SUWON,
    Region.INCHEON,
)

private val INITIAL_MAP_POSITION = LatLng.from(36.35, 127.75)

private val MAP_PADDING = 44.dp
private val METROPOLITAN_FOCUS_PADDING = 28.dp
private val MARKER_COLLISION_DISTANCE = 72.dp

private const val INITIAL_ZOOM_LEVEL = 7
private const val SINGLE_REGION_ZOOM_LEVEL = 11
private const val COMPLETED_REGIONS_MAX_ZOOM_LEVEL = 11
private const val METROPOLITAN_FOCUS_MAX_ZOOM_LEVEL = 12

private const val MARKER_BITMAP_WIDTH_DP = 72f
private const val CLUSTER_MARKER_BITMAP_WIDTH_DP = 96f
private const val CLUSTER_CIRCLE_DIAMETER_DP = 48
private const val CLUSTER_COUNT_TEXT_SIZE_DP = 15f
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
