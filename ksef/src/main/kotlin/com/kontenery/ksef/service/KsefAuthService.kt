package com.kontenery.ksef.service

import com.kontenery.ksef.model.KsefSession
import com.kontenery.ksef.repository.KsefAuthRepository
import kotlinx.coroutines.delay

class KsefAuthService(
    private val authRepository: KsefAuthRepository,
    private val pollIntervalMs: Long = DEFAULT_POLL_INTERVAL_MS,
    private val pollTimeoutMs: Long = DEFAULT_POLL_TIMEOUT_MS,
) {
    suspend fun authenticate(): KsefSession {
        val challenge = authRepository.fetchChallenge()
        val encryptionCertificate = authRepository.fetchTokenEncryptionCertificate()
        val initResponse = authRepository.submitKsefToken(challenge, encryptionCertificate)

        val authToken = initResponse.authenticationToken.token
        val referenceNumber = initResponse.referenceNumber

        waitForAuthentication(referenceNumber, authToken)

        val tokens = authRepository.redeemToken(authToken)
        return KsefSession.from(tokens)
    }

    private suspend fun waitForAuthentication(referenceNumber: String, authenticationToken: String) {
        val deadline = System.currentTimeMillis() + pollTimeoutMs
        while (System.currentTimeMillis() < deadline) {
            val status = authRepository.getAuthStatus(referenceNumber, authenticationToken)
            when (status.status.code) {
                KsefAuthRepository.AUTH_STATUS_SUCCESS -> {
                    authRepository.ensureAuthenticationSucceeded(status)
                    return
                }
                KsefAuthRepository.AUTH_STATUS_IN_PROGRESS -> delay(pollIntervalMs)
                else -> authRepository.ensureAuthenticationSucceeded(status)
            }
        }
        throw com.kontenery.ksef.error.KsefAuthenticationException(
            "Przekroczono czas oczekiwania na uwierzytelnienie KSeF (${pollTimeoutMs} ms)",
        )
    }

    companion object {
        const val DEFAULT_POLL_INTERVAL_MS = 2_000L
        const val DEFAULT_POLL_TIMEOUT_MS = 120_000L
    }
}
