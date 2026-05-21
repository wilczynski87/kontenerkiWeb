package com.kontenery.ksef.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationChallengeResponse(
    val challenge: String,
    val timestamp: String? = null,
    val timestampMs: Long,
    val clientIp: String? = null,
)

@Serializable
data class TokenInfo(
    val token: String,
    val validUntil: String,
)

@Serializable
data class AuthenticationInitResponse(
    val referenceNumber: String,
    val authenticationToken: TokenInfo,
)

@Serializable
data class StatusInfo(
    val code: Int,
    val description: String,
    val details: List<String>? = null,
)

@Serializable
data class AuthenticationOperationStatusResponse(
    val status: StatusInfo,
    val startDate: String? = null,
)

@Serializable
data class AuthenticationTokensResponse(
    val accessToken: TokenInfo,
    val refreshToken: TokenInfo,
)

@Serializable
data class AuthenticationContextIdentifier(
    val type: String,
    val value: String,
)

@Serializable
data class InitTokenAuthenticationRequest(
    val challenge: String,
    val contextIdentifier: AuthenticationContextIdentifier,
    val encryptedToken: String,
    val publicKeyId: String? = null,
)

@Serializable
data class PublicKeyCertificate(
    val certificate: String,
    val certificateId: String? = null,
    val publicKeyId: String? = null,
    val validFrom: String? = null,
    val validTo: String? = null,
    val usage: List<String> = emptyList(),
)
