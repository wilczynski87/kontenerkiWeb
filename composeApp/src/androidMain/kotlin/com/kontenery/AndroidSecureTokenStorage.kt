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
import kotlinx.coroutines.flow.first


class AndroidSecureTokenStorage(
    private val context: Context
) : SecureTokenStorage {

    private val aead: Aead by lazy { createAead(context) }
    private val Context.dataStore by preferencesDataStore("secure_tokens")
    private val ACCESS = stringPreferencesKey("access")
    private val REFRESH = stringPreferencesKey("refresh")

    override suspend fun saveAccessToken(token: String) {
        context.dataStore.edit {
            it[ACCESS] = encrypt(token)
        }
    }

    override suspend fun saveRefreshToken(token: String?) {
        if (token == null) return
        context.dataStore.edit {
            it[REFRESH] = encrypt(token)
        }
    }

    override suspend fun getAccessToken(): String? {
        val prefs = context.dataStore.data.first()
        return prefs[ACCESS]?.let { decrypt(it) }
    }

    override suspend fun getRefreshToken(): String? {
        val prefs = context.dataStore.data.first()
        return prefs[REFRESH]?.let { decrypt(it) }
    }

    override suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    private fun encrypt(value: String): String {
        val encrypted = aead.encrypt(value.toByteArray(), null)
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String {
        val decrypted = aead.decrypt(Base64.decode(value, Base64.NO_WRAP), null)
        return String(decrypted)
    }
}

@Suppress("DEPRECATION")
private fun createAead(context: Context): Aead {
    AeadConfig.register()

    val keysetHandle = AndroidKeysetManager.Builder()
        .withSharedPref(context, "tink_keyset", "tink_master_key")
        .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
        .withMasterKeyUri("android-keystore://tink_master_key")
        .build()
        .keysetHandle

//    val aead: Aead = keysetHandle.getPrimitive(Aead::class)
    val aead: Aead = keysetHandle.getPrimitive(Aead::class.java)
    return aead
}
