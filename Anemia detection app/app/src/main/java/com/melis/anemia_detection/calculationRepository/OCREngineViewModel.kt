package com.melis.anemia_detection.calculationRepository

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.melis.anemia_detection.errorModels.ErrorTypes
import com.melis.anemia_detection.fileRepository.FileRepositoryImplementation
import com.melis.anemia_detection.fileRepository.FileRepositoryInterface
import com.melis.anemia_detection.imageLoadingRepository.ImageLoadingImplementation
import com.melis.anemia_detection.imageLoadingRepository.ImageLoadingInterface
import com.melis.anemia_detection.imageManipulation.ImageManipulationImplementation
import com.melis.anemia_detection.imageManipulation.ImageManipulationInterface

class OCREngineViewModel : ViewModel() {

    private val fileRepository: FileRepositoryInterface = FileRepositoryImplementation()
    private val imageManipulation: ImageManipulationInterface = ImageManipulationImplementation()
    private val imageLoader: ImageLoadingInterface = ImageLoadingImplementation()

    @RequiresApi(Build.VERSION_CODES.Q)
    fun parse(
        context: Context,
        uri: Uri,
        onSuccessAction: (FirebaseVisionText?) -> Unit,
        onFailureAction: (ErrorTypes) -> Unit
    ) {
        // handle errors
        val inputStream = fileRepository.getInputStreamFromUri(context, uri)
        val imageFile = fileRepository.getFileFromUri(context, uri)

        val bitmap = imageLoader.getBitmapFromInputStream(context, inputStream!!)
        val exif = imageLoader.getExifFromImage(imageFile!!)

        val rotated = imageManipulation.rotateBitmapToPortrait(bitmap!!, exif)
        val image = FirebaseVisionImage.fromBitmap(rotated)


        val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
        detector.processImage(image)
            .addOnSuccessListener { firebaseVisionText ->
                firebaseVisionText?.let { text ->
                    onSuccessAction(text)
                }
            }
            .addOnFailureListener {
                onFailureAction(ErrorTypes.OCR_ERROR)
            }
    }
}