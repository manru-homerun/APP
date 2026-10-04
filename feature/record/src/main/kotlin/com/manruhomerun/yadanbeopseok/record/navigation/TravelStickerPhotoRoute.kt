package com.manruhomerun.yadanbeopseok.record.navigation

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.manruhomerun.yadanbeopseok.record.screen.TravelStickerPhotoScreen
import com.manruhomerun.yadanbeopseok.record.viewmodel.TravelStickerPhotoViewModel
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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
    val coroutineScope = rememberCoroutineScope()
    var photoMetadataJob by remember {
        mutableStateOf<Job?>(null)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { photoUri ->
                photoMetadataJob?.cancel()
                photoMetadataJob = coroutineScope.launch {
                    try {
                        val aspectRatio = withContext(Dispatchers.IO) {
                            readPhotoAspectRatio(context, photoUri)
                        }

                        if (aspectRatio == null) {
                            Toast.makeText(
                                context,
                                "사진 정보를 확인할 수 없습니다. 다른 사진을 선택해주세요.",
                                Toast.LENGTH_SHORT,
                            ).show()
                            return@launch
                        }

                        viewModel.selectPhoto(photoUri.toString(), aspectRatio)
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (_: Exception) {
                        Toast.makeText(
                            context,
                            "사진 정보를 확인할 수 없습니다. 다른 사진을 선택해주세요.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            }
        }
    }

    LaunchedEffect(travelId, viewModel) {
        viewModel.loadStickers(travelId)
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

            val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()

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

    BackHandler(enabled = uiState.isExporting) {
        // 캔버스 캡처와 갤러리 저장이 끝날 때까지 화면을 유지합니다.
    }

    TravelStickerPhotoScreen(
        uiState = uiState,
        onBackClick = navigator::navigateBack,
        onPhotoSelectClick = {
            galleryLauncher.launch(
                Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ).apply {
                    type = IMAGE_PICKER_MIME_TYPE
                },
            )
        },
        onPhotoResetClick = viewModel::clearPhoto,
        onStickerClick = viewModel::addSticker,
        onClearStickerSelection = {
            viewModel.selectSticker(null)
        },
        onStickerSelect = viewModel::selectSticker,
        onStickerTransform = viewModel::updateStickerTransform,
        onDeleteSelectedSticker = viewModel::deleteSelectedSticker,
        onRetryClick = viewModel::retry,
        onSaveClick = viewModel::startExport,
        modifier = modifier,
        canvasModifier = Modifier.drawWithContent {
            graphicsLayer.record {
                this@drawWithContent.drawContent()
            }

            drawLayer(graphicsLayer)
        },
    )
}

/** 선택한 사진의 회전 방향을 반영한 가로세로 비율을 반환합니다. */
private fun readPhotoAspectRatio(
    context: Context,
    photoUri: Uri,
): Float? {
    val metadataRetriever = MediaMetadataRetriever()

    return try {
        metadataRetriever.setDataSource(context, photoUri)

        val width = metadataRetriever
            .extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_WIDTH)
            ?.toIntOrNull()
            ?: return null
        val height = metadataRetriever
            .extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_HEIGHT)
            ?.toIntOrNull()
            ?: return null
        val rotation = metadataRetriever
            .extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_ROTATION)
            ?.toIntOrNull()
            ?: 0

        if (width <= 0 || height <= 0) return null

        val hasQuarterTurn = rotation == 90 || rotation == 270
        val displayWidth = if (hasQuarterTurn) height else width
        val displayHeight = if (hasQuarterTurn) width else height
        val aspectRatio = displayWidth.toFloat() / displayHeight.toFloat()

        aspectRatio.takeIf {
            it.isFinite() && it > 0f
        }
    } finally {
        metadataRetriever.release()
    }
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
