package cl.duoc.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(name = "token_prefs")

class TokenManager(private val context: Context) {
    
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    }
    
    // Guardar token y datos de usuario
    suspend fun saveToken(token: String, userId: Int, email: String) {
        context.tokenDataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId.toString()
            preferences[USER_EMAIL_KEY] = email
        }
    }
    
    // Obtener token (bloqueante para uso en interceptor)
    fun getToken(): String? {
        return runBlocking {
            context.tokenDataStore.data.map { preferences ->
                preferences[TOKEN_KEY]
            }.first()
        }
    }
    
    // Obtener token como Flow
    fun getTokenFlow(): Flow<String?> {
        return context.tokenDataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }
    
    // Obtener user ID
    suspend fun getUserId(): Int? {
        return context.tokenDataStore.data.map { preferences ->
            preferences[USER_ID_KEY]?.toIntOrNull()
        }.first()
    }
    
    // Obtener email
    suspend fun getUserEmail(): String? {
        return context.tokenDataStore.data.map { preferences ->
            preferences[USER_EMAIL_KEY]
        }.first()
    }
    
    // Limpiar token (logout)
    suspend fun clearToken() {
        context.tokenDataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    // Verificar si hay token guardado
    suspend fun hasToken(): Boolean {
        return context.tokenDataStore.data.map { preferences ->
            preferences[TOKEN_KEY] != null
        }.first()
    }
}
