package com.kontenery.ksef.config

import com.kontenery.ksef.error.KsefValidationException

/**
 * Konfiguracja klienta KSeF API v2.
 *
 * @param nip NIP podmiotu (kontekst uwierzytelnienia), 10 cyfr.
 * @param ksefToken Sekret tokena wygenerowanego w MCU (Module Certyfikatów i Uprawnień).
 * Jeśli wklejono cały ciąg z portala w formacie `reference|secret`, używana jest część po ostatnim `|`.
 * @param environment Środowisko API (domyślnie produkcja).
 */
data class KsefConfig(
    val nip: String,
    val ksefToken: String,
    val environment: KsefEnvironment = KsefEnvironment.PRODUCTION,
) {
    val baseUrl: String get() = environment.baseUrl

    fun validated(): KsefConfig {
        val normalizedNip = nip.filter { it.isDigit() }
        require(normalizedNip.length == 10) {
            throw KsefValidationException("NIP musi składać się z 10 cyfr")
        }
        val token = KsefConfig.normalizeKsefToken(ksefToken)
        require(token.isNotBlank()) {
            throw KsefValidationException("Token KSeF nie może być pusty")
        }
        return copy(nip = normalizedNip, ksefToken = token)
    }

    companion object {
        fun normalizeKsefToken(raw: String): String {
            val trimmed = raw.trim()
            if (!trimmed.contains('|')) return trimmed
            return trimmed.substringAfterLast('|').trim()
        }
    }
}
