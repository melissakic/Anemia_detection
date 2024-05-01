package com.melis.anemia_detection.imageLoadingRepository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File
import java.io.InputStream

class ImageLoadingImplementation : ImageLoadingInterface {

    override fun getBitmapFromInputStream(context: Context, inputStream: InputStream): Bitmap? {
        return inputStream.use { BitmapFactory.decodeStream(it) }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun getExifFromImage(imageFile: File): ExifInterface {
        return ExifInterface(imageFile)
    }
}