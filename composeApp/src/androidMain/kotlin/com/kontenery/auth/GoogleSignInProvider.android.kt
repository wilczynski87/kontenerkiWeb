package com.kontenery.auth

import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kontenery.mainActivity

actual fun createGoogleSignInProvider(): GoogleSignInProvider? {
    val clientId = com.kontenery.config.googleOAuthClientId()?.takeIf { it.isNotBlank() } ?: return null
    val activity = mainActivity ?: return null
    return AndroidGoogleSignInProvider(activity, clientId)
}

private class AndroidGoogleSignInProvider(
    private val activity: com.kontenery.MainActivity,
    private val serverClientId: String,
) : GoogleSignInProvider {

    override suspend fun requestIdToken(): Result<String> = try {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = CredentialManager.create(activity).getCredential(
            context = activity,
            request = request,
        )

        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
            Result.success(googleCredential.idToken)
        } else {
            Result.failure(IllegalStateException("Unexpected Google credential type"))
        }
    } catch (cancelled: GetCredentialCancellationException) {
        Result.failure(cancelled)
    } catch (error: Exception) {
        Result.failure(error)
    }
}
