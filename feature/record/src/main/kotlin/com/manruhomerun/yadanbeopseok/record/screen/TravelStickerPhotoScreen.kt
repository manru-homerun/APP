package com.manruhomerun.yadanbeopseok.record.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.manruhomerun.yadanbeopseok.record.component.TravelStickerPhotoImageState
import com.manruhomerun.yadanbeopseok.record.viewmodel.PlacedSticker
import com.manruhomerun.yadanbeopseok.record.viewmodel.TravelStickerPhotoUiState
import com.manruhomerun.yadanbeopseok.ui.component.YadanImageLoadState
import com.manruhomerun.yadanbeopseok.ui.component.YadanStickerSize
import com.manruhomerun.yadanbeopseok.ui.component.YadanStickerView

/**
 * D04 스티커 사진 편집 전체 화면입니다.
 *
 * 갤러리 실행과 완성된 이미지의 갤러리 저장은 Route에서 처리합니다.
 * 이 화면은 현재 상태를 표시하고 사용자의 동작을 콜백으로 전달합니다.
 *
 * [isReadingPhoto]가 true이면 사진 정보 조회 상태를 표시하고 편집을 제한합니다.
 * [imageState]가 현재 이미지와 실제 배치 측정을 마친 경우에만 저장 버튼을 활성화합니다.
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
    isReadingPhoto: Boolean = false,
    photoRequestId: Long = 0L,
    imageState: TravelStickerPhotoImageState? = null,
    onCanvasImageStateChange: (TravelStickerPhotoImageState) -> Unit = {},
) {
    val hasPhoto = uiState.hasSelectedPhoto
    val isEditingEnabled = !uiState.isExporting && !isReadingPhoto
    val currentImageState = imageState?.takeIf {
        it.matchesContent(photoRequestId, uiState.photoUri, uiState.placedStickers)
    }
    val canSave = uiState.canExport && !isReadingPhoto && currentImageState?.isReady == true
    val imageStatusMessage = when {
        isReadingPhoto || !hasPhoto -> ""
        uiState.isExporting -> "사진을 갤러리에 저장하고 있어요."
        currentImageState?.hasPhotoError == true -> "사진을 불러오지 못했어요.\n다시 선택해 주세요."
        currentImageState?.loadState == YadanImageLoadState.ERROR ->
            "스티커를 불러오지 못했어요.\n삭제 후 다시 추가해 주세요."
        currentImageState?.isReady != true -> "사진과 스티커를 불러오고 있어요."
        else -> ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        YadanTopAppBar(
            title = "스티커 사진",
            onNavigationClick = {
                if (!uiState.isExporting) {
                    onBackClick()
                }
            },
            style = YadanTopAppBarStyle.ON_DARK,
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 20.dp,
                ),
        ) {
            // 조작 영역이 길어져도 사진 편집에 남은 높이의 절반 이상을 확보합니다.
            val controlsMaxHeight = (maxHeight - 13.dp).coerceAtLeast(0.dp) / 2f

            Column(modifier = Modifier.fillMaxSize()) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    val photoAspectRatio = uiState.photoAspectRatio
                    val hasAvailableSpace = maxWidth > 0.dp && maxHeight > 0.dp
                    val photoCanvasModifier =
                        if (hasPhoto && photoAspectRatio != null && hasAvailableSpace) {
                            val availableAspectRatio = maxWidth.value / maxHeight.value

                            if (photoAspectRatio >= availableAspectRatio) {
                                Modifier.size(
                                    width = maxWidth,
                                    height = maxWidth / photoAspectRatio,
                                )
                            } else {
                                Modifier.size(
                                    width = maxHeight * photoAspectRatio,
                                    height = maxHeight,
                                )
                            }
                        } else {
                            Modifier.fillMaxSize()
                        }

                    if (isReadingPhoto) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "사진 정보를 확인하고 있어요",
                                style = YadanTypography.bodySmall,
                                color = YadanOnPrimary.copy(alpha = 0.65f),
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = YadanPrimary,
                                trackColor = YadanOnPrimary.copy(alpha = 0.14f),
                            )
                        }
                    } else {
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
                            photoRequestId = photoRequestId,
                            onImageStateChange = onCanvasImageStateChange,
                            modifier = photoCanvasModifier,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(13.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = controlsMaxHeight)
                        .verticalScroll(rememberScrollState()),
                ) {
                    StickerTray(
                        stickers = uiState.availableStickers,
                        isLoading = uiState.isLoading,
                        errorMessage = uiState.errorMessage,
                        canAddStickers = hasPhoto && isEditingEnabled,
                        isRetryEnabled = isEditingEnabled,
                        onStickerClick = onStickerClick,
                        onRetryClick = onRetryClick,
                    )

                    // 최소 두 줄은 유지하되 큰 글꼴의 안내 문구는 필요한 만큼 늘어납니다.
                    Text(
                        text = imageStatusMessage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        style = YadanTypography.labelSmall,
                        color = YadanOnPrimary.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center,
                        minLines = 2,
                    )

                    PhotoActionButtons(
                        canReselect = hasPhoto && isEditingEnabled,
                        canSave = canSave,
                        isSaving = uiState.isExporting,
                        onReselectClick = onPhotoResetClick,
                        onSaveClick = onSaveClick,
                    )
                }
            }
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
    isRetryEnabled: Boolean,
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
                    enabled = isRetryEnabled,
                )
            }

            stickers.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
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
    enabled: Boolean,
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
            enabled = enabled,
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
        itemsIndexed(
            items = stickers,
            key = { _, sticker -> sticker.id },
        ) { index, sticker ->
            YadanStickerView(
                sticker = sticker,
                contentDescription = "획득한 스티커 ${index + 1} 추가",
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
    isSaving: Boolean,
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
            text = if (isSaving) "저장 중" else "사진 저장",
            onClick = onSaveClick,
            modifier = Modifier.weight(1f),
            enabled = canSave || isSaving,
            isLoading = isSaving,
            reserveOppositeIconSpace = false,
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
@Preview(name = "사진 선택 전 · 작은 화면", widthDp = 320, heightDp = 480)
@Preview(name = "사진 선택 전 · 작은 화면 · 글꼴 1.5", widthDp = 320, heightDp = 480, fontScale = 1.5f)
@Preview(name = "사진 선택 전 · 작은 화면 · 글꼴 2", widthDp = 320, heightDp = 480, fontScale = 2f)
@Preview(name = "사진 선택 전 · 가로 화면 · 글꼴 2", widthDp = 640, heightDp = 360, fontScale = 2f)
@Composable
private fun TravelStickerPhotoScreenEmptyPreview() {
    TravelStickerPhotoScreenPreview(photoAspectRatio = null)
}

@Preview(
    name = "D04 전체 화면 · 사진 정보 조회 중",
    showBackground = true,
    backgroundColor = 0xFF0F0C0B,
    widthDp = 360,
    heightDp = 800,
)
@Preview(name = "사진 정보 조회 · 작은 화면 · 글꼴 2", widthDp = 320, heightDp = 480, fontScale = 2f)
@Preview(name = "사진 정보 조회 · 가로 화면 · 글꼴 2", widthDp = 640, heightDp = 360, fontScale = 2f)
@Composable
private fun TravelStickerPhotoScreenReadingPreview() {
    TravelStickerPhotoScreenPreview(photoAspectRatio = null, isReadingPhoto = true)
}

@Preview(
    name = "D04 전체 화면 · 가로 사진",
    showBackground = true,
    backgroundColor = 0xFF0F0C0B,
    widthDp = 360,
    heightDp = 800,
)
@Preview(name = "가로 사진 · 작은 화면 · 글꼴 1.5", widthDp = 320, heightDp = 480, fontScale = 1.5f)
@Preview(name = "가로 사진 · 작은 화면 · 글꼴 2", widthDp = 320, heightDp = 480, fontScale = 2f)
@Preview(name = "가로 사진 · 가로 화면", widthDp = 640, heightDp = 360)
@Preview(name = "가로 사진 · 가로 화면 · 글꼴 2", widthDp = 640, heightDp = 360, fontScale = 2f)
@Composable
private fun TravelStickerPhotoScreenLandscapePreview() {
    TravelStickerPhotoScreenPreview(photoAspectRatio = 16f / 9f)
}

@Preview(
    name = "D04 전체 화면 · 세로 사진",
    showBackground = true,
    backgroundColor = 0xFF0F0C0B,
    widthDp = 360,
    heightDp = 800,
)
@Preview(name = "세로 사진 · 작은 화면 · 글꼴 2", widthDp = 320, heightDp = 480, fontScale = 2f)
@Preview(name = "세로 사진 · 가로 화면 · 글꼴 2", widthDp = 640, heightDp = 360, fontScale = 2f)
@Composable
private fun TravelStickerPhotoScreenPortraitPreview() {
    TravelStickerPhotoScreenPreview(photoAspectRatio = 3f / 4f)
}

@Preview(
    name = "D04 전체 화면 · 정사각형 사진",
    showBackground = true,
    backgroundColor = 0xFF0F0C0B,
    widthDp = 360,
    heightDp = 800,
)
@Preview(name = "정사각형 사진 · 작은 화면 · 글꼴 2", widthDp = 320, heightDp = 480, fontScale = 2f)
@Preview(name = "정사각형 사진 · 가로 화면 · 글꼴 2", widthDp = 640, heightDp = 360, fontScale = 2f)
@Composable
private fun TravelStickerPhotoScreenSquarePreview() {
    TravelStickerPhotoScreenPreview(photoAspectRatio = 1f)
}

@Preview(name = "D04 전체 화면 · 이미지 로딩 중", widthDp = 360, heightDp = 800)
@Preview(name = "이미지 로딩 · 작은 화면 · 글꼴 2", widthDp = 320, heightDp = 480, fontScale = 2f)
@Composable
private fun TravelStickerPhotoScreenImageLoadingPreview() {
    TravelStickerPhotoScreenPreview(photoAspectRatio = 3f / 4f, imageLoadState = YadanImageLoadState.LOADING)
}

@Preview(name = "D04 전체 화면 · 사진 이미지 오류", widthDp = 360, heightDp = 800)
@Preview(name = "사진 이미지 오류 · 작은 화면 · 글꼴 2", widthDp = 320, heightDp = 480, fontScale = 2f)
@Preview(name = "사진 이미지 오류 · 가로 화면 · 글꼴 2", widthDp = 640, heightDp = 360, fontScale = 2f)
@Composable
private fun TravelStickerPhotoScreenPhotoErrorPreview() {
    TravelStickerPhotoScreenPreview(
        photoAspectRatio = 3f / 4f,
        imageLoadState = YadanImageLoadState.ERROR,
        hasPhotoError = true,
    )
}

@Preview(name = "D04 전체 화면 · 스티커 이미지 오류", widthDp = 360, heightDp = 800)
@Preview(name = "스티커 이미지 오류 · 작은 화면 · 글꼴 2", widthDp = 320, heightDp = 480, fontScale = 2f)
@Composable
private fun TravelStickerPhotoScreenStickerErrorPreview() {
    TravelStickerPhotoScreenPreview(photoAspectRatio = 3f / 4f, imageLoadState = YadanImageLoadState.ERROR)
}

@Preview(name = "D04 전체 화면 · 사진 저장 중", widthDp = 360, heightDp = 800)
@Preview(name = "사진 저장 중 · 작은 화면 · 글꼴 2", widthDp = 320, heightDp = 480, fontScale = 2f)
@Composable
private fun TravelStickerPhotoScreenSavingPreview() {
    TravelStickerPhotoScreenPreview(photoAspectRatio = 3f / 4f, isSaving = true)
}

@Preview(name = "D04 전체 화면 · 여러 스티커 중 두 번째 선택", widthDp = 360, heightDp = 800)
@Preview(name = "여러 스티커 편집 · 작은 화면 · 글꼴 2", widthDp = 320, heightDp = 480, fontScale = 2f)
@Preview(name = "여러 스티커 편집 · 가로 화면 · 글꼴 2", widthDp = 640, heightDp = 360, fontScale = 2f)
@Composable
private fun TravelStickerPhotoScreenMultipleStickersPreview() {
    TravelStickerPhotoScreenPreview(
        photoAspectRatio = 3f / 4f,
        hasMultipleStickers = true,
        selectedStickerId = 2L,
    )
}

@Preview(name = "D04 전체 화면 · 스티커 선택 해제", widthDp = 360, heightDp = 800)
@Composable
private fun TravelStickerPhotoScreenNoSelectionPreview() {
    TravelStickerPhotoScreenPreview(
        photoAspectRatio = 3f / 4f,
        hasMultipleStickers = true,
        selectedStickerId = null,
    )
}

@Preview(name = "D04 전체 화면 · 여러 스티커 저장 중", widthDp = 360, heightDp = 800)
@Composable
private fun TravelStickerPhotoScreenMultipleStickersSavingPreview() {
    TravelStickerPhotoScreenPreview(
        photoAspectRatio = 3f / 4f,
        isSaving = true,
        hasMultipleStickers = true,
        selectedStickerId = 2L,
    )
}

@Preview(name = "스티커 없음 · 작은 화면 · 글꼴 2", widthDp = 320, heightDp = 480, fontScale = 2f)
@Preview(name = "스티커 없음 · 가로 화면 · 글꼴 2", widthDp = 640, heightDp = 360, fontScale = 2f)
@Composable
private fun TravelStickerPhotoScreenNoStickersPreview() {
    TravelStickerPhotoScreenPreview(photoAspectRatio = null, hasStickerPack = false)
}

@Preview(name = "스티커 조회 중 · 작은 화면 · 글꼴 2", widthDp = 320, heightDp = 480, fontScale = 2f)
@Composable
private fun TravelStickerPhotoScreenStickersLoadingPreview() {
    TravelStickerPhotoScreenPreview(photoAspectRatio = null, isLoadingStickers = true)
}

@Preview(name = "스티커 조회 오류 · 작은 화면 · 글꼴 2", widthDp = 320, heightDp = 480, fontScale = 2f)
@Preview(name = "스티커 조회 오류 · 가로 화면 · 글꼴 2", widthDp = 640, heightDp = 360, fontScale = 2f)
@Composable
private fun TravelStickerPhotoScreenStickersErrorPreview() {
    TravelStickerPhotoScreenPreview(
        photoAspectRatio = null,
        hasStickerPack = false,
        stickerErrorMessage = "스티커를 불러오지 못했어요. 네트워크 연결을 확인한 뒤 다시 시도해 주세요.",
    )
}

@Composable
private fun TravelStickerPhotoScreenPreview(
    photoAspectRatio: Float?,
    isReadingPhoto: Boolean = false,
    imageLoadState: YadanImageLoadState = YadanImageLoadState.SUCCESS,
    hasPhotoError: Boolean = false,
    isSaving: Boolean = false,
    hasMultipleStickers: Boolean = false,
    selectedStickerId: Long? = 1L,
    hasStickerPack: Boolean = true,
    isLoadingStickers: Boolean = false,
    stickerErrorMessage: String? = null,
) {
    val previewState = stickerPhotoPreviewState(photoAspectRatio, hasMultipleStickers)
    val uiState = previewState.copy(
        isExporting = isSaving,
        selectedStickerId = selectedStickerId?.takeIf { id -> previewState.placedStickers.any { it.id == id } },
        stickerPack = previewState.stickerPack.takeIf { hasStickerPack },
        isLoading = isLoadingStickers,
        errorMessage = stickerErrorMessage,
    )
    var measuredImageState by remember(uiState.photoUri, uiState.placedStickers) {
        mutableStateOf<TravelStickerPhotoImageState?>(null)
    }
    val imageState = measuredImageState?.copy(
        loadState = imageLoadState,
        hasPhotoError = hasPhotoError,
    )
    YadanbeopseokTheme {
        TravelStickerPhotoScreen(
            uiState = uiState,
            onBackClick = {},
            onPhotoSelectClick = {},
            onPhotoResetClick = {},
            onStickerClick = {},
            onClearStickerSelection = {},
            onStickerSelect = {},
            onStickerTransform = { _, _, _, _, _ -> },
            onDeleteSelectedSticker = {},
            onRetryClick = {},
            onSaveClick = {},
            isReadingPhoto = isReadingPhoto,
            imageState = imageState,
            onCanvasImageStateChange = { measuredImageState = it },
        )
    }
}

private fun stickerPhotoPreviewState(photoAspectRatio: Float?, hasMultipleStickers: Boolean): TravelStickerPhotoUiState {
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

    val placedStickers = if (photoAspectRatio != null) {
        buildList {
            add(
                PlacedSticker(
                    id = 1L,
                    sticker = stickerPack.stickers.first(),
                    centerXFraction = 0.72f,
                    centerYFraction = 0.22f,
                    rotationDegrees = 9f,
                ),
            )

            if (hasMultipleStickers) {
                add(
                    PlacedSticker(
                        id = 2L,
                        sticker = stickerPack.stickers.first(),
                        centerXFraction = 0.30f,
                        centerYFraction = 0.52f,
                        scale = 0.75f,
                        rotationDegrees = 270f,
                    ),
                )
                add(
                    PlacedSticker(
                        id = 3L,
                        sticker = stickerPack.stickers[1],
                        centerXFraction = 0.62f,
                        centerYFraction = 0.76f,
                        scale = 1.25f,
                        rotationDegrees = 30f,
                    ),
                )
            }
        }
    } else {
        emptyList()
    }

    return TravelStickerPhotoUiState(
        stickerPack = stickerPack,
        photoUri = if (photoAspectRatio != null) {
            "preview://selected-photo"
        } else {
            null
        },
        photoAspectRatio = photoAspectRatio,
        placedStickers = placedStickers,
        selectedStickerId = placedStickers.firstOrNull()?.id,
        isLoading = false,
    )
}

private val ScreenBackground = Color(0xFF0F0C0B)
