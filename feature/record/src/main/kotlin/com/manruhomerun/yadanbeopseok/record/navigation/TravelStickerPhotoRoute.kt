package com.manruhomerun.yadanbeopseok.record.navigation

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.record.component.TravelStickerPhotoImageState
import com.manruhomerun.yadanbeopseok.record.screen.TravelStickerPhotoScreen
import com.manruhomerun.yadanbeopseok.record.viewmodel.TravelStickerPhotoViewModel
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * D04 스티커 사진 편집 화면을 ViewModel과 Android 시스템 기능에 연결합니다.
 *
 * 갤러리 사진 선택 실행, 스티커 편집 콜백 연결, 캔버스 캡처와
 * 갤러리 저장을 담당합니다.
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
    val graphicsLayer = rememberGraphicsLayer()
    var isGalleryLaunchPending by rememberSaveable(travelId) { mutableStateOf(false) }
    var pendingPhotoUri by rememberSaveable(travelId) { mutableStateOf<String?>(null) }
    var isLeavingRoute by remember(travelId) { mutableStateOf(false) }
    var photoRequestId by remember(travelId) { mutableLongStateOf(0L) }
    var canvasImageState by remember(travelId) { mutableStateOf<TravelStickerPhotoImageState?>(null) }
    val isReadingPhoto = pendingPhotoUri != null

    fun canEditPhoto(): Boolean = !viewModel.uiState.value.isExporting && !isGalleryLaunchPending &&
        pendingPhotoUri == null && !isLeavingRoute

    fun isCanvasReady(): Boolean {
        val state = viewModel.uiState.value
        val imageState = canvasImageState ?: return false
        return imageState.isReady && imageState.matchesContent(photoRequestId, state.photoUri, state.placedStickers)
    }

    /** 화면 재생성 전의 준비 상태를 재사용하지 않고 캡처 직전에도 현재 이미지를 검사합니다. */
    fun requireReadyCanvasForExport() {
        val state = viewModel.uiState.value
        if (!state.isExporting || !state.hasSelectedPhoto || state.isLoading ||
            pendingPhotoUri != null || isGalleryLaunchPending || isLeavingRoute || !isCanvasReady()
        ) {
            throw IOException("사진과 스티커가 저장할 준비를 마치지 못했습니다.")
        }
    }

    fun navigateBack() {
        if (viewModel.uiState.value.isExporting || isLeavingRoute) return

        isLeavingRoute = true
        pendingPhotoUri = null
        isGalleryLaunchPending = false
        navigator.navigateBack()
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

    /*
     * 저장을 시작하면 편집용 테두리와 삭제 버튼이 사라집니다.
     * 변경된 캔버스가 실제로 그려진 뒤 캡처하도록 두 프레임을 기다립니다.
     */
    LaunchedEffect(
        uiState.isExporting,
        graphicsLayer,
        context,
        viewModel,
    ) {
        if (!uiState.isExporting) {
            return@LaunchedEffect
        }

        try {
            withFrameNanos {
                // isExporting 상태 변경을 화면에 반영합니다.
            }

            withFrameNanos {
                // 편집용 UI가 제거된 캔버스의 그리기를 기다립니다.
            }

            requireReadyCanvasForExport()
            val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
            ensureActive()
            requireReadyCanvasForExport()

            withContext(Dispatchers.IO) {
                saveBitmapToGallery(
                    context = context,
                    bitmap = bitmap,
                )
            }

            viewModel.finishExport()

            Toast.makeText(context, "사진이 갤러리에 저장되었습니다.", Toast.LENGTH_SHORT).show()
        } catch (exception: CancellationException) {
            viewModel.finishExport()
            throw exception
        } catch (_: Exception) {
            val errorMessage = "사진을 저장하지 못했습니다. 다시 시도해주세요."

            viewModel.finishExport(errorMessage)

            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    BackHandler(enabled = uiState.isExporting || isGalleryLaunchPending || isReadingPhoto) {
        // 저장 중에는 이동하지 않고, 사진 조회 중에는 결과 반영을 취소한 뒤 이동합니다.
        navigateBack()
    }

    TravelStickerPhotoScreen(
        uiState = uiState,
        onBackClick = ::navigateBack,
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
        onSaveClick = {
            if (canEditPhoto() && viewModel.uiState.value.canExport && isCanvasReady()) {
                viewModel.startExport()
            }
        },
        modifier = modifier,
        isReadingPhoto = isReadingPhoto,
        photoRequestId = photoRequestId,
        imageState = canvasImageState,
        onCanvasImageStateChange = { imageState ->
            val state = viewModel.uiState.value
            if (!isLeavingRoute && pendingPhotoUri == null &&
                imageState.matchesContent(photoRequestId, state.photoUri, state.placedStickers)
            ) {
                canvasImageState = imageState
            }
        },
        canvasModifier = Modifier.drawWithContent {
            graphicsLayer.record {
                this@drawWithContent.drawContent()
            }

            drawLayer(graphicsLayer)
        },
    )
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
 * 캡처한 캔버스 이미지를 Pictures/Yadanbeopseok 폴더에 저장합니다.
 */
private fun saveBitmapToGallery(
    context: Context,
    bitmap: Bitmap,
) {
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

    try {
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

        val publishValues = ContentValues().apply {
            put(MediaStore.Images.Media.IS_PENDING, 0)
        }

        val updatedRows = contentResolver.update(
            imageUri,
            publishValues,
            null,
            null,
        )

        if (updatedRows == 0) {
            throw IOException("저장한 이미지를 갤러리에 게시하지 못했습니다.")
        }
    } catch (exception: Exception) {
        contentResolver.delete(
            imageUri,
            null,
            null,
        )

        throw exception
    }
}

private const val IMAGE_MIME_TYPE = "image/png"
private const val IMAGE_PICKER_MIME_TYPE = "image/*"
private const val GALLERY_DIRECTORY_NAME = "Yadanbeopseok"
private const val PNG_QUALITY = 100
