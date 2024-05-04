package com.melis.anemia_detection.networkRepository

import com.melis.anemia_detection.anemiaConstants.Constants
import com.melis.anemia_detection.anemiaModels.AnemiaRequestModel
import com.melis.anemia_detection.anemiaModels.GenderValues
import com.melis.anemia_detection.anemiaModels.ParameterValues
import com.melis.anemia_detection.anemiaModels.RefinedValues
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class NetworkViewModel {
    private val network: NetworkInterface = NetworkImplementation()

    fun predictAnemia(
        gender: GenderValues,
        values: List<RefinedValues>,
        onSuccessAction: (String) -> Unit,
        onFailureAction: (String) -> Unit
    ) {
        val mcvValue = values.find { it.name == ParameterValues.MCV.value }?.value
        val hemoglobinValue =
            values.find { it.name == ParameterValues.HEMOGLOBIN_VAR1.value || it.name == ParameterValues.HEMOGLOBIN_VAR2.value }?.value
        val anemiaModel = AnemiaRequestModel(hemoglobinValue ?: 0.0, gender.value, mcvValue ?: 0.0)

        val json = Json.encodeToString(anemiaModel)
        network.sendPostRequest(json, Constants.API_ADDRESS) {
            onSuccessAction(it)
        }
    }
}