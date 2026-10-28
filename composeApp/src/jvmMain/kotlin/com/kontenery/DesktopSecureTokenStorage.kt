package com.kontenery

import com.kontenery.auth.SecureTokenStorage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.util.Properties

class DesktopSecureTokenStorage : SecureTokenStorage {
    private val file: Path = Path.of(System.getProperty("user.home"), ".kontenerki", "session.properties")

    override suspend fun saveAccessToken(token: String) = update { it.setProperty(ACCESS, token) }

    override suspend fun saveRefreshToken(token: String?) = update {
        if (token.isNullOrBlank()) it.remove(REFRESH) else it.setProperty(REFRESH, token)
    }

    override suspend fun getAccessToken(): String? = read().getProperty(ACCESS)?.takeIf { it.isNotBlank() }

    override suspend fun getRefreshToken(): String? = read().getProperty(REFRESH)?.takeIf { it.isNotBlank() }

    override suspend fun clear() = update {
        it.remove(ACCESS)
        it.remove(REFRESH)
    }

    private fun read(): Properties {
        val properties = Properties()
        if (Files.isRegularFile(file)) {
            Files.newInputStream(file).use { properties.load(it) }
        }
        return properties
    }

    private fun update(block: (Properties) -> Unit) {
        Files.createDirectories(file.parent)
        val properties = read()
        block(properties)
        Files.newOutputStream(file).use { properties.store(it, "kontenerki session") }
        runCatching {
            Files.setPosixFilePermissions(
                file,
                setOf(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE),
            )
        }
    }

    private companion object {
        const val ACCESS = "access_token"
        const val REFRESH = "refresh_token"
    }
}
