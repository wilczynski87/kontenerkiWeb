package com.kontenery

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.kontenery.auth.SecureTokenStorage
import com.kontenery.logError
import com.kontenery.logDebug
import kotlinx.coroutines.flow.first
import java.security.GeneralSecurityException
import java.security.KeyStore

private val Context.secureTokenDataStore by preferencesDataStore("secure_tokens")

class AndroidSecureTokenStorage(
    private val context: Context,
) : SecureTokenStorage {

    @Volatile
    private var aead: Aead? = null

    private val ACCESS = stringPreferencesKey("access")
    private val REFRESH = stringPreferencesKey("refresh")

    private fun aeadOrCreate(): Aead {
        aead?.let { return it }
        return synchronized(this) {
            aead ?: createAeadWithRecovery(context).also { aead = it }
        }
    }

    override suspend fun saveAccessToken(token: String) {
        context.secureTokenDataStore.edit {
            it[ACCESS] = encrypt(token)
        }
    }

    override suspend fun saveRefreshToken(token: String?) {
        if (token == null) return
        context.secureTokenDataStore.edit {
            it[REFRESH] = encrypt(token)
        }
    }

    override suspend fun getAccessToken(): String? =
        readDecrypted(ACCESS)

    override suspend fun getRefreshToken(): String? =
        readDecrypted(REFRESH)

    override suspend fun clear() {
        context.secureTokenDataStore.edit { it.clear() }
    }

    private suspend fun readDecrypted(key: androidx.datastore.preferences.core.Preferences.Key<String>): String? {
        val prefs = context.secureTokenDataStore.data.first()
        val encrypted = prefs[key] ?: return null
        return try {
            decrypt(encrypted)
        } catch (e: GeneralSecurityException) {
            logError(
                "SecureTokenStorage",
                "Nie można odszyfrować tokenów (${e.javaClass.simpleName}), czyszczę magazyn",
            )
            resetAllKeyMaterial()
            null
        }
    }

    private fun encrypt(value: String): String {
        val encrypted = aeadOrCreate().encrypt(value.toByteArray(), null)
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String {
        val decrypted = aeadOrCreate().decrypt(Base64.decode(value, Base64.NO_WRAP), null)
        return String(decrypted)
    }

    private suspend fun resetAllKeyMaterial() {
        aead = null
        clear()
        resetTinkKeyMaterial(context)
    }
}

@Suppress("DEPRECATION")
private fun createAeadWithRecovery(context: Context): Aead {
    AeadConfig.register()
    return try {
        buildAead(context)
    } catch (first: Exception) {
        logError("SecureTokenStorage", "Inicjalizacja klucza Tink nie powiodła się: $first")
        resetTinkKeyMaterial(context)
        try {
            buildAead(context)
        } catch (second: Exception) {
            logError("SecureTokenStorage", "Ponowna inicjalizacja klucza nie powiodła się: $second")
            throw second
        }
    }
}

@Suppress("DEPRECATION")
private fun buildAead(context: Context): Aead {
    val keysetHandle = AndroidKeysetManager.Builder()
        .withSharedPref(context, "tink_keyset", "tink_master_key")
        .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
        .withMasterKeyUri("android-keystore://tink_master_key")
        .build()
        .keysetHandle

    return keysetHandle.getPrimitive(Aead::class.java)
}

private fun resetTinkKeyMaterial(context: Context) {
    logDebug("SecureTokenStorage", "Resetowanie materiału kluczy Tink")
    context.getSharedPreferences("tink_keyset", Context.MODE_PRIVATE).edit().clear().apply()
    runCatching {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (keyStore.containsAlias("tink_master_key")) {
            keyStore.deleteEntry("tink_master_key")
        }
    }.onFailure {
        logError("SecureTokenStorage", "Nie udało się usunąć klucza Keystore: $it")
    }
}
