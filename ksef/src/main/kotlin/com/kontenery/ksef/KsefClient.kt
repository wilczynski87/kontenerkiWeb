package com.kontenery.ksef

import com.kontenery.ksef.config.KsefConfig
import com.kontenery.ksef.dto.InvoiceMetadata
import com.kontenery.ksef.http.KsefHttpClientFactory
import com.kontenery.ksef.model.InvoiceListRequest
import com.kontenery.ksef.model.KsefSession
import com.kontenery.ksef.repository.KsefAuthRepository
import com.kontenery.ksef.repository.KsefInvoiceRepository
import com.kontenery.ksef.repository.KsefSecurityRepository
import com.kontenery.ksef.service.InvoiceListResult
import com.kontenery.ksef.service.KsefAuthService
import com.kontenery.ksef.service.KsefInvoiceService
import io.ktor.client.HttpClient

/**
 * Klient KSeF API v2 — uwierzytelnianie tokenem oraz pobieranie listy faktur (metadane).
 *
 * Przykład:
 * ```
 * val client = KsefClient(
 *     KsefConfig(
 *         nip = "1234567890",
 *         ksefToken = System.getenv("KSEF_TOKEN")!!,
 *         environment = KsefEnvironment.TEST,
 *     ),
 * )
 * val session = client.authenticate()
 * val invoices = client.getInvoices(session, InvoiceListRequest.lastMonths(3))
 * ```
 */
class KsefClient(
    config: KsefConfig,
    private val httpClient: HttpClient = KsefHttpClientFactory.create(),
    authService: KsefAuthService? = null,
    invoiceService: KsefInvoiceService? = null,
) : AutoCloseable {
    private val config: KsefConfig = config.validated()

    private val securityRepository = KsefSecurityRepository(httpClient, this.config)
    private val authRepository = KsefAuthRepository(httpClient, this.config, securityRepository)
    private val invoiceRepository = KsefInvoiceRepository(httpClient, this.config)

    private val authService: KsefAuthService = authService ?: KsefAuthService(authRepository)
    private val invoiceService: KsefInvoiceService = invoiceService ?: KsefInvoiceService(invoiceRepository)

    /** Pełny proces logowania tokenem KSeF — zwraca parę access/refresh JWT. */
    suspend fun authenticate(): KsefSession = authService.authenticate()

    /** Jedna strona metadanych faktur. */
    suspend fun getInvoices(
        session: KsefSession,
        request: InvoiceListRequest = InvoiceListRequest.lastMonths(),
    ): InvoiceListResult = invoiceService.listInvoices(session, request)

    /** Wszystkie strony wyników (do [maxPages]). */
    suspend fun getAllInvoices(
        session: KsefSession,
        request: InvoiceListRequest = InvoiceListRequest.lastMonths(),
        maxPages: Int = 50,
    ): List<InvoiceMetadata> = invoiceService.listAllInvoices(session, request, maxPages)

    override fun close() {
        httpClient.close()
    }
}
