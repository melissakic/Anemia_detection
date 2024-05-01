package com.melis.anemia_detection.imageLoadingRepository

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.InputStream

interface ImageLoadingInterface {
    fun getBitmapFromInputStream(context: Context, inputStream: InputStream): Bitmap?

    fun getExifFromImage(imageFile: File): android.media.ExifInterface
}