package com.kontenery.controller

import com.kontenery.config.ApiConfig.baseUrl
import com.kontenery.library.model.Contract
import com.kontenery.library.model.ContractDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ApiContract(
    private val httpClient: HttpClient
) {
    suspend fun getContractsByClient(id: Long): List<Contract> = httpClient.get("$baseUrl/contract/$id/client").body()

    suspend fun getContractByProductId(id: Long): Contract? = httpClient.get("$baseUrl/contract/$id/product").body()

    suspend fun getContractById(id: Long): Contract? = httpClient.get("$baseUrl/contract/$id").body()

//    suspend fun postContract(contract: ContractDto): Contract = httpClient.post("$baseUrl/contract") {
//        setBody(contract)
//        contentType(ContentType.Application.Json)
//    }.body()
    suspend fun postContract(contract: ContractDto): Result<Contract> {
        return try {
            val response = httpClient.post("$baseUrl/contract") {
                setBody(contract)
                contentType(ContentType.Application.Json)
            }
            if (response.status.value in 200..299) {
                Result.success(response.body())
            } else {
                val errorText = response.bodyAsText()
                println("postContract error: $errorText")
                Result.failure(Exception(errorText))
            }
        } catch (e: Throwable) {
            println(e)
            Result.failure(e)
        }
    }

    suspend fun putContract(id: Long, contract: ContractDto): Contract = httpClient.put("$baseUrl/contract/$id") {
        setBody(contract)
        contentType(ContentType.Application.Json)
    }.body()

    suspend fun deleteContract(id: Long): Boolean = httpClient.delete("$baseUrl/contract/$id").body()
}