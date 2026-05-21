package com.kontenery.ksef.crypto

import com.kontenery.ksef.dto.PublicKeyCertificate
import com.kontenery.ksef.error.KsefException
import java.security.cert.CertificateFactory
import java.security.spec.MGF1ParameterSpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

object KsefTokenEncryptor {
    private const val KSEF_TOKEN_USAGE = "KsefTokenEncryption"

    fun selectTokenEncryptionCertificate(certificates: List<PublicKeyCertificate>): PublicKeyCertificate {
        val match = certificates.firstOrNull { cert ->
            cert.usage.any { it.equals(KSEF_TOKEN_USAGE, ignoreCase = true) }
        }
        return match ?: throw KsefException(
            "Brak certyfikatu KSeF z usage=$KSEF_TOKEN_USAGE w odpowiedzi /security/public-key-certificates",
        )
    }

    fun encryptToken(
        ksefToken: String,
        challengeTimestampMs: Long,
        certificate: PublicKeyCertificate,
    ): String {
        val payload = "$ksefToken|$challengeTimestampMs"
        val publicKey = parsePublicKey(certificate.certificate)
        val cipher = Cipher.getInstance("RSA/ECB/OAEPPadding")
        val oaepSpec = OAEPParameterSpec(
            "SHA-256",
            "MGF1",
            MGF1ParameterSpec.SHA256,
            PSource.PSpecified.DEFAULT,
        )
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepSpec)
        val encrypted = cipher.doFinal(payload.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encrypted)
    }

    private fun parsePublicKey(certificateBase64: String): java.security.PublicKey {
        val der = Base64.getDecoder().decode(certificateBase64)
        val factory = CertificateFactory.getInstance("X.509")
        val certificate = factory.generateCertificate(der.inputStream())
        return certificate.publicKey
    }
}
