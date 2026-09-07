package com.manruhomerun.yadanbeopseok.record.navigation

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.withContext

/**
 * D04 스티커 사진 편집 화면을 ViewModel과 Android 시스템 기능에 연결합니다.
 *
 * Photo Picker 실행, 스티커 편집 콜백 연결, 캔버스 캡처와
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

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { photoUri ->
        if (photoUri != null) {
            viewModel.selectPhoto(photoUri.toString())
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
            photoPickerLauncher.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly,
                ),
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
private const val GALLERY_DIRECTORY_NAME = "Yadanbeopseok"
private const val PNG_QUALITY = 100
