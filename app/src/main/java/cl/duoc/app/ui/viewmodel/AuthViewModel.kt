package cl.duoc.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.app.data.model.User
import cl.duoc.app.data.preferences.UserPreferences
import cl.duoc.app.data.repository.UserRepository
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
    
    private val userRepository = UserRepository()
    private val userPreferences = UserPreferences(context)
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
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
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            try {
                val user = userRepository.login(email, password)
                if (user != null) {
                    userPreferences.saveUserSession(user.id, user.email)
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
                result.fold(
                    onSuccess = { user ->
                        userPreferences.saveUserSession(user.id, user.email)
                        _authState.value = AuthState(
                            isLoggedIn = true,
                            currentUser = user,
                            isLoading = false
                        )
                    },
                    onFailure = { exception ->
                        _authState.value = AuthState(
                            isLoading = false,
                            error = exception.message ?: "Error al registrar usuario"
                        )
                    }
                )
            } catch (e: Exception) {
                _authState.value = AuthState(
                    isLoading = false,
                    error = e.message ?: "Error al registrar usuario"
                )
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            userPreferences.clearUserSession()
            _authState.value = AuthState(isLoggedIn = false, currentUser = null)
        }
    }
    
    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}
