package com.melis.anemia_detection.imageManipulation

import android.graphics.Bitmap
import android.media.ExifInterface

interface ImageManipulationInterface {

    fun rotateBitmapToPortrait(bitmap: Bitmap, meta: ExifInterface?): Bitmap

    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap
}