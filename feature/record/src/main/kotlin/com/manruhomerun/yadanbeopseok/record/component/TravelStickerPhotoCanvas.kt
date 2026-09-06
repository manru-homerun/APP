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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.Sticker
import com.manruhomerun.yadanbeopseok.record.viewmodel.PlacedSticker
import com.manruhomerun.yadanbeopseok.ui.component.YadanAsyncImage
import kotlin.math.roundToInt

/**
 * D04에서 사용자가 선택한 사진과 배치된 스티커를 표시합니다.
 *
 * 편집 중에는 스티커 선택, 이동, 확대·축소, 회전과 삭제를 지원합니다.
 * 저장하거나 공유할 때 [isEditingEnabled]를 false로 전달하면
 * 선택 테두리와 삭제 버튼이 이미지에 포함되지 않습니다.
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

    Box(
        modifier = modifier
            .clip(YadanShapes.large)
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
            YadanAsyncImage(
                imageUrl = photoUri,
                contentDescription = "선택한 사진",
                modifier = Modifier.matchParentSize(),
                shape = RectangleShape,
                contentScale = ContentScale.Crop,
            )

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

            if (canvasSize != IntSize.Zero) {
                placedStickers.forEach { placedSticker ->
                    key(placedSticker.id) {
                        PlacedStickerItem(
                            placedSticker = placedSticker,
                            canvasSize = canvasSize,
                            selected = selectedStickerId == placedSticker.id,
                            isEditingEnabled = isEditingEnabled,
                            onSelect = {
                                onStickerSelect(placedSticker.id)
                            },
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
        modifier = modifier.clickable(
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
    canvasSize: IntSize,
    selected: Boolean,
    isEditingEnabled: Boolean,
    onSelect: () -> Unit,
    onTransform: (
        panXFraction: Float,
        panYFraction: Float,
        zoomChange: Float,
        rotationChange: Float,
    ) -> Unit,
) {
    val currentSelected by rememberUpdatedState(selected)
    val currentOnSelect by rememberUpdatedState(onSelect)
    val currentOnTransform by rememberUpdatedState(onTransform)

    val stickerSizePx = with(LocalDensity.current) {
        PlacedStickerSize.toPx()
    }

    val interactionModifier =
        if (
            isEditingEnabled &&
            canvasSize.width > 0 &&
            canvasSize.height > 0
        ) {
            Modifier
                .pointerInput(placedSticker.id, canvasSize) {
                    detectTransformGestures { _, pan, zoom, rotation ->
                        if (!currentSelected) {
                            currentOnSelect()
                        }

                        currentOnTransform(
                            pan.x / canvasSize.width.toFloat(),
                            pan.y / canvasSize.height.toFloat(),
                            zoom,
                            rotation,
                        )
                    }
                }
                .clickable(
                    role = Role.Button,
                    onClick = currentOnSelect,
                )
        } else {
            Modifier
        }

    val selectionModifier =
        if (isEditingEnabled && selected) {
            Modifier
                .border(
                    width = 2.dp,
                    color = YadanPrimary,
                    shape = YadanShapes.small,
                )
                .padding(3.dp)
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
            .size(PlacedStickerSize)
            .graphicsLayer {
                scaleX = placedSticker.scale
                scaleY = placedSticker.scale
                rotationZ = placedSticker.rotationDegrees
            }
            .zIndex(
                if (isEditingEnabled && selected) {
                    1f
                } else {
                    0f
                },
            )
            .then(interactionModifier)
            .then(selectionModifier),
    ) {
        YadanAsyncImage(
            imageUrl = placedSticker.sticker.imageUrl,
            contentDescription = "사진에 배치된 스티커",
            modifier = Modifier.fillMaxSize(),
            shape = RectangleShape,
            contentScale = ContentScale.Fit,
        )
    }
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
private val PhotoCanvasBackground = Color(0xFF15110F)
private val DarkPreviewBackground = Color(0xFF0F0C0B)
