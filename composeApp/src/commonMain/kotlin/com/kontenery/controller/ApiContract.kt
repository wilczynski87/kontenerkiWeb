package com.kontenery.controller

import com.kontenery.config.ApiConfig.BASE_URL
import com.kontenery.library.model.Contract
import com.kontenery.library.model.ContractDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put

class ApiContract(
    private val httpClient: HttpClient
) {
    suspend fun getContractsByClient(id: Long): List<Contract> = httpClient.get("$BASE_URL/contract/{id}/client").body()

    suspend fun getContractByProductId(id: Long): Contract? = httpClient.get("$BASE_URL/contract/{id}/product").body()

    suspend fun getContractById(id: Long): Contract? = httpClient.get("$BASE_URL/contract/{id}").body()

    suspend fun postContract(contract: ContractDto): Contract = httpClient.post("$BASE_URL/contract").body()

    suspend fun putContract(id: Long, contract: ContractDto): Contract = httpClient.put("$BASE_URL/contract/{id}").body()

    suspend fun deleteContract(id: Long): Boolean = httpClient.delete("$BASE_URL/contract/{id}").body()
}