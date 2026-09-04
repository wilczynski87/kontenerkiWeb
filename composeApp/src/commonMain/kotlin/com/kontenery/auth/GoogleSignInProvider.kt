package com.kontenery.auth

interface GoogleSignInProvider {
    suspend fun requestIdToken(): Result<String>
}

expect fun createGoogleSignInProvider(): GoogleSignInProvider?
