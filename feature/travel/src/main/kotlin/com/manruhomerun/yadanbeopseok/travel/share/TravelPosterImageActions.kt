package com.manruhomerun.yadanbeopseok.travel.share

import android.app.Activity
import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.GraphicsContext
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import java.io.IOException
import kotlin.math.max
import kotlin.math.sqrt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

private const val IMAGE_MIME_TYPE = "image/png"
private const val IMAGE_DIRECTORY = "Yadanbeopseok"
private const val PNG_QUALITY = 100
private const val MAX_POSTER_IMAGE_LENGTH = 4_096
private const val MAX_POSTER_PIXEL_COUNT = 4_194_304L

/**
 * 메인 스레드에서 전체 포스터를 제한된 크기로 캡처하고 갤러리에 저장합니다.
 * 임시 레이어와 캡처 Bitmap은 이 함수가 소유하며 실패와 취소 시에도 해제합니다.
 */
internal suspend fun captureAndSaveTravelPosterImage(
    context: Context,
    graphicsContext: GraphicsContext,
    posterGraphicsLayer: GraphicsLayer,
    density: Density,
    layoutDirection: LayoutDirection,
): Uri {
    currentCoroutineContext().ensureActive()
    val sourceSize = posterGraphicsLayer.size
    val outputSize = sourceSize.toPosterExportSize()
    val exportScale = sourceSize.posterExportScale(outputSize)

    val exportLayer = graphicsContext.createGraphicsLayer()
    var capturedBitmap: Bitmap? = null

    try {
        try {
            exportLayer.record(density, layoutDirection, outputSize) {
                scale(exportScale, pivot = Offset.Zero) {
                    drawLayer(posterGraphicsLayer)
                }
            }
            capturedBitmap = exportLayer.toImageBitmap().asAndroidBitmap()
        } finally {
            graphicsContext.releaseGraphicsLayer(exportLayer)
        }

        currentCoroutineContext().ensureActive()
        return saveTravelPosterImage(context, checkNotNull(capturedBitmap))
    } finally {
        capturedBitmap?.recycle()
    }
}

/** 출력 이미지를 만들기 전에 긴 변과 전체 픽셀 수를 제한합니다. */
private fun IntSize.toPosterExportSize(): IntSize {
    require(width > 0 && height > 0) { "저장할 포스터가 준비되지 않았습니다." }

    val pixelCount = width.toLong() * height.toLong()
    val exportScale = minOf(
        1.0,
        MAX_POSTER_IMAGE_LENGTH.toDouble() / max(width, height),
        sqrt(MAX_POSTER_PIXEL_COUNT.toDouble() / pixelCount),
    )
    val outputWidth = (width * exportScale).toInt().coerceIn(1, MAX_POSTER_IMAGE_LENGTH)
    val outputHeight = (height * exportScale).toInt().coerceIn(1, MAX_POSTER_IMAGE_LENGTH)
    check(outputWidth.toLong() * outputHeight <= MAX_POSTER_PIXEL_COUNT)

    return IntSize(outputWidth, outputHeight)
}

/** 실제 그리기 크기인 Float를 기준으로 전체 포스터가 출력 영역에 들어갈 배율을 계산합니다. */
private fun IntSize.posterExportScale(outputSize: IntSize): Float {
    val scale = minOf(
        1.0,
        outputSize.width.toDouble() / width.toFloat().toDouble(),
        outputSize.height.toDouble() / height.toFloat().toDouble(),
    ).toFloat()
    // 배율 반올림으로 마지막 줄이 출력 경계를 넘지 않도록 축소 배율을 한 단계 낮춥니다.
    return if (scale < 1f) Math.nextDown(scale) else scale
}

/**
 * 캡처한 여행 포스터를 갤러리에 PNG 이미지로 저장합니다.
 */
private suspend fun saveTravelPosterImage(context: Context, bitmap: Bitmap): Uri = withContext(Dispatchers.IO) {
    currentCoroutineContext().ensureActive()
    val contentResolver = context.contentResolver
    val displayName = "yadanbeopseok_travel_${System.currentTimeMillis()}.png"

    val pendingValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        put(MediaStore.MediaColumns.MIME_TYPE, IMAGE_MIME_TYPE)
        put(
            MediaStore.MediaColumns.RELATIVE_PATH,
            "${Environment.DIRECTORY_PICTURES}/$IMAGE_DIRECTORY",
        )
        put(MediaStore.MediaColumns.IS_PENDING, 1)
    }

    val imageUri = contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        pendingValues,
    ) ?: throw IOException("이미지 저장 공간을 만들 수 없습니다.")

    try {
        currentCoroutineContext().ensureActive()
        val wasSaved = contentResolver
            .openOutputStream(imageUri, "w")
            ?.use { outputStream ->
                bitmap.compress(
                    Bitmap.CompressFormat.PNG,
                    PNG_QUALITY,
                    outputStream,
                )
            }
            ?: false

        if (!wasSaved) {
            throw IOException("이미지를 저장할 수 없습니다.")
        }

        currentCoroutineContext().ensureActive()

        val completedValues = ContentValues().apply {
            put(MediaStore.MediaColumns.IS_PENDING, 0)
        }

        val updatedCount = contentResolver.update(
            imageUri,
            completedValues,
            null,
            null,
        )

        if (updatedCount == 0) {
            throw IOException("저장한 이미지를 갤러리에 표시할 수 없습니다.")
        }

        imageUri
    } catch (throwable: Throwable) {
        runCatching {
            contentResolver.delete(imageUri, null, null)
        }
        throw throwable
    }
}

/**
 * 갤러리에 저장된 여행 포스터로 Android 공유 선택창을 엽니다.
 */
internal fun openTravelPosterShareSheet(context: Context, imageUri: Uri) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = IMAGE_MIME_TYPE
        putExtra(Intent.EXTRA_STREAM, imageUri)
        clipData = ClipData.newUri(
            context.contentResolver,
            "travel_poster",
            imageUri,
        )
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooserIntent = Intent.createChooser(
        shareIntent,
        "여행 일정 공유",
    ).apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        if (context !is Activity) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    context.startActivity(chooserIntent)
}
