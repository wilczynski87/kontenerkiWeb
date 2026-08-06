package com.kontenery.controller

import com.kontenery.config.ApiConfig.baseUrl
import com.kontenery.model.invoice.Invoice
import com.kontenery.model.errors.InvoiceErrorMessage
import com.kontenery.model.enums.now
import com.kontenery.model.invoice.InvoiceSend
import com.kontenery.model.invoice.KsefSendResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.datetime.LocalDate

class ApiInvoice(
    private val httpClient: HttpClient
) {

    suspend fun postPeriodicInvoice(
        clientId: Long,
        period: String? = null,
    ): List<InvoiceErrorMessage> =
        httpClient.postPeriodicInvoiceErrors("$baseUrl/invoice/$clientId") {
            parameter("period", period)
        }

    suspend fun postPeriodicInvoiceAgain(
        invoiceNumber: String,
    ): Result<InvoiceSend> {
        return try {
            val result: InvoiceSend = httpClient.post("$baseUrl/invoice/sendAgain") {
                setBody(invoiceNumber)
            }.body()

            Result.success(result)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }


    suspend fun postPeriodicInvoiceToAllClients(
        period: String? = null,
    ): List<InvoiceErrorMessage> =
        httpClient.postPeriodicInvoiceErrors("$baseUrl/invoice/sendInvoices/forAll") {
            parameter("period", period)
        }

    suspend fun postCustomInvoice(
       clientId: Long,
       customInvoice: Invoice,
    ): Invoice? =
        httpClient.post("$baseUrl/invoice/${clientId}/custom"){
            contentType(ContentType.Application.Json)
            setBody(customInvoice)
        }.body()


    suspend fun fetchInvoicesForClient(
        clientId: Long,
        from: String?,
        to: String?,
    ): List<Invoice> = httpClient.get("$baseUrl/invoice/${clientId}/forClient") {
        parameter("from", from)
        parameter("to", to)
    }.body()

    suspend fun printAllInvoice(
        date: LocalDate? = LocalDate.now()
    ): Boolean = httpClient.get("$baseUrl/invoice/${date}/print").body()

    suspend fun sendInvoiceToKsef(
        invoiceNumber: String,
    ): Result<KsefSendResult> {
        return try {
            val result: KsefSendResult = httpClient.post("$baseUrl/invoice/ksef") {
                setBody(invoiceNumber)
            }.body()
            Result.success(result)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}

/**
 * POST returning [List] of soft business errors.
 * Handles legacy API responses that used 417 + error list instead of 200.
 */
private suspend inline fun HttpClient.postPeriodicInvoiceErrors(
    url: String,
    crossinline block: HttpRequestBuilder.() -> Unit = {},
): List<InvoiceErrorMessage> {
    return try {
        post(url) { block() }.body()
    } catch (e: ClientRequestException) {
        if (e.response.status == HttpStatusCode.ExpectationFailed) {
            e.response.body<List<InvoiceErrorMessage>>()
        } else {
            throw e
        }
    }
}
