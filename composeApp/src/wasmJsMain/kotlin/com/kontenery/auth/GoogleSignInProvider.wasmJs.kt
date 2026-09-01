@file:OptIn(ExperimentalWasmJsInterop::class)

package com.kontenery.auth

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.js.JsAny
import kotlin.js.Promise

@JsFun("() => !!(window.magazynkiGoogleSignIn && window.magazynkiGoogleSignIn.isAvailable())")
private external fun jsGoogleSignInAvailable(): Boolean

@JsFun("(clientId) => window.magazynkiGoogleSignIn.requestIdToken(clientId)")
private external fun jsRequestGoogleIdToken(clientId: String): Promise<JsAny?>

@OptIn(ExperimentalWasmJsInterop::class)
private object MagazynkiGoogleSignInJs {
    fun isAvailable(): Boolean = jsGoogleSignInAvailable()
    fun requestIdToken(clientId: String): Promise<JsAny?> = jsRequestGoogleIdToken(clientId)
}

actual fun createGoogleSignInProvider(): GoogleSignInProvider? =
    if (MagazynkiGoogleSignInJs.isAvailable()) WasmGoogleSignInProvider() else null

private class WasmGoogleSignInProvider : GoogleSignInProvider {
    override suspend fun requestIdToken(): Result<String> {
        val clientId = com.kontenery.config.googleOAuthClientId()
            ?: return Result.failure(IllegalStateException("Google OAuth client ID is not configured"))

        return runCatching {
            suspendCancellableCoroutine { cont ->
                MagazynkiGoogleSignInJs.requestIdToken(clientId)
                    .then(
                        onFulfilled = { value ->
                            val token = value?.toString()?.trim().orEmpty()
                            if (token.isEmpty()) {
                                cont.resumeWithException(IllegalStateException("Empty Google ID token"))
                            } else {
                                cont.resume(token)
                            }
                            null
                        },
                        onRejected = { error ->
                            cont.resumeWithException(
                                error?.toString()?.let { RuntimeException(it) }
                                    ?: RuntimeException("Google Sign-In failed"),
                            )
                            null
                        },
                    )
            }
        }
    }
}
