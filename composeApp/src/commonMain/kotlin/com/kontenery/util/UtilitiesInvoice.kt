package com.kontenery.util

fun clientIdForUtilityReading(submeterClientId: Long?, clientId: Long?): Long? {
    val resolved = submeterClientId ?: clientId
    return resolved?.takeIf { it > 0L }
}
