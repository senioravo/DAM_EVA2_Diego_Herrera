package cl.duoc.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.app.model.data.config.AppDatabase
import cl.duoc.app.model.data.config.UserPreferences
import cl.duoc.app.model.data.repositories.UserRepository
import cl.duoc.app.model.domain.User
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

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val userRepository = UserRepository(database.userDao())
    private val userPreferences = UserPreferences(application)
    
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
                val isLoggedIn = userPreferences.isLoggedIn.first()
                if (isLoggedIn) {
                    val userId = userPreferences.currentUserId.first()
                    userId?.let {
                        val user = userRepository.getUserById(it)
                        _authState.value = AuthState(
                            isLoggedIn = true,
                            currentUser = user
                        )
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
                val user = userRepository.login(email, password)
                if (user != null) {
                    // Guardar sesión actual
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
                        error = "Email o contraseña incorrectos"
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
                
                val result = userRepository.register(email, password)
                
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
