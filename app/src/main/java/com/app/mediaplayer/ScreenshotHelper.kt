package com.app.mediaplayer

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.TextureView
import android.widget.Toast
import androidx.media3.ui.PlayerView
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ScreenshotHelper {

    fun captureFrame(context: Context, playerView: PlayerView) {
        try {
            val videoSurfaceView = playerView.videoSurfaceView
            if (videoSurfaceView is TextureView) {
                val bitmap = videoSurfaceView.bitmap
                if (bitmap != null) {
                    saveBitmapToGallery(context, bitmap)
                    return
                }
            }
            
            playerView.isDrawingCacheEnabled = true
            val bitmap = Bitmap.createBitmap(playerView.drawingCache)
            playerView.isDrawingCacheEnabled = false
            saveBitmapToGallery(context, bitmap)

        } catch (e: Exception) {
            Toast.makeText(context, "Screenshot Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
        val filename = "MAX_Player_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MAXPlayer")
            }
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (imageUri != null) {
                fos = resolver.openOutputStream(imageUri)
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/MAXPlayer"
            val file = File(imagesDir)
            if (!file.exists()) file.mkdirs()
            val image = File(file, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(context, "📸 Screenshot Saved in Gallery!", Toast.LENGTH_SHORT).show()
        }
    }
}
