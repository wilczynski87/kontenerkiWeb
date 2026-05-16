package com.kontenery.controller

import com.kontenery.config.ApiConfig.baseUrl
import com.kontenery.model.Product
import com.kontenery.model.Reading
import com.kontenery.model.Submeter
import com.kontenery.model.invoice.Position
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
    suspend fun fetchSubmetersForClient(clientId: Long): Result<List<Submeter>> {
        return try {
            val result: List<Submeter> = httpClient.get("$baseUrl/utilities/submeter/$clientId").body()
            Result.success(result)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun addReadingToSubmeter(submeterId: Long, reading: Reading): Result<Submeter> {
        return try {
            val result: Submeter = httpClient.post("$baseUrl/utilities/readings") {
                setBody(reading)
            }.body()
            Result.success(result)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun deleteReadingToSubmeter(readingId: Long): Result<Boolean> {
        return try {
            val result: Boolean = httpClient.delete("$baseUrl/utilities/readings/$readingId").body()
            Result.success(result)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun getSubmeterByReadingId(readingId: Long): Result<Submeter> {
        return try {
            val result: Submeter = httpClient.delete("$baseUrl/utilities/submeters/byReading/$readingId").body()
            Result.success(result)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun getAllSubmeters(): List<Submeter> = httpClient.get("$baseUrl/utilities/submeter").body()
    suspend fun getSubmeter(submeterId: Long): Result<Submeter> {
        return try {
            val result: Submeter =
                httpClient.get("$baseUrl/utilities/submeter/$submeterId").body()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun putSubmeter(id: Long, submeter: Submeter): Result<Submeter> {
        return try {
            val result: Submeter = httpClient.put("$baseUrl/utilities/submeter/$id") {
                setBody(submeter)
            }.body()
            Result.success(result)
        } catch (e: Throwable) {
            println("putSubmeter ERROR: $e")
            Result.failure(e)
        }
    }
    suspend fun postSubmeter(submeter: Submeter): Result<List<Submeter>> {
        return try {
            val result: List<Submeter> = httpClient.post("$baseUrl/utilities/submeter") {
                setBody(submeter)
            }.body()
            Result.success(result)
        } catch (e: Throwable) {
            println("postSubmeter błąd: $e")
            Result.failure(e)
        }
    }
    suspend fun deleteSubmeter(submeterId: Long): Result<List<Submeter>> = httpClient.delete("$baseUrl/utilities/submeter/$submeterId").body()

    suspend fun checkReading(clientId: Long, reading: Reading): Result<Reading> {
        return try {
            val result: Reading = httpClient.post("$baseUrl/utilities/readings/check/$clientId") {
                    setBody(reading)
                }.body()
            Result.success(result)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun createPositionFromReading(clientId: Long, reading: Reading): Result<Position> {
        return try {
            val result: Position = httpClient.post("$baseUrl/utilities/readings/createPosition/$clientId") {
                setBody(reading)
            }.body()
            Result.success(result)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}