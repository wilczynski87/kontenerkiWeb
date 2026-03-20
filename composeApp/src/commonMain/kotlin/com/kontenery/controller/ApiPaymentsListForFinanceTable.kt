package com.kontenery.controller

import com.kontenery.config.ApiConfig.baseUrl
import com.kontenery.model.PaymentsListForFinanceTable
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.datetime.LocalDate

class ApiPaymentsListForFinanceTable(
    private val httpClient: HttpClient
) {
    suspend fun getPaymentsListForFinanceTable(
        page: Long?,
        size: Long?,
        from: LocalDate? = null,
        to: LocalDate? = null,
    ): List<PaymentsListForFinanceTable> {
        val results: List<PaymentsListForFinanceTable> =  httpClient.get("$baseUrl/list/clientsPayments") {
            parameter("page", page)
            parameter("size", size)
            parameter("from", from)
            parameter("to", to)
        }.body()
//        println("getPaymentsListForFinanceTable:")
//        results.forEach { println("${it.client?.name} - ${it.client?.isActive}") }
        return results
    }

}