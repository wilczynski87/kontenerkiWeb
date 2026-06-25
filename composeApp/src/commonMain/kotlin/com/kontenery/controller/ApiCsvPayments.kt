package com.kontenery.controller

import com.kontenery.config.ApiConfig.baseUrl
import com.kontenery.data.CSVType
import com.kontenery.data.CsvUploadResult
import com.kontenery.data.MessageRequest
import com.kontenery.model.payment.PaymentsRecogniseList
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ApiCsvPayments(
    private val httpClient: HttpClient
) {
    suspend fun uploadCsv(
        message: MessageRequest,
        csvType: CSVType,
    ): CsvUploadResult {
        return try {
            when (csvType) {
                CSVType.ALIOR -> {
                    val result: PaymentsRecogniseList = httpClient.post("$baseUrl/csv/${csvType.endpoint}") {
                        contentType(ContentType.Application.Json)
                        setBody(message)
                    }.body()
                    CsvUploadResult.Recognised(result)
                }
                else -> {
                    val response: MessageRequest = httpClient.post("$baseUrl/csv/${csvType.endpoint}") {
                        contentType(ContentType.Application.Json)
                        setBody(message)
                    }.body()
                    CsvUploadResult.Simple(response.message)
                }
            }
        } catch (e: ClientRequestException) {
            CsvUploadResult.Failed(e.message)
        } catch (e: Exception) {
            CsvUploadResult.Failed(e.message ?: "Nie udało się wgrać pliku")
        }
    }

    suspend fun sendCSVMessage(
        message: MessageRequest,
        csvType: CSVType,
    ): MessageRequest {
        return httpClient.post("$baseUrl/csv/${csvType.endpoint}") {
            contentType(ContentType.Application.Json)
            setBody(message)
        }.body()
    }

    // przykład GET
    suspend fun fetchMessages(
    ): List<MessageRequest> {
        return httpClient.post("$baseUrl/csv/PeKaOSA").body()
    }
}