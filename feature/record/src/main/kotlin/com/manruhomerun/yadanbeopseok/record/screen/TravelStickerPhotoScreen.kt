package com.manruhomerun.yadanbeopseok.record.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButtonStyle
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBarStyle
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.Sticker
import com.manruhomerun.yadanbeopseok.model.StickerPack
import com.manruhomerun.yadanbeopseok.record.component.TravelStickerPhotoCanvas
import com.manruhomerun.yadanbeopseok.record.viewmodel.PlacedSticker
import com.manruhomerun.yadanbeopseok.record.viewmodel.TravelStickerPhotoUiState
import com.manruhomerun.yadanbeopseok.ui.component.YadanStickerSize
import com.manruhomerun.yadanbeopseok.ui.component.YadanStickerView

/**
 * D04 스티커 사진 편집 전체 화면입니다.
 *
 * Photo Picker 실행과 완성된 이미지의 갤러리 저장은 Route에서 처리합니다.
 * 이 화면은 현재 상태를 표시하고 사용자의 동작을 콜백으로 전달합니다.
 *
 * [canvasModifier]는 사진과 스티커가 표시되는 캔버스를 캡처할 때 사용합니다.
 */
@Composable
fun TravelStickerPhotoScreen(
    uiState: TravelStickerPhotoUiState,
    onBackClick: () -> Unit,
    onPhotoSelectClick: () -> Unit,
    onPhotoResetClick: () -> Unit,
    onStickerClick: (Sticker) -> Unit,
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
    onRetryClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
    canvasModifier: Modifier = Modifier,
) {
    val hasPhoto = !uiState.photoUri.isNullOrBlank()
    val isEditingEnabled = !uiState.isExporting

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        YadanTopAppBar(
            title = "스티커 사진",
            onNavigationClick = {
                if (isEditingEnabled) {
                    onBackClick()
                }
            },
            style = YadanTopAppBarStyle.ON_DARK,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 20.dp,
                ),
        ) {
            TravelStickerPhotoCanvas(
                photoUri = uiState.photoUri,
                placedStickers = uiState.placedStickers,
                selectedStickerId = uiState.selectedStickerId,
                onPhotoSelectClick = onPhotoSelectClick,
                onClearStickerSelection = onClearStickerSelection,
                onStickerSelect = onStickerSelect,
                onStickerTransform = onStickerTransform,
                onDeleteSelectedSticker = onDeleteSelectedSticker,
                isEditingEnabled = isEditingEnabled,
                modifier = canvasModifier
                    .fillMaxWidth()
                    .weight(1f),
            )

            Spacer(modifier = Modifier.height(13.dp))

            StickerTray(
                stickers = uiState.availableStickers,
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                canAddStickers = hasPhoto && isEditingEnabled,
                onStickerClick = onStickerClick,
                onRetryClick = onRetryClick,
            )

            Spacer(modifier = Modifier.height(14.dp))

            PhotoActionButtons(
                canReselect = hasPhoto && isEditingEnabled,
                canSave = uiState.canExport,
                onReselectClick = onPhotoResetClick,
                onSaveClick = onSaveClick,
            )
        }
    }
}

/**
 * 획득한 스티커를 가로 목록으로 표시합니다.
 *
 * 사진을 선택하기 전에는 스티커를 표시하되 추가 동작은 비활성화합니다.
 */
@Composable
private fun StickerTray(
    stickers: List<Sticker>,
    isLoading: Boolean,
    errorMessage: String?,
    canAddStickers: Boolean,
    onStickerClick: (Sticker) -> Unit,
    onRetryClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "획득한 스티커 · 탭하면 사진에 추가돼요",
            style = YadanTypography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
            ),
            color = YadanOnPrimary.copy(alpha = 0.60f),
        )

        Spacer(modifier = Modifier.height(9.dp))

        when {
            isLoading -> {
                StickerTrayLoading()
            }

            stickers.isEmpty() && errorMessage != null -> {
                StickerTrayError(
                    message = errorMessage,
                    onRetryClick = onRetryClick,
                )
            }

            stickers.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = "획득한 스티커가 없습니다.",
                        style = YadanTypography.bodySmall,
                        color = YadanOnPrimary.copy(alpha = 0.50f),
                    )
                }
            }

            else -> {
                StickerTrayItems(
                    stickers = stickers,
                    enabled = canAddStickers,
                    onStickerClick = onStickerClick,
                )
            }
        }
    }
}

@Composable
private fun StickerTrayLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = YadanPrimary,
            strokeWidth = 2.dp,
        )
    }
}

@Composable
private fun StickerTrayError(
    message: String,
    onRetryClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            style = YadanTypography.labelSmall,
            color = YadanOnPrimary.copy(alpha = 0.65f),
        )

        TextButton(
            onClick = onRetryClick,
            colors = ButtonDefaults.textButtonColors(
                contentColor = YadanPrimary,
            ),
        ) {
            Text(
                text = "다시 시도",
                style = YadanTypography.labelMedium,
            )
        }
    }
}

@Composable
private fun StickerTrayItems(
    stickers: List<Sticker>,
    enabled: Boolean,
    onStickerClick: (Sticker) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(
            items = stickers,
            key = Sticker::id,
        ) { sticker ->
            YadanStickerView(
                sticker = sticker,
                contentDescription = "사진에 스티커 추가",
                size = YadanStickerSize.TRAY,
                enabled = enabled,
                onClick = {
                    onStickerClick(sticker)
                },
            )
        }

        item(key = "locked-sticker-slot") {
            YadanStickerView(
                sticker = null,
                contentDescription = "잠긴 스티커",
                size = YadanStickerSize.TRAY,
                locked = true,
                enabled = false,
            )
        }
    }
}

/**
 * 선택한 사진을 다시 고르거나 편집 결과를 갤러리에 저장합니다.
 */
@Composable
private fun PhotoActionButtons(
    canReselect: Boolean,
    canSave: Boolean,
    onReselectClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        YadanButton(
            text = "다시 선택",
            onClick = onReselectClick,
            modifier = Modifier.weight(1f),
            style = YadanButtonStyle.ON_DARK,
            enabled = canReselect,
        )

        YadanButton(
            text = "사진 저장",
            onClick = onSaveClick,
            modifier = Modifier.weight(1f),
            enabled = canSave,
        )
    }
}

@Preview(
    name = "D04 전체 화면 · 사진 선택 전",
    showBackground = true,
    backgroundColor = 0xFF0F0C0B,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun TravelStickerPhotoScreenEmptyPreview() {
    YadanbeopseokTheme {
        TravelStickerPhotoScreen(
            uiState = stickerPhotoPreviewState(photoSelected = false),
            onBackClick = {},
            onPhotoSelectClick = {},
            onStickerClick = {},
            onClearStickerSelection = {},
            onStickerSelect = {},
            onStickerTransform = { _, _, _, _, _ -> },
            onDeleteSelectedSticker = {},
            onRetryClick = {},
            onSaveClick = {},
            onPhotoResetClick = {}
        )
    }
}

@Preview(
    name = "D04 전체 화면 · 편집 중",
    showBackground = true,
    backgroundColor = 0xFF0F0C0B,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun TravelStickerPhotoScreenEditingPreview() {
    YadanbeopseokTheme {
        TravelStickerPhotoScreen(
            uiState = stickerPhotoPreviewState(photoSelected = true),
            onBackClick = {},
            onPhotoSelectClick = {},
            onStickerClick = {},
            onClearStickerSelection = {},
            onStickerSelect = {},
            onStickerTransform = { _, _, _, _, _ -> },
            onDeleteSelectedSticker = {},
            onRetryClick = {},
            onSaveClick = {},
            onPhotoResetClick = {}
        )
    }
}

private fun stickerPhotoPreviewState(photoSelected: Boolean): TravelStickerPhotoUiState {
    val stickerPack = StickerPack(
        id = "pack-busan",
        name = "사직 한정 스티커팩",
        stickers = listOf(
            Sticker(
                id = "sticker-sajik",
                stickerPackId = "pack-busan",
                imageUrl = "",
            ),
            Sticker(
                id = "sticker-gamcheon",
                stickerPackId = "pack-busan",
                imageUrl = "",
            ),
            Sticker(
                id = "sticker-gwangalli",
                stickerPackId = "pack-busan",
                imageUrl = "",
            ),
        ),
    )

    val placedStickers = if (photoSelected) {
        listOf(
            PlacedSticker(
                id = 1L,
                sticker = stickerPack.stickers.first(),
                centerXFraction = 0.72f,
                centerYFraction = 0.22f,
                rotationDegrees = 9f,
            ),
        )
    } else {
        emptyList()
    }

    return TravelStickerPhotoUiState(
        stickerPack = stickerPack,
        photoUri = if (photoSelected) {
            "preview://selected-photo"
        } else {
            null
        },
        placedStickers = placedStickers,
        selectedStickerId = placedStickers.firstOrNull()?.id,
        isLoading = false,
    )
}

private val ScreenBackground = Color(0xFF0F0C0B)
