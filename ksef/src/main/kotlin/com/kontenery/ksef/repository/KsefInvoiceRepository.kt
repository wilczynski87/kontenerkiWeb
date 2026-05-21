package com.kontenery.ksef.repository

import com.kontenery.ksef.config.KsefConfig
import com.kontenery.ksef.dto.InvoiceQueryDateRange
import com.kontenery.ksef.dto.InvoiceQueryFilters
import com.kontenery.ksef.dto.QueryInvoicesMetadataResponse
import com.kontenery.ksef.model.InvoiceListRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class KsefInvoiceRepository(
    private val httpClient: HttpClient,
    private val config: KsefConfig,
) {
    suspend fun queryMetadata(
        accessToken: String,
        request: InvoiceListRequest,
    ): QueryInvoicesMetadataResponse {
        val filters = InvoiceQueryFilters(
            subjectType = request.subjectType,
            dateRange = InvoiceQueryDateRange(
                dateType = request.dateType,
                from = request.dateFrom,
                to = request.dateTo,
            ),
        )
        return httpClient.post("${config.baseUrl}/invoices/query/metadata") {
            bearerAuth(accessToken)
            parameter("pageOffset", request.pageOffset)
            parameter("pageSize", request.pageSize)
            parameter("sortOrder", request.sortOrder)
            setBody(filters)
        }.requireBody()
    }
}
