package com.kontenery.controller

import com.kontenery.config.ApiConfig.BASE_URL
import com.kontenery.model.Payment
import com.kontenery.model.PaymentDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ApiPayments(
    private val httpClient: HttpClient
) {
    suspend fun getPaymentsForClient(
        clientId: Long,
        from: String?,
        to: String?
    ): List<Payment> = httpClient.get("$BASE_URL/payment/${clientId}/byClient") {
        parameter("clientId", clientId)
        parameter("from", from)
        parameter("to", to)
    }.body()

    suspend fun postPayment(
        paymentDto: PaymentDto
    ): Payment? = httpClient.post("$BASE_URL/payment") {
        contentType(ContentType.Application.Json)
        setBody(paymentDto)
    }.body()

    suspend fun deletePayment(
        paymentId: Long
    ): Boolean = httpClient.delete("$BASE_URL/payment/${paymentId}/delete") {
        parameter("paymentId", paymentId)
    }.body()


}