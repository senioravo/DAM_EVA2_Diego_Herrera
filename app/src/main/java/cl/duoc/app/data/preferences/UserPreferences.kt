package cl.duoc.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {
    
    private object PreferencesKeys {
        val CURRENT_USER_ID = intPreferencesKey("current_user_id")
        val CURRENT_USER_EMAIL = stringPreferencesKey("current_user_email")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    }
    
    val currentUserId: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CURRENT_USER_ID]
    }
    
    val currentUserEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CURRENT_USER_EMAIL]
    }
    
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_LOGGED_IN] ?: false
    }
    
    val isDarkModeEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_DARK_MODE] ?: false
    }
    
    suspend fun saveUserSession(userId: Int, email: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_USER_ID] = userId
            preferences[PreferencesKeys.CURRENT_USER_EMAIL] = email
            preferences[PreferencesKeys.IS_LOGGED_IN] = true
        }
    }
    
    suspend fun clearUserSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.CURRENT_USER_ID)
            preferences.remove(PreferencesKeys.CURRENT_USER_EMAIL)
            preferences[PreferencesKeys.IS_LOGGED_IN] = false
        }
    }
    
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = enabled
        }
    }
}
