package com.kontenery.ksef.repository

import com.kontenery.ksef.config.KsefConfig
import com.kontenery.ksef.crypto.KsefTokenEncryptor
import com.kontenery.ksef.dto.AuthenticationChallengeResponse
import com.kontenery.ksef.dto.AuthenticationContextIdentifier
import com.kontenery.ksef.dto.AuthenticationInitResponse
import com.kontenery.ksef.dto.AuthenticationOperationStatusResponse
import com.kontenery.ksef.dto.AuthenticationTokensResponse
import com.kontenery.ksef.dto.InitTokenAuthenticationRequest
import com.kontenery.ksef.dto.PublicKeyCertificate
import com.kontenery.ksef.error.KsefAuthenticationException
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class KsefAuthRepository(
    private val httpClient: HttpClient,
    private val config: KsefConfig,
    private val securityRepository: KsefSecurityRepository,
) {
    suspend fun fetchChallenge(): AuthenticationChallengeResponse =
        httpClient.post("${config.baseUrl}/auth/challenge").requireBody()

    suspend fun submitKsefToken(
        challenge: AuthenticationChallengeResponse,
        encryptionCertificate: PublicKeyCertificate,
    ): AuthenticationInitResponse {
        val encryptedToken = KsefTokenEncryptor.encryptToken(
            ksefToken = config.ksefToken,
            challengeTimestampMs = challenge.timestampMs,
            certificate = encryptionCertificate,
        )
        val request = InitTokenAuthenticationRequest(
            challenge = challenge.challenge,
            contextIdentifier = AuthenticationContextIdentifier(
                type = "Nip",
                value = config.nip,
            ),
            encryptedToken = encryptedToken,
            publicKeyId = encryptionCertificate.publicKeyId,
        )
        return httpClient.post("${config.baseUrl}/auth/ksef-token") {
            setBody(request)
        }.requireBody()
    }

    suspend fun getAuthStatus(
        referenceNumber: String,
        authenticationToken: String,
    ): AuthenticationOperationStatusResponse =
        httpClient.get("${config.baseUrl}/auth/$referenceNumber") {
            bearerAuth(authenticationToken)
        }.requireBody()

    suspend fun redeemToken(authenticationToken: String): AuthenticationTokensResponse =
        httpClient.post("${config.baseUrl}/auth/token/redeem") {
            bearerAuth(authenticationToken)
        }.requireBody()

    suspend fun fetchTokenEncryptionCertificate(): PublicKeyCertificate {
        val certificates = securityRepository.fetchPublicKeyCertificates()
        return KsefTokenEncryptor.selectTokenEncryptionCertificate(certificates)
    }

    fun ensureAuthenticationSucceeded(status: AuthenticationOperationStatusResponse) {
        if (status.status.code != AUTH_STATUS_SUCCESS) {
            val details = status.status.details?.joinToString(", ").orEmpty()
            val suffix = if (details.isNotBlank()) " ($details)" else ""
            throw KsefAuthenticationException(
                "Uwierzytelnianie KSeF nie powiodło się: ${status.status.code} – ${status.status.description}$suffix",
                statusCode = status.status.code,
            )
        }
    }

    companion object {
        const val AUTH_STATUS_SUCCESS = 200
        const val AUTH_STATUS_IN_PROGRESS = 100
    }
}
