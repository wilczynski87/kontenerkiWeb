package com.kontenery.ksef.service

import com.kontenery.ksef.dto.InvoiceMetadata
import com.kontenery.ksef.model.InvoiceListRequest
import com.kontenery.ksef.model.KsefSession
import com.kontenery.ksef.repository.KsefInvoiceRepository

data class InvoiceListResult(
    val invoices: List<InvoiceMetadata>,
    val hasMore: Boolean,
    val isTruncated: Boolean,
)

class KsefInvoiceService(
    private val invoiceRepository: KsefInvoiceRepository,
) {
    suspend fun listInvoices(
        session: KsefSession,
        request: InvoiceListRequest,
    ): InvoiceListResult {
        val response = invoiceRepository.queryMetadata(session.accessToken, request)
        return InvoiceListResult(
            invoices = response.invoices,
            hasMore = response.hasMore,
            isTruncated = response.isTruncated,
        )
    }

    suspend fun listAllInvoices(
        session: KsefSession,
        request: InvoiceListRequest,
        maxPages: Int = 50,
    ): List<InvoiceMetadata> {
        val all = mutableListOf<InvoiceMetadata>()
        var pageOffset = request.pageOffset
        var pagesLoaded = 0
        var hasMore = true

        while (hasMore && pagesLoaded < maxPages) {
            val pageRequest = request.copy(pageOffset = pageOffset)
            val page = listInvoices(session, pageRequest)
            all += page.invoices
            hasMore = page.hasMore
            pageOffset += request.pageSize
            pagesLoaded++
        }
        return all
    }
}
