package com.melis.anemia_detection.imageParsing

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import java.io.File
import java.io.IOException
import java.io.InputStream


class ParsingViewModel: ViewModel() {


    @RequiresApi(Build.VERSION_CODES.Q)
    fun parse(context: Context, uri: Uri, onSuccessAction: (String) -> Unit) {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = inputStream?.use { BitmapFactory.decodeStream(it) }


        val imageFile = getFileFromUri(context, uri)
        val exif = imageFile?.let { ExifInterface(it) }
        val rotated = bitmap?.let { rotateBitmapToPortrait(it,exif) }

        rotated?.let {
            var fetchedData: FirebaseVisionText?

            val tolerance = 5 // Tolerance in pixels
            var targetText = "MCV"


            val image = FirebaseVisionImage.fromBitmap(it)
            val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
            detector.processImage(image)
                .addOnSuccessListener { firebaseVisionText ->
                    fetchedData = firebaseVisionText
                    fetchedData?.let { text ->
                        // Iterate through each text block
                        val textLines = mutableListOf<String>()
                        text.textBlocks.forEach { block ->
                            // Iterate through each line in the text block
                            block.lines.forEach { line ->
                                // Check if the line contains the target text
                                if (line.text.contains(targetText)) {
                                    // Store the top and bottom coordinates of the line containing the target text
                                    val targetTop = line.boundingBox?.top ?: 0
                                    val targetBottom = line.boundingBox?.bottom ?: 0

                                    // Iterate through all text lines again to find lines within tolerance
                                    text.textBlocks.forEach { innerBlock ->
                                        innerBlock.lines.forEach { innerLine ->
                                            // Check if the line's top or bottom coordinate is within the tolerance range of the target line
                                            val topWithinTolerance = innerLine.boundingBox?.top?.let {
                                                it in (targetTop - tolerance)..(targetTop + tolerance)
                                            } ?: false
                                            val bottomWithinTolerance = innerLine.boundingBox?.bottom?.let {
                                                it in (targetBottom - tolerance)..(targetBottom + tolerance)
                                            } ?: false

                                            // If the line's top or bottom coordinate is within the tolerance range, add it to the result
                                            if (topWithinTolerance || bottomWithinTolerance) {
                                                textLines.add(innerLine.text)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // Join the lines within tolerance
                        val extractedText = textLines.joinToString("\n")
                        onSuccessAction(extractedText)
                    }
                }
                .addOnFailureListener {
                    println("Error")
                }
        }
    }


    fun rotateBitmapToPortrait(bitmap: Bitmap,meta: ExifInterface?): Bitmap {
        val orientation = meta?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val rotationAngle = when (orientation) {
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

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        val filePath: String = getRealPathFromURI(context, uri) ?: return null
        return File(filePath)
    }

    private fun getRealPathFromURI(context: Context, uri: Uri): String? {
        var realPath: String? = null

        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
                    realPath = it.getString(columnIndex)
                }
            }
        } else if (uri.scheme == ContentResolver.SCHEME_FILE) {
            realPath = uri.path
        }

        return realPath
    }


    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun removeCellSpacing(bitmap: Bitmap): Bitmap {
        // Define the threshold for considering a pixel as part of the text
        val threshold = 128

        // Create a new bitmap with the same dimensions as the original bitmap
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)

        // Iterate through each pixel in the bitmap
        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                // Get the color of the current pixel
                val pixel = bitmap.getPixel(x, y)

                // Check if the pixel is part of the text (black) or spacing (white)
                if (Color.red(pixel) < threshold || Color.green(pixel) < threshold || Color.blue(pixel) < threshold) {
                    // Pixel is part of the text, set it as black in the new bitmap
                    newBitmap.setPixel(x, y, Color.BLACK)
                } else {
                    // Pixel is part of the spacing, set it as white in the new bitmap
                    newBitmap.setPixel(x, y, Color.WHITE)
                }
            }
        }

        return newBitmap
    }

    private fun removeWhitespace(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val newBitmap = Bitmap.createBitmap(width, height, bitmap.config)

        // Remove whitespace by finding non-white pixels
        for (x in 0 until width) {
            for (y in 0 until height) {
                if (bitmap.getPixel(x, y) != Color.WHITE) {
                    newBitmap.setPixel(x, y, bitmap.getPixel(x, y))
                }
            }
        }

        return newBitmap
    }
}