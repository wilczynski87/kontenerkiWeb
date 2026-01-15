package com.kontenery.controller

import com.kontenery.model.auth.LoginRequest
import com.kontenery.model.auth.LoginResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class ApiAuth(
    private val httpClient: HttpClient
) {

    suspend fun login(email: String, password: String): LoginResponse {
        return httpClient.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }.body()
    }

//    suspend fun getProfile(): Profile {
//        return httpClient.get("/me").body()
//    }
    suspend fun safeRequest(block: suspend () -> HttpResponse): HttpResponse {
        val response = block()
        if (response.status == HttpStatusCode.Unauthorized) {
            httpClient.post("/auth/refresh")
            return block()
        }
        return response
    }


}