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
            val result: PaymentsRecogniseList = httpClient.post("$baseUrl/csv/${csvType.endpoint}") {
                contentType(ContentType.Application.Json)
                setBody(message)
            }.body()
            println("csvType, result: $csvType, $result")
            CsvUploadResult.Recognised(result)
        } catch (e: ClientRequestException) {
            CsvUploadResult.Failed(e.message)
        } catch (e: Exception) {
            CsvUploadResult.Failed(e.message ?: "Nie udało się wgrać pliku")
        }
    }
}