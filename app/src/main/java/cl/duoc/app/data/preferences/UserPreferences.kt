package cl.duoc.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {
    
    companion object {
        private val CURRENT_USER_ID = intPreferencesKey("current_user_id")
        private val CURRENT_USER_EMAIL = stringPreferencesKey("current_user_email")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
    }
    
    val currentUserId: Flow<Int?> = context.dataStore.data
        .map { preferences ->
            preferences[CURRENT_USER_ID]
        }
    
    val currentUserEmail: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[CURRENT_USER_EMAIL]
        }
    
    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_LOGGED_IN] ?: false
        }
    
    val isDarkModeEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_ENABLED] ?: false
        }
    
    suspend fun saveUserSession(userId: Int, email: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_USER_ID] = userId
            preferences[CURRENT_USER_EMAIL] = email
            preferences[IS_LOGGED_IN] = true
        }
    }
    
    suspend fun clearUserSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(CURRENT_USER_ID)
            preferences.remove(CURRENT_USER_EMAIL)
            preferences[IS_LOGGED_IN] = false
        }
    }
    
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_ENABLED] = enabled
        }
    }
}
