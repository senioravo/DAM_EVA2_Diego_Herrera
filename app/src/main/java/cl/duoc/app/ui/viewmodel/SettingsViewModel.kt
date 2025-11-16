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

data class SettingsState(
    val currentUser: User? = null,
    val isDarkMode: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class SettingsViewModel(
    private val context: Context
) : ViewModel() {
    
    private val userRepository = UserRepository()
    private val userPreferences = UserPreferences(context)
    
    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val isDarkMode = userPreferences.isDarkModeEnabled.first()
                val userId = userPreferences.currentUserId.first()
                val user = userId?.let { userRepository.getUserById(it) }
                
                _settingsState.value = SettingsState(
                    currentUser = user,
                    isDarkMode = isDarkMode
                )
            } catch (e: Exception) {
                _settingsState.value = _settingsState.value.copy(
                    error = e.message ?: "Error al cargar configuración"
                )
            }
        }
    }
    
    fun toggleDarkMode() {
        viewModelScope.launch {
            val newValue = !_settingsState.value.isDarkMode
            userPreferences.setDarkMode(newValue)
            _settingsState.value = _settingsState.value.copy(isDarkMode = newValue)
        }
    }
    
    fun updateProfileImage(imageUrl: String) {
        viewModelScope.launch {
            _settingsState.value = _settingsState.value.copy(isLoading = true, error = null)
            
            try {
                val userId = _settingsState.value.currentUser?.id
                if (userId != null) {
                    val success = userRepository.updateProfileImage(userId, imageUrl)
                    if (success) {
                        val updatedUser = userRepository.getUserById(userId)
                        _settingsState.value = _settingsState.value.copy(
                            currentUser = updatedUser,
                            isLoading = false,
                            successMessage = "Imagen de perfil actualizada"
                        )
                    } else {
                        _settingsState.value = _settingsState.value.copy(
                            isLoading = false,
                            error = "Error al actualizar imagen"
                        )
                    }
                }
            } catch (e: Exception) {
                _settingsState.value = _settingsState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al actualizar imagen"
                )
            }
        }
    }
    
    fun updatePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            _settingsState.value = _settingsState.value.copy(isLoading = true, error = null)
            
            try {
                if (newPassword != confirmPassword) {
                    _settingsState.value = _settingsState.value.copy(
                        isLoading = false,
                        error = "Las contraseñas no coinciden"
                    )
                    return@launch
                }
                
                val userId = _settingsState.value.currentUser?.id
                if (userId != null) {
                    val result = userRepository.updatePassword(userId, currentPassword, newPassword)
                    result.fold(
                        onSuccess = {
                            _settingsState.value = _settingsState.value.copy(
                                isLoading = false,
                                successMessage = "Contraseña actualizada correctamente"
                            )
                        },
                        onFailure = { exception ->
                            _settingsState.value = _settingsState.value.copy(
                                isLoading = false,
                                error = exception.message ?: "Error al actualizar contraseña"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _settingsState.value = _settingsState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al actualizar contraseña"
                )
            }
        }
    }
    
    fun clearMessages() {
        _settingsState.value = _settingsState.value.copy(error = null, successMessage = null)
    }
}
