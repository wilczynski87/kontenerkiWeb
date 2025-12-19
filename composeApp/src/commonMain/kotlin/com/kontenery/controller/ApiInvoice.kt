package com.kontenery.controller

import com.kontenery.config.ApiConfig.BASE_URL
import com.kontenery.library.model.invoice.Invoice
import com.kontenery.library.utils.errors.InvoiceErrorMessage
import com.kontenery.model.enums.now
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import kotlinx.datetime.LocalDate

class ApiInvoice(
    private val httpClient: HttpClient
) {

    suspend fun postPeriodicInvoice(
        clientId: Long,
        period: String? = null,
    ): List<InvoiceErrorMessage> = httpClient.post("$BASE_URL/invoice/$clientId") {
        parameter("period", period)
    }.body()

    suspend fun postPeriodicInvoiceAgain(
        invoiceNumber: String,
    ): String = httpClient.get("$BASE_URL/invoice/$invoiceNumber/sendAgain").body()


    suspend fun postPeriodicInvoiceToAllClients(
        period: String? = null,
    ): List<InvoiceErrorMessage> = httpClient.post("$BASE_URL/invoice/sendInvoices/forAll") {
        parameter("period", period)
    }.body()

    suspend fun postCustomInvoice(
        clientID: Long,
       customInvoice: Invoice,
    ): String? =
        httpClient.post("$BASE_URL/invoice/{clientId}/custom").body()


    suspend fun fetchInvoicesForClient(
        clientId: Long,
        from: String?,
        to: String?,
    ): List<Invoice> = httpClient.get("$BASE_URL/invoice/{clientId}/forClient").body()

    suspend fun printAllInvoice(
        date: LocalDate? = LocalDate.now()
    ): Boolean = httpClient.get("$BASE_URL/invoice/{date}/print").body()
}