package com.melis.anemia_detection.imageManipulation

import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface

class ImageManipulationImplementation : ImageManipulationInterface {

    override fun rotateBitmapToPortrait(bitmap: Bitmap, meta: ExifInterface?): Bitmap {
        val rotationAngle = when (meta?.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                90f
            }

            ExifInterface.ORIENTATION_ROTATE_180 -> {
                180f
            }

            ExifInterface.ORIENTATION_ROTATE_270 -> {
                270f
            }

            else -> {
                0f
            }
        }
        return if (rotationAngle != 0f) {
            rotateBitmap(bitmap, rotationAngle)
        } else {
            bitmap
        }
    }

    override fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}