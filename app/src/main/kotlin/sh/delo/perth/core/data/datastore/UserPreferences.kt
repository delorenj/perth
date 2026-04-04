package sh.delo.perth.core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import sh.delo.perth.core.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private object Keys {
        val SERVER_URL = stringPreferencesKey("server_url")
        val VOICE_MODE = stringPreferencesKey("voice_mode")
    }

    val serverUrlFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[Keys.SERVER_URL]
    }

    val voiceModeFlow: Flow<SettingsRepository.VoiceMode> = context.dataStore.data.map { prefs ->
        prefs[Keys.VOICE_MODE]
            ?.let { runCatching { SettingsRepository.VoiceMode.valueOf(it) }.getOrNull() }
            ?: SettingsRepository.VoiceMode.Transcription
    }

    suspend fun saveServerUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SERVER_URL] = url
        }
    }

    suspend fun clearServerUrl() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.SERVER_URL)
        }
    }

    suspend fun saveVoiceMode(mode: SettingsRepository.VoiceMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.VOICE_MODE] = mode.name
        }
    }
}
