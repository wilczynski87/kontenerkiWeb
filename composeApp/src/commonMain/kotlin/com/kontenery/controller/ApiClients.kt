package com.kontenery.controller

import com.kontenery.config.ApiConfig.BASE_URL
import com.kontenery.model.Client
import com.kontenery.model.ClientOnList
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class ApiClients(
    private val httpClient: HttpClient
) {
    suspend fun getClientList(
        page: Int,
        size: Int
    ): List<ClientOnList> =
        httpClient.get("$BASE_URL/list/clients") {
            parameter("page", page)
            parameter("size", size)
        }.body()

    suspend fun clientListSize(): Long =
        httpClient.get("$BASE_URL/list/clients/count").body()

    suspend fun getClientData(id: Long): Client =
        httpClient.get("$BASE_URL/client/$id/id").body()

    suspend fun saveClient(clientData: Client): Client =
        httpClient.post("$BASE_URL/client") {
            setBody(clientData)
        }.body()

    suspend fun updateClient(id: Long, clientData: Client): Client =
        httpClient.put("$BASE_URL/client/$id") {
            setBody(clientData)
        }.body()

    suspend fun deleteClient(id: Long) {
        httpClient.delete("$BASE_URL/client/$id/id")
    }
}