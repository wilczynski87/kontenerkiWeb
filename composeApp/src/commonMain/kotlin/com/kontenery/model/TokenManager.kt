package com.kontenery.model

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

//object TokenManager {
//    private var accessToken: String? = null
//    private var refreshToken: String? = null
//    private var refreshing = false
//    private val mutex = Mutex()
//
//    fun setTokens(access: String, refresh: String?) {
//        accessToken = access
//        refreshToken = refresh
//        println("Tokens set: access=${access.take(20)}..., refresh=${refresh?.take(20)}...")
//    }
//
//    fun getAccessToken(): String? = accessToken
//
//    fun getRefreshToken(): String? = refreshToken
//
//    suspend fun clearTokens() {
//        mutex.withLock {
//            accessToken = null
//            refreshToken = null
//            refreshing = false
//        }
//        println("Tokens cleared")
//    }
//
//    fun isAuthenticated(): Boolean = accessToken != null
//
//    suspend fun isRefreshing(): Boolean = mutex.withLock { refreshing }
//
//    suspend fun setRefreshing(value: Boolean) {
//        mutex.withLock { refreshing = value }
//    }
//}