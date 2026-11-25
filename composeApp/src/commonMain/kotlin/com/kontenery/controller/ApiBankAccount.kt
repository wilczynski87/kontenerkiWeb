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
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart

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

    suspend fun ignoreAccount(accountNumber: String): ClientBankAccount = httpClient.post("$baseUrl/bankAccount/ignore") {
        contentType(ContentType.Application.Json)
        accept(ContentType.Application.Json)
        setBody(ClientBankAccount(bankAccount = accountNumber, ignored = true))
    }.body()

    suspend fun listIgnored(): List<ClientBankAccount> =
        httpClient.get("$baseUrl/bankAccount/ignored").body()

    suspend fun assignBankAccount(id: Long, clientBankAccount: ClientBankAccount): ClientBankAccount =
        httpClient.put("$baseUrl/bankAccount/$id") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(clientBankAccount.copy(id = id, ignored = false))
        }.body()

    suspend fun deleteClientBankAccount(
        clientId: String,
        accountNumber: String
    ): Boolean = httpClient.delete(
        "$baseUrl/bankAccount/${clientId.encodeURLPathPart()}/${accountNumber.encodeURLPathPart()}/delete"
    ) {
        parameter("clientId", clientId)
        parameter("accountNumber", accountNumber)
    }.body()
}

