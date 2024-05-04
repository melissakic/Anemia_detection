package com.melis.anemia_detection.networkRepository

interface NetworkInterface {
    fun sendPostRequest(json: String, endpoint: String, response: (String) -> Unit)
}