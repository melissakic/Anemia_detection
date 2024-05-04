package com.melis.anemia_detection.mainRepository

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.melis.anemia_detection.anemiaModels.GenderValues
import com.melis.anemia_detection.anemiaModels.RefinedValues
import com.melis.anemia_detection.calculationRepository.OCREngineViewModel
import com.melis.anemia_detection.errorModels.ErrorTypes
import com.melis.anemia_detection.networkRepository.NetworkViewModel
import com.melis.anemia_detection.parsingRepository.ParsingEngine

class MainViewModel {
    private val ocrEngineViewModel: OCREngineViewModel = OCREngineViewModel()
    private val networkViewModel: NetworkViewModel = NetworkViewModel()

    private val tolerance: Int = 10
    var sex = mutableStateOf(GenderValues.MALE)
    var age = mutableStateOf(0)
    var refinedValues = mutableStateListOf<RefinedValues>()
    var result = mutableStateOf("")

    @RequiresApi(Build.VERSION_CODES.Q)
    fun parseText(
        context: Context,
        uri: Uri,
        onFailureAction: (ErrorTypes) -> Unit
    ) {
        ocrEngineViewModel.parse(context, uri, onSuccessAction = { analyzedText ->
            if (analyzedText != null) {
                val extractedValues = ParsingEngine.extractAnemiaData(analyzedText, tolerance)
                refinedValues.clear()
                refinedValues.addAll(extractedValues)
            } else onFailureAction(ErrorTypes.OCR_ERROR)
        }, onFailureAction)
    }

    fun checkForAnemia() {
        networkViewModel.predictAnemia(sex.value, refinedValues,
            onSuccessAction = {
                result.value = it
            },
            onFailureAction = {})
    }

}