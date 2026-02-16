package com.kontenery.controller

import com.kontenery.auth.TokenManager
import com.kontenery.provideSecureTokenStorage
import io.ktor.client.HttpClient
import com.kontenery.createHttpClient

object ApiClientsService {
    private val tokenManager by lazy { TokenManager(provideSecureTokenStorage()) }

    val httpClient: HttpClient by lazy { createHttpClient(tokenManager) }

    val clients by lazy { ApiClients(httpClient) }
    val products by lazy { ApiProduct(httpClient) }
    val invoices by lazy { ApiInvoice(httpClient) }
    val contracts by lazy { ApiContract(httpClient) }
    val payments by lazy { ApiPayments(httpClient) }
    val bankAccounts by lazy { ApiBankAccount(httpClient) }
    val paymentsListForFinanceTable by lazy { ApiPaymentsListForFinanceTable(httpClient) }
    val auth by lazy { ApiAuth(tokenManager, httpClient) }
    val csvPayments by lazy { ApiCsvPayments(httpClient) }

    val healthCheck by lazy { ApiHealthCheck(httpClient) }

}