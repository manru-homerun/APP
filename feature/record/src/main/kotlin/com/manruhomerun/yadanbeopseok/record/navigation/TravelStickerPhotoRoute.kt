package com.manruhomerun.yadanbeopseok.record.navigation

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorSpace
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.imageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Scale
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.record.component.TravelStickerPhotoImageState
import com.manruhomerun.yadanbeopseok.record.screen.TravelStickerPhotoScreen
import com.manruhomerun.yadanbeopseok.record.viewmodel.PlacedSticker
import com.manruhomerun.yadanbeopseok.record.viewmodel.StickerPhotoGeometry
import com.manruhomerun.yadanbeopseok.record.viewmodel.TravelStickerPhotoViewModel
import com.manruhomerun.yadanbeopseok.record.viewmodel.inRenderOrder
import java.io.IOException
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * D04 스티커 사진 편집 화면을 ViewModel과 Android 시스템 기능에 연결합니다.
 *
 * 갤러리 사진 선택, 스티커 편집 콜백 연결과 원본 기반 이미지 합성·저장을 담당합니다.
 */
@Composable
fun TravelStickerPhotoRoute(
    travelId: String,
    navigator: Navigator,
    modifier: Modifier = Modifier,
    viewModel: TravelStickerPhotoViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current.applicationContext
    val coroutineScope = rememberCoroutineScope()
    var isGalleryLaunchPending by rememberSaveable(travelId) { mutableStateOf(false) }
    var pendingPhotoUri by rememberSaveable(travelId) { mutableStateOf<String?>(null) }
    var isExitConfirmationVisible by rememberSaveable(travelId) { mutableStateOf(false) }
    var isLeavingRoute by remember(travelId) { mutableStateOf(false) }
    var photoRequestId by remember(travelId) { mutableLongStateOf(0L) }
    var canvasImageState by remember(travelId) { mutableStateOf<TravelStickerPhotoImageState?>(null) }
    val isReadingPhoto = pendingPhotoUri != null

    fun canEditPhoto(): Boolean = !viewModel.uiState.value.isExporting && !isGalleryLaunchPending &&
        pendingPhotoUri == null && !isLeavingRoute && !isExitConfirmationVisible

    fun isCanvasReady(): Boolean {
        val state = viewModel.uiState.value
        val imageState = canvasImageState ?: return false
        return imageState.isReady && imageState.photoGeometry == state.photoGeometry &&
            imageState.matchesContent(photoRequestId, state.photoUri, state.placedStickers)
    }

    /** 저장 시점의 사진 기준 비율과 배치를 고정하며 화면 측정값이나 UI Bitmap은 가져오지 않습니다. */
    fun createExportSnapshot(): StickerPhotoExportSnapshot {
        val state = viewModel.uiState.value
        val imageState = canvasImageState
        if (!canEditPhoto() || !state.canExport || imageState == null || !isCanvasReady()) {
            throw IOException("사진과 스티커가 저장할 준비를 마치지 못했습니다.")
        }

        return StickerPhotoExportSnapshot(
            photoUri = Uri.parse(checkNotNull(state.photoUri)),
            photoAspectRatio = checkNotNull(state.photoAspectRatio),
            geometry = checkNotNull(state.photoGeometry),
            placedStickers = state.placedStickers.toList(),
            selectedStickerId = state.selectedStickerId,
            backgroundColor = imageState.backgroundColor.toArgb(),
            layoutDirection = imageState.layoutDirection,
        )
    }

    fun navigateBack() {
        if (viewModel.uiState.value.isExporting || isLeavingRoute) return

        isLeavingRoute = true
        isExitConfirmationVisible = false
        pendingPhotoUri = null
        isGalleryLaunchPending = false
        navigator.navigateBack()
    }

    /** 미저장 내용을 확인한 뒤 상단·시스템 뒤로가기를 같은 방식으로 처리합니다. */
    fun requestNavigateBack() {
        val state = viewModel.uiState.value
        if (state.isExporting || isLeavingRoute || isExitConfirmationVisible) return

        if (state.hasUnsavedChanges) {
            isExitConfirmationVisible = true
        } else {
            navigateBack()
        }
    }

    fun showGalleryLaunchFailure() {
        isGalleryLaunchPending = false
        Toast.makeText(context, "갤러리를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        isGalleryLaunchPending = false

        if (canEditPhoto() && result.resultCode == Activity.RESULT_OK) {
            val photoUri = result.data?.data?.toString()?.takeIf { it.isNotBlank() }

            if (photoUri == null) {
                Toast.makeText(context, "선택한 사진을 확인할 수 없습니다.", Toast.LENGTH_SHORT).show()
            } else {
                canvasImageState = null
                pendingPhotoUri = photoUri
            }
        }
    }

    LaunchedEffect(travelId, viewModel) {
        viewModel.loadStickers(travelId)
    }

    LaunchedEffect(uiState.hasUnsavedChanges, uiState.isExporting) {
        if (!uiState.hasUnsavedChanges || uiState.isExporting) {
            isExitConfirmationVisible = false
        }
    }

    LaunchedEffect(travelId, pendingPhotoUri, viewModel) {
        val photoUri = pendingPhotoUri ?: return@LaunchedEffect

        try {
            val aspectRatio = withContext(Dispatchers.IO) {
                readPhotoAspectRatio(context, Uri.parse(photoUri))
            }

            ensureActive()
            if (pendingPhotoUri != photoUri || isLeavingRoute) return@LaunchedEffect

            photoRequestId++
            canvasImageState = null
            viewModel.selectPhoto(photoUri, aspectRatio)
            pendingPhotoUri = null
        } catch (exception: CancellationException) {
            // 화면 재생성 시 대기 URI는 유지하고, 이전 작업의 결과만 버립니다.
            throw exception
        } catch (_: Exception) {
            ensureActive()
            if (pendingPhotoUri != photoUri || isLeavingRoute) return@LaunchedEffect

            pendingPhotoUri = null
            Toast.makeText(
                context,
                "사진 정보를 확인할 수 없습니다. 다른 사진을 선택해주세요.",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    /** 화면이 재생성되면 작업을 취소하며, 저장 상태만으로 작업을 자동 재실행하지 않습니다. */
    fun savePhoto() {
        if (!canEditPhoto() || !viewModel.uiState.value.canExport || !isCanvasReady()) return

        coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            var hasStarted = false
            var errorMessage: String? = null

            try {
                ensureActive()
                val snapshot = createExportSnapshot()
                viewModel.startExport()
                hasStarted = viewModel.uiState.value.isExporting
                if (!hasStarted) return@launch

                withContext(Dispatchers.IO) {
                    exportStickerPhotoToGallery(context, context.imageLoader, snapshot) {
                        viewModel.markPhotoSaved(
                            photoUri = snapshot.photoUri.toString(),
                            aspectRatio = snapshot.photoAspectRatio,
                            placedStickers = snapshot.placedStickers,
                            selectedStickerId = snapshot.selectedStickerId,
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: OutOfMemoryError) {
                ensureActive()
                errorMessage = "사진을 저장할 메모리가 부족합니다. 다른 앱을 닫고 다시 시도해주세요."
            } catch (_: Exception) {
                ensureActive()
                errorMessage = "사진을 저장하지 못했습니다. 다시 시도해주세요."
            } finally {
                if (hasStarted) viewModel.finishExport(errorMessage)
            }

            ensureActive()
            Toast.makeText(
                context,
                errorMessage ?: "사진이 갤러리에 저장되었습니다.",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    BackHandler {
        if (isExitConfirmationVisible) {
            isExitConfirmationVisible = false
        } else {
            requestNavigateBack()
        }
    }

    TravelStickerPhotoScreen(
        uiState = uiState,
        onBackClick = ::requestNavigateBack,
        onPhotoSelectClick = {
            if (canEditPhoto() && !viewModel.uiState.value.hasSelectedPhoto) {
                isGalleryLaunchPending = true

                try {
                    galleryLauncher.launch(
                        Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        ).apply {
                            type = IMAGE_PICKER_MIME_TYPE
                        },
                    )
                } catch (_: ActivityNotFoundException) {
                    showGalleryLaunchFailure()
                } catch (_: SecurityException) {
                    showGalleryLaunchFailure()
                } catch (_: IllegalArgumentException) {
                    showGalleryLaunchFailure()
                }
            }
        },
        onPhotoResetClick = {
            if (canEditPhoto()) {
                photoRequestId++
                canvasImageState = null
                viewModel.clearPhoto()
            }
        },
        onStickerClick = {
            if (canEditPhoto()) viewModel.addSticker(it)
        },
        onClearStickerSelection = {
            if (canEditPhoto()) viewModel.selectSticker(null)
        },
        onStickerSelect = {
            if (canEditPhoto()) viewModel.selectSticker(it)
        },
        onStickerTransform = { stickerId, panX, panY, zoom, rotation ->
            if (canEditPhoto()) {
                viewModel.updateStickerTransform(stickerId, panX, panY, zoom, rotation)
            }
        },
        onDeleteSelectedSticker = {
            if (canEditPhoto()) viewModel.deleteSelectedSticker()
        },
        onRetryClick = {
            if (canEditPhoto()) viewModel.retry()
        },
        onSaveClick = ::savePhoto,
        modifier = modifier,
        isReadingPhoto = isReadingPhoto,
        photoRequestId = photoRequestId,
        imageState = canvasImageState,
        isExitConfirmationVisible = isExitConfirmationVisible,
        onExitConfirmationDismiss = { isExitConfirmationVisible = false },
        onExitConfirmationConfirm = ::navigateBack,
        onCanvasImageStateChange = { imageState ->
            val state = viewModel.uiState.value
            if (!isLeavingRoute && pendingPhotoUri == null &&
                imageState.matchesContent(photoRequestId, state.photoUri, state.placedStickers)
            ) {
                // 최초 측정은 이미지 로딩 성공이나 기준 등록 완료를 기다리지 않습니다.
                val geometry = imageState.photoGeometry
                val photoUri = state.photoUri
                if (photoUri != null && geometry != null && imageState.canvasSize.width > 0 && imageState.canvasSize.height > 0) {
                    viewModel.initializePhotoGeometry(photoUri, geometry)
                }
                canvasImageState = imageState
            }
        },
    )
}

/** 저장 작업에만 사용하는 불변 배치 정보이며 Bitmap이나 UI Painter는 보관하지 않습니다. */
private data class StickerPhotoExportSnapshot(
    val photoUri: Uri,
    val photoAspectRatio: Float,
    val geometry: StickerPhotoGeometry,
    val placedStickers: List<PlacedSticker>,
    val selectedStickerId: Long?,
    val backgroundColor: Int,
    val layoutDirection: LayoutDirection,
)

/** 할당부터 갤러리 저장까지 같은 작업이 Bitmap을 소유해 취소 시에도 반드시 해제합니다. */
private suspend fun exportStickerPhotoToGallery(
    context: Context,
    imageLoader: ImageLoader,
    snapshot: StickerPhotoExportSnapshot,
    onPublished: () -> Unit,
) {
    var photoBitmap: Bitmap? = null
    var exportBitmap: Bitmap? = null

    try {
        val photo = decodeExportPhoto(context, snapshot)
        photoBitmap = photo
        currentCoroutineContext().ensureActive()

        val output = Bitmap.createBitmap(photo.width, photo.height, Bitmap.Config.ARGB_8888)
        exportBitmap = output
        val canvas = Canvas(output)
        val width = output.width.toFloat()
        val height = output.height.toFloat()
        val cornerRadius = min(width, height) * snapshot.geometry.cornerRadiusFraction
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        withContext(Dispatchers.Default) {
            // 회전 후 화면 크기와 무관하게 출력 사진 좌표에서 바로 합성합니다.
            val clipPath = Path().apply {
                addRoundRect(
                    RectF(0f, 0f, width, height),
                    cornerRadius,
                    cornerRadius,
                    Path.Direction.CW,
                )
            }
            canvas.clipPath(clipPath)
            canvas.drawColor(snapshot.backgroundColor)
            val photoRect = fitImageRect(snapshot.photoAspectRatio, 1f, width, height)
            canvas.drawBitmap(photo, null, photoRect, paint)
        }
        photo.recycle()
        photoBitmap = null

        // Compose의 zIndex와 동일하게 선택된 스티커만 마지막에 그립니다.
        val orderedStickers = snapshot.placedStickers.inRenderOrder(snapshot.selectedStickerId)
        for (sticker in orderedStickers) {
            currentCoroutineContext().ensureActive()
            drawExportSticker(context, imageLoader, canvas, paint, sticker, snapshot.geometry, snapshot.layoutDirection)
        }

        currentCoroutineContext().ensureActive()
        saveBitmapToGallery(context, output, onPublished)
    } finally {
        photoBitmap?.recycle()
        exportBitmap?.recycle()
    }
}

/** 원본을 먼저 크게 펼치지 않고 방향이 반영된 크기에서 긴 변 최대 2,048px로 디코딩합니다. */
private fun decodeExportPhoto(context: Context, snapshot: StickerPhotoExportSnapshot): Bitmap {
    val source = ImageDecoder.createSource(context.contentResolver, snapshot.photoUri)
    val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        val width = info.size.width
        val height = info.size.height
        if (width <= 0 || height <= 0 || width.toFloat() / height != snapshot.photoAspectRatio) {
            throw IOException("선택한 사진의 크기가 변경되었습니다. 다시 선택해주세요.")
        }

        val scale = min(1.0, MAX_EXPORT_IMAGE_LENGTH.toDouble() / max(width, height))
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        decoder.setTargetColorSpace(ColorSpace.get(ColorSpace.Named.SRGB))
        decoder.setTargetSize(
            (width * scale).roundToInt().coerceAtLeast(1),
            (height * scale).roundToInt().coerceAtLeast(1),
        )
    }

    if (bitmap.width <= 0 || bitmap.height <= 0 || max(bitmap.width, bitmap.height) > MAX_EXPORT_IMAGE_LENGTH) {
        bitmap.recycle()
        throw IOException("저장할 사진의 크기가 유효하지 않습니다.")
    }
    return bitmap
}

/** 출력 사진에 대한 상대 크기로 스티커를 읽고 Fit·중심 회전·확대·이동으로 합성합니다. */
private suspend fun drawExportSticker(
    context: Context,
    imageLoader: ImageLoader,
    canvas: Canvas,
    paint: Paint,
    sticker: PlacedSticker,
    geometry: StickerPhotoGeometry,
    layoutDirection: LayoutDirection,
) {
    val size = min(canvas.width, canvas.height) * geometry.stickerSizeFraction
    val neededSize = size * sticker.scale
    // 화면 밖으로 크게 확대한 스티커도 이미지 요청 크기는 출력 상한에 맞춥니다.
    val requestSize = ceil(neededSize.toDouble()).toInt().coerceIn(1, MAX_EXPORT_IMAGE_LENGTH)
    val request = ImageRequest.Builder(context)
        .data(sticker.sticker.imageUrl)
        .size(requestSize, requestSize)
        .scale(Scale.FIT)
        .allowHardware(false)
        .bitmapConfig(Bitmap.Config.ARGB_8888)
        .build()
    val result = imageLoader.execute(request)
    val drawable = (result as? SuccessResult)?.drawable as? BitmapDrawable
        ?: throw IOException("저장할 스티커를 불러오지 못했습니다.", (result as? ErrorResult)?.throwable)
    val bitmap = drawable.bitmap
    if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) {
        throw IOException("저장할 스티커 이미지가 유효하지 않습니다.")
    }

    withContext(Dispatchers.Default) {
        val centerX = if (layoutDirection == LayoutDirection.Ltr) {
            canvas.width * sticker.centerXFraction
        } else {
            canvas.width * (1f - sticker.centerXFraction)
        }
        val centerY = canvas.height * sticker.centerYFraction
        val saveCount = canvas.save()
        try {
            canvas.translate(centerX, centerY)
            canvas.rotate(sticker.rotationDegrees)
            canvas.scale(sticker.scale, sticker.scale)
            canvas.translate(-size / 2f, -size / 2f)
            canvas.clipRect(0f, 0f, size, size)
            val imageRect = fitImageRect(bitmap.width.toFloat(), bitmap.height.toFloat(), size, size)
            canvas.drawBitmap(bitmap, null, imageRect, paint)
        } finally {
            canvas.restoreToCount(saveCount)
        }
    }
    // Coil 캐시 소유 Bitmap은 복사하거나 recycle하지 않습니다.
}

/** 사진과 스티커 모두 ContentScale.Fit / Alignment.Center와 같은 영역을 사용합니다. */
private fun fitImageRect(sourceWidth: Float, sourceHeight: Float, width: Float, height: Float): RectF {
    val scale = min(width / sourceWidth, height / sourceHeight)
    val imageWidth = sourceWidth * scale
    val imageHeight = sourceHeight * scale
    val left = (width - imageWidth) / 2f
    val top = (height - imageHeight) / 2f
    return RectF(left, top, left + imageWidth, top + imageHeight)
}

/** 이미지 방향이 반영된 원본 비율을 읽고, 정보 확인용 작은 Bitmap은 즉시 해제합니다. */
private fun readPhotoAspectRatio(context: Context, photoUri: Uri): Float {
    val source = ImageDecoder.createSource(context.contentResolver, photoUri)
    var aspectRatio: Float? = null

    val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        val width = info.size.width
        val height = info.size.height
        if (width <= 0 || height <= 0) throw IOException("사진 크기가 유효하지 않습니다.")

        aspectRatio = (width.toFloat() / height.toFloat()).takeIf {
            it.isFinite() && it > 0f
        }
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        decoder.setTargetSize(1, 1)
    }

    bitmap.recycle()
    return aspectRatio ?: throw IOException("사진 비율이 유효하지 않습니다.")
}

/**
 * 합성한 이미지를 저장하고, 게시 전 실패·취소된 파일만 제거합니다.
 */
private suspend fun saveBitmapToGallery(context: Context, bitmap: Bitmap, onPublished: () -> Unit) {
    currentCoroutineContext().ensureActive()
    val contentResolver = context.contentResolver
    val displayName = "yadanbeopseok_${System.currentTimeMillis()}.png"

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
        put(MediaStore.Images.Media.MIME_TYPE, IMAGE_MIME_TYPE)
        put(
            MediaStore.Images.Media.RELATIVE_PATH,
            "${Environment.DIRECTORY_PICTURES}/$GALLERY_DIRECTORY_NAME",
        )
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }

    val imageCollection = MediaStore.Images.Media.getContentUri(
        MediaStore.VOLUME_EXTERNAL_PRIMARY,
    )

    val imageUri = contentResolver.insert(
        imageCollection,
        contentValues,
    ) ?: throw IOException("이미지 저장 위치를 생성하지 못했습니다.")
    var isPublished = false

    try {
        currentCoroutineContext().ensureActive()
        contentResolver.openOutputStream(imageUri)?.use { outputStream ->
            val isSaved = bitmap.compress(
                Bitmap.CompressFormat.PNG,
                PNG_QUALITY,
                outputStream,
            )

            if (!isSaved) {
                throw IOException("이미지를 변환하지 못했습니다.")
            }
        } ?: throw IOException("이미지 출력 스트림을 열지 못했습니다.")

        currentCoroutineContext().ensureActive()
        val publishValues = ContentValues().apply {
            put(MediaStore.Images.Media.IS_PENDING, 0)
        }

        val updatedRows = contentResolver.update(
            imageUri,
            publishValues,
            null,
            null,
        )

        if (updatedRows <= 0) {
            throw IOException("저장한 이미지를 갤러리에 게시하지 못했습니다.")
        }
        isPublished = true
        // 게시가 끝난 내용의 기록만 취소로부터 보호하며, 이미지 생성·쓰기는 기존대로 취소합니다.
        withContext(NonCancellable + Dispatchers.Main.immediate) {
            onPublished()
        }
    } finally {
        if (!isPublished) {
            try {
                contentResolver.delete(imageUri, null, null)
            } catch (_: Exception) {
                // 미완성 파일 정리 실패가 원래 저장 오류나 취소를 덮어쓰지 않게 합니다.
            }
        }
    }
}

private const val IMAGE_MIME_TYPE = "image/png"
private const val IMAGE_PICKER_MIME_TYPE = "image/*"
private const val GALLERY_DIRECTORY_NAME = "Yadanbeopseok"
private const val PNG_QUALITY = 100
private const val MAX_EXPORT_IMAGE_LENGTH = 2_048
