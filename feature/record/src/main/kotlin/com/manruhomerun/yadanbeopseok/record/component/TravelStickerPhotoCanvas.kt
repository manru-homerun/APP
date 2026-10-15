package com.manruhomerun.yadanbeopseok.record.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.Sticker
import com.manruhomerun.yadanbeopseok.record.viewmodel.PlacedSticker
import com.manruhomerun.yadanbeopseok.record.viewmodel.StickerPhotoGeometry
import com.manruhomerun.yadanbeopseok.ui.component.YadanAsyncImage
import com.manruhomerun.yadanbeopseok.ui.component.YadanImageLoadState
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** 현재 이미지 요청의 준비 상태와 사진 기준 비율, 캔버스 측정 정보를 전달합니다. */
data class TravelStickerPhotoImageState(
    val photoRequestId: Long,
    val photoUri: String?,
    val stickerSources: Map<Long, String>,
    val loadState: YadanImageLoadState,
    val hasPhotoError: Boolean = false,
    val canvasSize: IntSize = IntSize.Zero,
    val photoGeometry: StickerPhotoGeometry? = null,
    val stickerSizePx: Float = 0f,
    val stickerSizes: Map<Long, IntSize> = emptyMap(),
    val backgroundColor: Color = PhotoCanvasBackground,
    val layoutDirection: LayoutDirection = LayoutDirection.Ltr,
) {
    val isReady: Boolean
        get() = loadState == YadanImageLoadState.SUCCESS &&
            canvasSize.width > 0 && canvasSize.height > 0 &&
            photoGeometry?.isValid == true && stickerSizePx.isFinite() && stickerSizePx > 0f &&
            stickerSources.keys.all { id ->
                stickerSizes[id] == IntSize(stickerSizePx.roundToInt(), stickerSizePx.roundToInt())
            }

    /** 이전 사진이나 변경 전 배치 목록의 완료 상태를 사용하지 않도록 검사합니다. */
    fun matchesContent(requestId: Long, uri: String?, stickers: List<PlacedSticker>): Boolean =
        photoRequestId == requestId && photoUri == uri &&
            stickerSources == stickers.associate { it.id to it.sticker.imageUrl }
}

/**
 * D04에서 사용자가 선택한 사진과 배치된 스티커를 표시합니다.
 *
 * 편집 중에는 스티커 선택, 이동, 확대·축소, 회전과 삭제를 지원합니다.
 * 접근성 작업 메뉴에서도 제스처 없이 이동, 확대·축소와 회전을 실행할 수 있습니다.
 * 저장하거나 공유할 때 [isEditingEnabled]를 false로 전달하면
 * 선택 테두리와 삭제 버튼이 이미지에 포함되지 않습니다.
 * 이때 스티커의 크기, 위치, 회전과 겹치는 순서는 유지합니다.
 * [onImageStateChange]는 이미지 준비 상태와 실제 측정 크기, 사진 기준 비율을 전달합니다.
 */
@Composable
fun TravelStickerPhotoCanvas(
    photoUri: String?,
    placedStickers: List<PlacedSticker>,
    selectedStickerId: Long?,
    onPhotoSelectClick: () -> Unit,
    onClearStickerSelection: () -> Unit,
    onStickerSelect: (Long) -> Unit,
    onStickerTransform: (
        stickerId: Long,
        panXFraction: Float,
        panYFraction: Float,
        zoomChange: Float,
        rotationChange: Float,
    ) -> Unit,
    onDeleteSelectedSticker: () -> Unit,
    modifier: Modifier = Modifier,
    isEditingEnabled: Boolean = true,
    photoRequestId: Long = 0L,
    photoGeometry: StickerPhotoGeometry? = null,
    onImageStateChange: (TravelStickerPhotoImageState) -> Unit = {},
) {
    var canvasSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    val currentOnClearStickerSelection by rememberUpdatedState(
        onClearStickerSelection,
    )

    val hasPhoto = !photoUri.isNullOrBlank()
    val hasSelectedSticker = placedStickers.any {
        it.id == selectedStickerId
    }

    val stickerSources = placedStickers.associate { it.id to it.sticker.imageUrl }
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val shortSide = minOf(canvasSize.width, canvasSize.height).toFloat()
    val geometry = photoGeometry ?: if (hasPhoto && shortSide > 0f) {
        val outline = YadanShapes.large.createOutline(
            size = Size(canvasSize.width.toFloat(), canvasSize.height.toFloat()),
            layoutDirection = layoutDirection,
            density = density,
        )
        (outline as? Outline.Rounded)?.roundRect?.let { roundedRect ->
            StickerPhotoGeometry(
                stickerSizeFraction = minOf(with(density) { PlacedStickerSize.toPx() }, shortSide) / shortSide,
                cornerRadiusFraction = (roundedRect.topLeftCornerRadius.x / shortSide).coerceIn(0f, 0.5f),
            )
        }
    } else {
        null
    }
    // 화면에서만 픽셀 단위로 반올림하며 저장에는 원래 비율을 그대로 사용합니다.
    val stickerSizePx = if (geometry != null && shortSide > 0f) {
        (shortSide * geometry.stickerSizeFraction).roundToInt().coerceAtLeast(1).toFloat()
    } else {
        0f
    }
    val photoShape = if (geometry != null) {
        RoundedCornerShape(with(density) { (shortSide * geometry.cornerRadiusFraction).toDp() })
    } else {
        YadanShapes.large
    }
    var photoLoadState by remember(photoRequestId, photoUri) {
        mutableStateOf(if (hasPhoto) YadanImageLoadState.LOADING else YadanImageLoadState.FALLBACK)
    }
    val stickerLoadStates = remember(photoRequestId, photoUri, stickerSizePx) {
        mutableStateMapOf<Pair<Long, String>, YadanImageLoadState>()
    }
    val stickerSizes = remember(photoRequestId, photoUri, stickerSizePx) { mutableStateMapOf<Long, IntSize>() }
    val currentPhotoRequestId by rememberUpdatedState(photoRequestId)
    val currentPhotoUri by rememberUpdatedState(photoUri)
    val currentStickerSources by rememberUpdatedState(stickerSources)
    val currentStickerSizePx by rememberUpdatedState(stickerSizePx)
    val currentOnImageStateChange by rememberUpdatedState(onImageStateChange)

    LaunchedEffect(photoRequestId, photoUri, stickerSources, stickerSizePx) {
        stickerLoadStates.keys.removeAll { (id, url) -> stickerSources[id] != url }
        stickerSizes.keys.removeAll { it !in stickerSources }
    }

    val hasPhotoError = hasPhoto &&
        (photoLoadState == YadanImageLoadState.ERROR || photoLoadState == YadanImageLoadState.FALLBACK)
    val hasStickerError = stickerSources.any { (id, url) ->
        url.isBlank() || stickerLoadStates[id to url] == YadanImageLoadState.ERROR ||
            stickerLoadStates[id to url] == YadanImageLoadState.FALLBACK
    }
    val loadState = when {
        !hasPhoto -> YadanImageLoadState.FALLBACK
        hasPhotoError || hasStickerError -> YadanImageLoadState.ERROR
        canvasSize.width <= 0 || canvasSize.height <= 0 ||
            photoLoadState != YadanImageLoadState.SUCCESS ||
            stickerSources.any { (id, url) -> stickerLoadStates[id to url] != YadanImageLoadState.SUCCESS } ->
            YadanImageLoadState.LOADING
        else -> YadanImageLoadState.SUCCESS
    }
    val imageState = TravelStickerPhotoImageState(
        photoRequestId = photoRequestId,
        photoUri = photoUri,
        stickerSources = stickerSources,
        loadState = loadState,
        hasPhotoError = hasPhotoError,
        canvasSize = canvasSize,
        photoGeometry = geometry,
        stickerSizePx = stickerSizePx,
        stickerSizes = stickerSources.keys.associateWith { stickerSizes[it] ?: IntSize.Zero },
        layoutDirection = layoutDirection,
    )

    LaunchedEffect(imageState) {
        currentOnImageStateChange(imageState)
    }

    Box(
        modifier = modifier
            .clip(photoShape)
            .background(PhotoCanvasBackground)
            .onSizeChanged {
                canvasSize = it
            },
    ) {
        if (!hasPhoto) {
            PhotoPlaceholder(
                onClick = onPhotoSelectClick,
                enabled = isEditingEnabled,
                modifier = Modifier.matchParentSize(),
            )
        } else {
            key(photoRequestId, photoUri) {
                YadanAsyncImage(
                    imageUrl = photoUri,
                    contentDescription = "선택한 사진",
                    modifier = Modifier.matchParentSize(),
                    shape = RectangleShape,
                    contentScale = ContentScale.Fit,
                    crossfade = false,
                    onLoadStateChange = { state ->
                        if (currentPhotoRequestId == photoRequestId && currentPhotoUri == photoUri) {
                            photoLoadState = state
                        }
                    },
                )
            }

            if (isEditingEnabled) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(isEditingEnabled) {
                            detectTapGestures {
                                currentOnClearStickerSelection()
                            }
                        },
                )
            }

            if (stickerSizePx > 0f) {
                placedStickers.forEachIndexed { index, placedSticker ->
                    key(photoRequestId, photoUri, placedSticker.id, stickerSizePx) {
                        PlacedStickerItem(
                            placedSticker = placedSticker,
                            stickerNumber = index + 1,
                            canvasSize = canvasSize,
                            layoutDirection = layoutDirection,
                            stickerSizePx = stickerSizePx,
                            isSelected = selectedStickerId == placedSticker.id,
                            isEditingEnabled = isEditingEnabled,
                            onSelect = {
                                onStickerSelect(placedSticker.id)
                            },
                            onClearSelection = onClearStickerSelection,
                            onTransform = {
                                    panX,
                                    panY,
                                    zoom,
                                    rotation,
                                ->
                                onStickerTransform(
                                    placedSticker.id,
                                    panX,
                                    panY,
                                    zoom,
                                    rotation,
                                )
                            },
                            onImageLoadStateChange = { state ->
                                val imageUrl = placedSticker.sticker.imageUrl
                                if (currentPhotoRequestId == photoRequestId && currentPhotoUri == photoUri &&
                                    currentStickerSources[placedSticker.id] == imageUrl &&
                                    currentStickerSizePx == stickerSizePx
                                ) {
                                    stickerLoadStates[placedSticker.id to imageUrl] = state
                                }
                            },
                            onSizeChange = { size ->
                                if (currentPhotoRequestId == photoRequestId && currentPhotoUri == photoUri &&
                                    placedSticker.id in currentStickerSources && currentStickerSizePx == stickerSizePx
                                ) {
                                    stickerSizes[placedSticker.id] = size
                                }
                            },
                        )
                    }
                }
            }

            if (isEditingEnabled && hasSelectedSticker) {
                DeleteStickerButton(
                    onClick = onDeleteSelectedSticker,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .zIndex(2f),
                )
            }
        }
    }
}

@Composable
private fun PhotoPlaceholder(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.AddPhotoAlternate,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = YadanOnPrimary.copy(alpha = 0.40f),
        )

        Text(
            text = "내 사진을 올려요",
            modifier = Modifier.padding(top = 8.dp),
            style = YadanTypography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = YadanOnPrimary.copy(alpha = 0.40f),
        )
    }
}

@Composable
private fun PlacedStickerItem(
    placedSticker: PlacedSticker,
    stickerNumber: Int,
    canvasSize: IntSize,
    layoutDirection: LayoutDirection,
    stickerSizePx: Float,
    isSelected: Boolean,
    isEditingEnabled: Boolean,
    onSelect: () -> Unit,
    onClearSelection: () -> Unit,
    onTransform: (
        panXFraction: Float,
        panYFraction: Float,
        zoomChange: Float,
        rotationChange: Float,
    ) -> Unit,
    onImageLoadStateChange: (YadanImageLoadState) -> Unit,
    onSizeChange: (IntSize) -> Unit,
) {
    val canInteract = isEditingEnabled && canvasSize.width > 0 && canvasSize.height > 0
    val stickerSize = with(LocalDensity.current) { stickerSizePx.toDp() }
    val currentCanInteract by rememberUpdatedState(canInteract)
    val currentPlacedSticker by rememberUpdatedState(placedSticker)
    val currentLayoutDirection by rememberUpdatedState(layoutDirection)
    val currentSelected by rememberUpdatedState(isSelected)
    val currentOnSelect by rememberUpdatedState(onSelect)
    val currentOnClearSelection by rememberUpdatedState(onClearSelection)
    val currentOnTransform by rememberUpdatedState(onTransform)

    /** 이전에 생성된 접근성 작업도 현재 편집 가능 상태를 검사한 뒤 기존 변환 콜백을 사용합니다. */
    fun transformSticker(
        panXFraction: Float = 0f,
        panYFraction: Float = 0f,
        zoomChange: Float = 1f,
        rotationChange: Float = 0f,
    ): Boolean {
        if (!currentCanInteract) return false

        currentOnTransform(panXFraction, panYFraction, zoomChange, rotationChange)
        return true
    }

    val accessibilityActions = if (canInteract) {
        buildList {
            add(CustomAccessibilityAction("왼쪽으로 이동") {
                transformSticker(panXFraction = -ACCESSIBILITY_MOVE_FRACTION)
            })
            add(CustomAccessibilityAction("오른쪽으로 이동") {
                transformSticker(panXFraction = ACCESSIBILITY_MOVE_FRACTION)
            })
            add(CustomAccessibilityAction("위로 이동") {
                transformSticker(panYFraction = -ACCESSIBILITY_MOVE_FRACTION)
            })
            add(CustomAccessibilityAction("아래로 이동") {
                transformSticker(panYFraction = ACCESSIBILITY_MOVE_FRACTION)
            })
            add(CustomAccessibilityAction("확대") {
                transformSticker(zoomChange = ACCESSIBILITY_ZOOM_FACTOR)
            })
            add(CustomAccessibilityAction("축소") {
                transformSticker(zoomChange = 1f / ACCESSIBILITY_ZOOM_FACTOR)
            })
            add(CustomAccessibilityAction("시계 방향으로 회전") {
                transformSticker(rotationChange = ACCESSIBILITY_ROTATION_DEGREES)
            })
            add(CustomAccessibilityAction("반시계 방향으로 회전") {
                transformSticker(rotationChange = -ACCESSIBILITY_ROTATION_DEGREES)
            })

            if (isSelected) {
                add(CustomAccessibilityAction("선택 해제") {
                    if (currentCanInteract && currentSelected) {
                        currentOnClearSelection()
                        true
                    } else {
                        false
                    }
                })
            }
        }
    } else {
        emptyList()
    }
    val transformDescription = "가로 위치 ${(placedSticker.centerXFraction * 100).roundToInt()}%, " +
        "세로 위치 ${(placedSticker.centerYFraction * 100).roundToInt()}%, " +
        "크기 ${(placedSticker.scale * 100).roundToInt()}%, " +
        "회전 ${placedSticker.rotationDegrees.roundToInt()}도"

    val interactionModifier =
        if (canInteract) {
            Modifier
                .pointerInput(placedSticker.id, canvasSize) {
                    detectTransformGestures { _, pan, zoom, rotation ->
                        if (!currentCanInteract) return@detectTransformGestures

                        val sticker = currentPlacedSticker
                        val canvasPan = pan.toCanvasPan(sticker.scale, sticker.rotationDegrees)
                        val horizontalPan = if (currentLayoutDirection == LayoutDirection.Ltr) {
                            canvasPan.x
                        } else {
                            -canvasPan.x
                        }

                        if (!currentSelected) {
                            currentOnSelect()
                        }

                        currentOnTransform(
                            horizontalPan / canvasSize.width.toFloat(),
                            canvasPan.y / canvasSize.height.toFloat(),
                            zoom,
                            rotation,
                        )
                    }
                }
                .clickable(
                    role = Role.Button,
                    onClickLabel = "스티커 선택",
                    onClick = currentOnSelect,
                )
        } else {
            Modifier
        }

    val stickerX = canvasSize.width * placedSticker.centerXFraction
    val stickerY = canvasSize.height * placedSticker.centerYFraction

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (stickerX - stickerSizePx / 2f).roundToInt(),
                    y = (stickerY - stickerSizePx / 2f).roundToInt(),
                )
            }
            .size(stickerSize)
            .onSizeChanged(onSizeChange)
            .graphicsLayer {
                scaleX = placedSticker.scale
                scaleY = placedSticker.scale
                rotationZ = placedSticker.rotationDegrees
            }
            .zIndex(if (isSelected) 1f else 0f)
            .then(interactionModifier)
            .semantics {
                contentDescription = "사진에 붙인 스티커 $stickerNumber"
                selected = isSelected
                stateDescription = transformDescription
                role = Role.Button
                customActions = accessibilityActions
                if (!canInteract) disabled()
            },
    ) {
        YadanAsyncImage(
            imageUrl = placedSticker.sticker.imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            shape = RectangleShape,
            contentScale = ContentScale.Fit,
            crossfade = false,
            onLoadStateChange = onImageLoadStateChange,
        )

        // 선택 표시가 이미지의 측정 크기를 바꾸지 않도록 별도로 겹쳐 그립니다.
        if (isEditingEnabled && isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(
                        width = 2.dp,
                        color = YadanPrimary,
                        shape = YadanShapes.small,
                    ),
            )
        }
    }
}

/** 로컬 터치 이동량에 현재 확대율과 회전각을 적용해 캔버스 픽셀 이동량으로 변환합니다. */
private fun Offset.toCanvasPan(scale: Float, rotationDegrees: Float): Offset {
    val radians = Math.toRadians(rotationDegrees.toDouble())
    val cosine = cos(radians).toFloat()
    val sine = sin(radians).toFloat()

    return Offset(
        x = scale * (x * cosine - y * sine),
        y = scale * (x * sine + y * cosine),
    )
}

@Composable
private fun DeleteStickerButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.64f)),
    ) {
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = "선택한 스티커 삭제",
            tint = YadanOnPrimary,
        )
    }
}

@Preview(
    name = "D04 사진 선택 전",
    showBackground = true,
    backgroundColor = 0xFF0F0C0B,
    widthDp = 360,
    heightDp = 700,
)
@Composable
private fun TravelStickerPhotoCanvasEmptyPreview() {
    YadanbeopseokTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkPreviewBackground)
                .padding(24.dp),
        ) {
            TravelStickerPhotoCanvas(
                photoUri = null,
                placedStickers = emptyList(),
                selectedStickerId = null,
                onPhotoSelectClick = {},
                onClearStickerSelection = {},
                onStickerSelect = {},
                onStickerTransform = { _, _, _, _, _ -> },
                onDeleteSelectedSticker = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp),
            )
        }
    }
}

@Preview(
    name = "D04 스티커 편집",
    showBackground = true,
    backgroundColor = 0xFF0F0C0B,
    widthDp = 360,
    heightDp = 700,
)
@Composable
private fun TravelStickerPhotoCanvasEditingPreview() {
    val sticker = Sticker(
        id = "sticker-sajik",
        stickerPackId = "pack-busan",
        imageUrl = "",
    )

    YadanbeopseokTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkPreviewBackground)
                .padding(24.dp),
        ) {
            TravelStickerPhotoCanvas(
                photoUri = "preview://selected-photo",
                placedStickers = listOf(
                    PlacedSticker(
                        id = 1L,
                        sticker = sticker,
                        centerXFraction = 0.72f,
                        centerYFraction = 0.24f,
                        rotationDegrees = 9f,
                    ),
                ),
                selectedStickerId = 1L,
                photoGeometry = StickerPhotoGeometry(stickerSizeFraction = 0.275f, cornerRadiusFraction = 0.05625f),
                onPhotoSelectClick = {},
                onClearStickerSelection = {},
                onStickerSelect = {},
                onStickerTransform = { _, _, _, _, _ -> },
                onDeleteSelectedSticker = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp),
            )
        }
    }
}

private val PlacedStickerSize = 88.dp
private const val ACCESSIBILITY_MOVE_FRACTION = 0.05f
private const val ACCESSIBILITY_ZOOM_FACTOR = 1.1f
private const val ACCESSIBILITY_ROTATION_DEGREES = 15f
private val PhotoCanvasBackground = Color(0xFF15110F)
private val DarkPreviewBackground = Color(0xFF0F0C0B)
