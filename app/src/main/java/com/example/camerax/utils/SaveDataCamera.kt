package com.example.camerax.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.VideoResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class SaveDataCamera() {

    suspend fun savePhoto(context: Context, bitmap: Bitmap): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ dùng MediaStore
                val contentValues = ContentValues().apply {
                    put(
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        "photo_${System.currentTimeMillis()}.jpg"
                    )
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/MyCameraApp")
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: return false

                withContext(Dispatchers.IO) {
                    resolver.openOutputStream(uri)?.use { outStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outStream)
                    }
                }
                true
            } else {
                // Android 9 trở xuống: lưu trực tiếp vào Pictures
                val picturesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                )
                if (!picturesDir.exists()) picturesDir.mkdirs()

                val file = File(picturesDir, "photo_${System.currentTimeMillis()}.jpg")
                withContext(Dispatchers.IO) {
                    FileOutputStream(file).use { outStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outStream)
                    }
                }

                // Quét file để hiện trong Gallery
                val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                intent.data = Uri.fromFile(file)
                context.sendBroadcast(intent)

                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }




    suspend fun saveVideo(context: Context, result: VideoResult): Boolean {
        val videoFile = result.file

        return try {
            withContext(Dispatchers.IO) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, "video_${System.currentTimeMillis()}.mp4")
                        put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "Movies/MyCameraApp")
                        put(MediaStore.MediaColumns.IS_PENDING, 1) // đánh dấu đang ghi
                    }

                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)

                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { out ->
                            videoFile.inputStream().use { input -> input.copyTo(out) }
                        }
                        val pendingValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.IS_PENDING, 0)
                        }
                        resolver.update(uri, pendingValues, null, null)
                        true
                    } else {
                        false
                    }
                } else {
                    val moviesDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                    if (!moviesDir.exists()) moviesDir.mkdirs()

                    val dest = File(moviesDir, "video_${System.currentTimeMillis()}.mp4")
                    videoFile.copyTo(dest, overwrite = true)

                    // báo cho MediaScanner biết có file mới
                    val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    intent.data = Uri.fromFile(dest)
                    context.sendBroadcast(intent)

                    true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}