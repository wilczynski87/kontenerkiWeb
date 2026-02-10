package com.kontenery.controller

import com.kontenery.config.ApiConfig.baseUrl
import com.kontenery.model.ClientBankAccount
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ApiBankAccount(
    private val httpClient: HttpClient
) {
    suspend fun getClientBankAccountsList(clientId: String ): List<ClientBankAccount> = httpClient.get("$baseUrl/bankAccount/$clientId/getAllForClient") {
        parameter("clientId", clientId)
    }.body()

    suspend fun saveClientBankAccount(clientBankAccount: ClientBankAccount): ClientBankAccount = httpClient.post("$baseUrl/bankAccount/add") {
        contentType(ContentType.Application.Json)
        accept(ContentType.Application.Json)
        setBody(clientBankAccount)
    }.body()

    suspend fun deleteClientBankAccount(
        clientId: String,
        accountNumber: String
    ): Boolean = httpClient.delete("$baseUrl/bankAccount/$clientId/$accountNumber/delete") {
        parameter("clientId", clientId)
        parameter("accountNumber", accountNumber)
    }.body()
}

