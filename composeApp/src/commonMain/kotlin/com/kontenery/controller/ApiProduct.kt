package com.kontenery.controller

import com.kontenery.config.ApiConfig.baseUrl
import com.kontenery.model.Product
import com.kontenery.model.Product.Container
import com.kontenery.model.Product.Yard
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ApiProduct(
    private val httpClient: HttpClient
) {
    suspend fun getProductList(
        page: Int,
        size: Int
    ): List<Product> = httpClient.get("$baseUrl/list/products") {
        parameter("page", page)
        parameter("size", size)
    }.body()

    suspend fun getProductData(id: Long): Product? = httpClient.get("$baseUrl/products/findById/{id}") {
        parameter("id", id)
    }.body()

    suspend fun saveProduct(product: Product): Product = httpClient.post("$baseUrl/products") {
        setBody(product)
    }.body()

    suspend fun saveContainer(container: Container): Container = httpClient.post("$baseUrl/products/container") {
        contentType(ContentType.Application.Json)
        setBody(container)
    }.body()

    suspend fun saveYard(yard: Yard): Yard = httpClient.post("$baseUrl/products/yard") {
        contentType(ContentType.Application.Json)
        setBody(yard)
    }.body()

    suspend fun updateYard(id: Long, product: Yard): Yard = httpClient.put("$baseUrl/products/yard/{id}") {
        parameter("id", id)
        contentType(ContentType.Application.Json)
        setBody(product)
    }.body()

    suspend fun updateContainer(id: Long, product: Container): Container = httpClient.put("$baseUrl/products/container/{id}") {
        parameter("id", id)
        contentType(ContentType.Application.Json)
        setBody(product)
    }.body()

    suspend fun deleteProduct(id: Long) = httpClient.delete("$baseUrl/products/{id}") {
        parameter("id", id)
    }

}