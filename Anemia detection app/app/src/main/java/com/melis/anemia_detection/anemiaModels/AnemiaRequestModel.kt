package com.melis.anemia_detection.anemiaModels

import kotlinx.serialization.Serializable

@Serializable
data class AnemiaRequestModel(val hemoglobin: Double, val gender: String, val mcv: Double)
