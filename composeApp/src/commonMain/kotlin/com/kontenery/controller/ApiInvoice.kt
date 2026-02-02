package com.kontenery.controller

import com.kontenery.config.ApiConfig.baseUrl
import com.kontenery.model.invoice.Invoice
import com.kontenery.library.utils.errors.InvoiceErrorMessage
import com.kontenery.model.enums.now
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.datetime.LocalDate

class ApiInvoice(
    private val httpClient: HttpClient
) {

    suspend fun postPeriodicInvoice(
        clientId: Long,
        period: String? = null,
    ): List<InvoiceErrorMessage> = httpClient.post("$baseUrl/invoice/$clientId") {
        parameter("period", period)
    }.body()

    suspend fun postPeriodicInvoiceAgain(
        invoiceNumber: String,
    ): String = httpClient.get("$baseUrl/invoice/$invoiceNumber/sendAgain").body()


    suspend fun postPeriodicInvoiceToAllClients(
        period: String? = null,
    ): List<InvoiceErrorMessage> = httpClient.post("$baseUrl/invoice/sendInvoices/forAll") {
        parameter("period", period)
    }.body()

    suspend fun postCustomInvoice(
       clientId: Long,
       customInvoice: Invoice,
    ): String? =
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
}