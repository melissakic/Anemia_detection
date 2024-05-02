package com.melis.anemia_detection.parsingRepository

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.melis.anemia_detection.calculationRepository.OCREngineViewModel
import com.melis.anemia_detection.errorModels.ErrorTypes

class ParsingViewModel {
    private val ocrEngineViewModel: OCREngineViewModel = OCREngineViewModel()
    private val tolerance: Int = 5
    private val targetText: List<String> = listOf("MCV", "Hemoglobin", "HGB")

    @RequiresApi(Build.VERSION_CODES.Q)
    fun parseText(
        context: Context,
        uri: Uri,
        onSuccessAction: (String) -> Unit,
        onFailureAction: (ErrorTypes) -> Unit
    ) {
        ocrEngineViewModel.parse(context, uri, onSuccessAction = { analyzedText ->
            if (analyzedText != null) {
                onSuccessAction(
                    ParsingEngine.extractAnemiaRawData(
                        analyzedText,
                        targetText,
                        tolerance
                    ) ?: ""
                )
            } else onFailureAction(ErrorTypes.OCR_ERROR)
        }, onFailureAction)
    }

}