package com.kontenery.controller

import com.kontenery.config.ApiConfig.baseUrl
import com.kontenery.model.Reading
import com.kontenery.model.Submeter
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class ApiUtilities(
    private val httpClient: HttpClient
) {
    suspend fun fetchSubmetersForClient(clientId: Long): Result<List<Submeter>> = httpClient.get("$baseUrl/utilities/fetchSubmeters/$clientId").body()

    suspend fun addReadingToSubmeter(submeterId: Long, reading: Reading): Result<Submeter> = httpClient.post("$baseUrl/utilities/addReading/$submeterId") {
        setBody(reading)
    }.body()

}