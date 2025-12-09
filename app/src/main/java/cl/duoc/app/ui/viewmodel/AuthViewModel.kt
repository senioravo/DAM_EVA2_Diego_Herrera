package cl.duoc.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.app.data.model.User
import cl.duoc.app.data.preferences.UserPreferences
import cl.duoc.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUser: User? = null,
    val error: String? = null
)

class AuthViewModel(
    private val context: Context
) : ViewModel() {
    
    private val authRepository = AuthRepository(context)
    private val userPreferences = UserPreferences(context)
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    // Exponer las sesiones guardadas
    val savedSessions = userPreferences.savedSessions
    
    init {
        checkLoginStatus()
    }
    
    private fun checkLoginStatus() {
        viewModelScope.launch {
            try {
                // Verificar si hay sesión activa con JWT
                val hasSession = authRepository.hasActiveSession()
                if (hasSession) {
                    // Intentar obtener usuario actual desde backend
                    val result = authRepository.getCurrentUser()
                    if (result.isSuccess) {
                        val user = result.getOrNull()!!
                        userPreferences.saveUserSession(user.id, user.email)
                        _authState.value = AuthState(
                            isLoggedIn = true,
                            currentUser = user
                        )
                    } else {
                        // Token inválido o expirado
                        authRepository.logout()
                        _authState.value = AuthState(isLoggedIn = false)
                    }
                }
            } catch (e: Exception) {
                _authState.value = AuthState(error = e.message)
            }
        }
    }
    
    fun login(email: String, password: String, rememberSession: Boolean = false) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            try {
                val result = authRepository.login(email, password)
                
                if (result.isSuccess) {
                    val user = result.getOrNull()!!
                    
                    // Guardar sesión en preferencias
                    userPreferences.saveUserSession(user.id, user.email)
                    
                    // Guardar credenciales si el usuario lo solicitó
                    if (rememberSession) {
                        userPreferences.saveSession(email, password)
                    }
                    
                    _authState.value = AuthState(
                        isLoggedIn = true,
                        currentUser = user,
                        isLoading = false
                    )
                } else {
                    _authState.value = AuthState(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Email o contraseña incorrectos"
                    )
                }
            } catch (e: Exception) {
                _authState.value = AuthState(
                    isLoading = false,
                    error = e.message ?: "Error al iniciar sesión"
                )
            }
        }
    }
    
    fun register(email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            try {
                if (password != confirmPassword) {
                    _authState.value = AuthState(
                        isLoading = false,
                        error = "Las contraseñas no coinciden"
                    )
                    return@launch
                }
                
                // Extraer username del email (antes del @)
                val username = email.substringBefore("@")
                
                val result = authRepository.register(email, username, password)
                
                if (result.isSuccess) {
                    val user = result.getOrNull()!!
                    userPreferences.saveUserSession(user.id, user.email)
                    _authState.value = AuthState(
                        isLoggedIn = true,
                        currentUser = user,
                        isLoading = false
                    )
                } else {
                    _authState.value = AuthState(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Error al registrar"
                    )
                }
            } catch (e: Exception) {
                _authState.value = AuthState(
                    isLoading = false,
                    error = e.message ?: "Error al registrar"
                )
            }
        }
    }
    
    fun removeSavedSession(email: String) {
        viewModelScope.launch {
            try {
                userPreferences.removeSavedSession(email)
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error al eliminar sesión guardada", e)
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            try {
                // Limpiar token JWT del backend
                authRepository.logout()
                
                // Limpiar preferencias locales
                userPreferences.clearUserSession()
                
                _authState.value = AuthState(isLoggedIn = false)
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(error = e.message)
            }
        }
    }
    
    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}
