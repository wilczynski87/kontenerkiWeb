package com.kontenery.controller

import io.ktor.client.HttpClient

expect fun createHttpClient(): HttpClient

object ApiClientsService {

    val httpClient by lazy { createHttpClient() }

    val clients by lazy { ApiClients(httpClient) }
    val products by lazy { ApiProduct(httpClient) }
    val invoices by lazy { ApiInvoice(httpClient) }
    val contracts by lazy { ApiContract(httpClient) }
    val payments by lazy { ApiPayments(httpClient) }
    val bankAccounts by lazy { ApiBankAccount(httpClient) }
    val paymentsListForFinanceTable by lazy { ApiPaymentsListForFinanceTable(httpClient) }
    val auth by lazy { ApiAuth(httpClient) }
}