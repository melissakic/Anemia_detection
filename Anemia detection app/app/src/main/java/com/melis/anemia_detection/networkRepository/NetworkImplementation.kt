package com.melis.anemia_detection.networkRepository

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class NetworkImplementation : NetworkInterface {

    private val client = HttpClient(Android) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun sendPostRequest(json: String, endpoint: String, response: (String) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val httpResponse: HttpResponse = client.post(endpoint) {
                    contentType(ContentType.Application.Json)
                    body = Json.parseToJsonElement(json)
                }
                val responseBody = httpResponse.readText()
                response(responseBody)
            } catch (e: Exception) {
                e.message
            }
        }
    }
}