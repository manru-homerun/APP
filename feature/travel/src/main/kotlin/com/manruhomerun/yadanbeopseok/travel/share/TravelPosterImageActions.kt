package com.manruhomerun.yadanbeopseok.travel.share

import android.app.Activity
import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

private const val IMAGE_MIME_TYPE = "image/png"
private const val IMAGE_DIRECTORY = "Yadanbeopseok"
private const val PNG_QUALITY = 100

/**
 * 캡처한 여행 포스터를 갤러리에 PNG 이미지로 저장합니다.
 */
internal suspend fun saveTravelPosterImage(context: Context, imageBitmap: ImageBitmap): Uri = withContext(Dispatchers.IO) {
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
        val wasSaved = contentResolver
            .openOutputStream(imageUri, "w")
            ?.use { outputStream ->
                imageBitmap
                    .asAndroidBitmap()
                    .compress(
                        android.graphics.Bitmap.CompressFormat.PNG,
                        PNG_QUALITY,
                        outputStream,
                    )
            }
            ?: false

        if (!wasSaved) {
            throw IOException("이미지를 저장할 수 없습니다.")
        }

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
