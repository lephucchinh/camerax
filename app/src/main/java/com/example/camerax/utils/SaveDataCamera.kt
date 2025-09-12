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

class SaveDataCamera() {

    suspend fun savePhoto(context: Context, result: PictureResult): Boolean {
        val bitmap = withContext(Dispatchers.Main) {
            // toBitmap phải chạy trên UI thread
            suspendCancellableCoroutine<Bitmap?> { cont ->
                result.toBitmap { bmp ->
                    cont.resume(bmp, onCancellation = null)
                }
            }
        }

        if (bitmap == null) return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "photo_${System.currentTimeMillis()}.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/MyCameraApp")
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                withContext(Dispatchers.IO) {
                    resolver.openOutputStream(uri)?.use { outStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
                true
            } else {
                false
            }
        } else {
            val picturesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            )
            if (!picturesDir.exists()) picturesDir.mkdirs()

            val file = File(picturesDir, "photo_${System.currentTimeMillis()}.jpg")

            withContext(Dispatchers.IO) {
                suspendCancellableCoroutine<Boolean> { cont ->
                    result.toFile(file) { savedFile ->
                        if (savedFile != null) {
                            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                            intent.data = Uri.fromFile(savedFile)
                            context.sendBroadcast(intent)
                            cont.resume(true, onCancellation = null)
                        } else {
                            cont.resume(false, onCancellation = null)
                        }
                    }
                }
            }
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