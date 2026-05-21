package com.kontenery.ksef.model

import com.kontenery.ksef.dto.TokenInfo

data class KsefSession(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenValidUntil: String,
    val refreshTokenValidUntil: String,
) {
    companion object {
        fun from(tokens: com.kontenery.ksef.dto.AuthenticationTokensResponse): KsefSession =
            from(tokens.accessToken, tokens.refreshToken)

        fun from(access: TokenInfo, refresh: TokenInfo): KsefSession = KsefSession(
            accessToken = access.token,
            refreshToken = refresh.token,
            accessTokenValidUntil = access.validUntil,
            refreshTokenValidUntil = refresh.validUntil,
        )
    }
}
