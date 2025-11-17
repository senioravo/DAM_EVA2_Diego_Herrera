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
        val SAVED_EMAILS = stringSetPreferencesKey("saved_emails")
        val SAVED_PASSWORDS = stringSetPreferencesKey("saved_passwords")
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
    
    // Gestión de sesiones guardadas
    val savedSessions: Flow<List<Pair<String, String>>> = context.dataStore.data.map { preferences ->
        val emails = preferences[PreferencesKeys.SAVED_EMAILS] ?: emptySet()
        val passwords = preferences[PreferencesKeys.SAVED_PASSWORDS] ?: emptySet()
        emails.zip(passwords)
    }
    
    suspend fun saveSession(email: String, password: String) {
        context.dataStore.edit { preferences ->
            val currentEmails = preferences[PreferencesKeys.SAVED_EMAILS]?.toMutableSet() ?: mutableSetOf()
            val currentPasswords = preferences[PreferencesKeys.SAVED_PASSWORDS]?.toMutableSet() ?: mutableSetOf()
            
            // Eliminar sesión existente del mismo email si existe
            val existingIndex = currentEmails.indexOf(email)
            if (existingIndex != -1) {
                val emailsList = currentEmails.toMutableList()
                val passwordsList = currentPasswords.toMutableList()
                emailsList.removeAt(existingIndex)
                passwordsList.removeAt(existingIndex)
                currentEmails.clear()
                currentPasswords.clear()
                currentEmails.addAll(emailsList)
                currentPasswords.addAll(passwordsList)
            }
            
            // Agregar nueva sesión
            currentEmails.add(email)
            currentPasswords.add(password)
            
            preferences[PreferencesKeys.SAVED_EMAILS] = currentEmails
            preferences[PreferencesKeys.SAVED_PASSWORDS] = currentPasswords
        }
    }
    
    suspend fun removeSavedSession(email: String) {
        context.dataStore.edit { preferences ->
            val currentEmails = preferences[PreferencesKeys.SAVED_EMAILS]?.toMutableList() ?: mutableListOf()
            val currentPasswords = preferences[PreferencesKeys.SAVED_PASSWORDS]?.toMutableList() ?: mutableListOf()
            
            val index = currentEmails.indexOf(email)
            if (index != -1) {
                currentEmails.removeAt(index)
                currentPasswords.removeAt(index)
                preferences[PreferencesKeys.SAVED_EMAILS] = currentEmails.toSet()
                preferences[PreferencesKeys.SAVED_PASSWORDS] = currentPasswords.toSet()
            }
        }
    }
}
