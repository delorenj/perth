package sh.delo.perth.core.data.secure

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encrypted key-value storage backed by Android Keystore via [EncryptedSharedPreferences].
 * Use for secrets: LLM API keys, auth tokens.
 */
@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val prefs: SharedPreferences by lazy { createEncryptedPrefs() }

    private fun createEncryptedPrefs(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun getString(key: String): String? = try {
        prefs.getString(key, null)
    } catch (e: Exception) {
        Timber.e(e, "SecureStorage: failed to read key=%s", key)
        null
    }

    fun putString(key: String, value: String) {
        try {
            prefs.edit().putString(key, value).apply()
        } catch (e: Exception) {
            Timber.e(e, "SecureStorage: failed to write key=%s", key)
        }
    }

    fun remove(key: String) {
        try {
            prefs.edit().remove(key).apply()
        } catch (e: Exception) {
            Timber.e(e, "SecureStorage: failed to remove key=%s", key)
        }
    }

    fun clear() {
        try {
            prefs.edit().clear().apply()
        } catch (e: Exception) {
            Timber.e(e, "SecureStorage: failed to clear")
        }
    }

    companion object {
        private const val FILE_NAME = "perth_secure_prefs"

        // Well-known keys
        const val KEY_LLM_API_KEY = "llm_api_key"
        const val KEY_SERVER_AUTH_TOKEN = "server_auth_token"
        const val KEY_SERVER_URL = "server_url"
    }
}
