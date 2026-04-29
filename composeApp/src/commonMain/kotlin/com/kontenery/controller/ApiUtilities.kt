package com.kontenery.controller

import com.kontenery.config.ApiConfig.baseUrl
import com.kontenery.model.Reading
import com.kontenery.model.Submeter
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class ApiUtilities(
    private val httpClient: HttpClient
) {
    suspend fun fetchSubmetersForClient(clientId: Long): Result<List<Submeter>> = httpClient.get("$baseUrl/utilities/fetchSubmeters/$clientId").body()

    suspend fun addReadingToSubmeter(submeterId: Long, reading: Reading): Result<Submeter> = httpClient.post("$baseUrl/utilities/addReading/$submeterId") {
        setBody(reading)
    }.body()
    suspend fun deleteReadingToSubmeter(submeterId: Long, readingId: Reading): Result<Submeter> = httpClient.delete("$baseUrl/utilities/addReading/$submeterId/$readingId").body()

    suspend fun getAllSubmeters(): Result<List<Submeter>> = httpClient.get("$baseUrl/utilities/submeter").body()
    suspend fun getSubmeter(submeterId: Long): Result<Submeter> = httpClient.get("$baseUrl/utilities/submeter/$submeterId").body()
    suspend fun putSubmeter(id: Long, submeter: Submeter): Result<List<Submeter>> = httpClient.put("$baseUrl/utilities/submeter/$id") {
        setBody(submeter)
    }.body()
    suspend fun postSubmeter(submeter: Submeter): Result<List<Submeter>> = httpClient.post("$baseUrl/utilities/submeter") {
        setBody(submeter)
    }.body()
    suspend fun deleteSubmeter(submeterId: Long): Result<List<Submeter>> = httpClient.delete("$baseUrl/utilities/submeter/$submeterId").body()

}