package com.example.inventory.data.settings

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit

class EncryptedPrefsManager(context: Context) {

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "inventory_app_prefs",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val SQLCIPHER_PASSPHRASE_KEY = "sqlcipher_passphrase_key"
    }

    fun getOrCreateSqlCipherPassphrase(): String {
        val existing = prefs.getString(SQLCIPHER_PASSPHRASE_KEY, null)
        if (existing != null) {
            return existing
        }

        val keyGen = javax.crypto.KeyGenerator.getInstance("AES")
        keyGen.init(256)
        val secretKey = keyGen.generateKey()
        val passphrase = String(secretKey.encoded, Charsets.UTF_8)

        prefs.edit {
            putString(SQLCIPHER_PASSPHRASE_KEY, passphrase)
        }

        return passphrase
    }

    fun putBoolean(key: String, value: Boolean) = prefs.edit {
        putBoolean(key, value)
    }
    fun getBoolean(key: String, default: Boolean): Boolean = prefs.getBoolean(key, default)

    fun putInt(key: String, value: Int) = prefs.edit {
        putInt(key, value)
    }
    fun getInt(key: String, default: Int): Int = prefs.getInt(key, default)
}