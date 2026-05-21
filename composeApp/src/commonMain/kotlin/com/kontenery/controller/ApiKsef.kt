package com.kontenery.controller

import com.kontenery.config.ApiConfig.baseUrl
import com.kontenery.model.invoice.Invoice
import com.kontenery.model.invoice.KsefSendInvoiceResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/** Klient endpointów KSeF w kontenerkiApi (route `/ksef`). */
class ApiKsef(
    private val httpClient: HttpClient,
) {
    suspend fun sendInvoice(invoice: Invoice): Result<KsefSendInvoiceResponse> {
        return try {
            val result: KsefSendInvoiceResponse = httpClient.post("$baseUrl/ksef/invoices/send") {
                contentType(ContentType.Application.Json)
                setBody(invoice)
            }.body()
            Result.success(result)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun sendInvoiceById(invoiceId: Long): Result<KsefSendInvoiceResponse> {
        return try {
            val result: KsefSendInvoiceResponse =
                httpClient.post("$baseUrl/ksef/invoices/$invoiceId/send").body()
            Result.success(result)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
