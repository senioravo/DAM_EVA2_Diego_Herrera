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
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class SettingsViewModel(
    private val context: Context
) : ViewModel() {
    
    private val userRepository = UserRepository.getInstance()
    private val userPreferences = UserPreferences(context)
    
    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val userId = userPreferences.currentUserId.first()
                val isDarkMode = userPreferences.isDarkModeEnabled.first()
                
                userId?.let {
                    val user = userRepository.getUserById(it)
                    _settingsState.value = SettingsState(
                        currentUser = user,
                        isDarkMode = isDarkMode
                    )
                }
            } catch (e: Exception) {
                _settingsState.value = _settingsState.value.copy(
                    errorMessage = e.message
                )
            }
        }
    }
    
    fun toggleDarkMode() {
        viewModelScope.launch {
            try {
                val newDarkMode = !_settingsState.value.isDarkMode
                userPreferences.setDarkMode(newDarkMode)
                _settingsState.value = _settingsState.value.copy(isDarkMode = newDarkMode)
            } catch (e: Exception) {
                _settingsState.value = _settingsState.value.copy(
                    errorMessage = e.message
                )
            }
        }
    }
    
    fun updateProfileImage(imageUrl: String) {
        viewModelScope.launch {
            _settingsState.value = _settingsState.value.copy(isLoading = true)
            
            try {
                // Primero obtener el userId actual desde preferences si no está en el estado
                var userId = _settingsState.value.currentUser?.id
                if (userId == null) {
                    userId = userPreferences.currentUserId.first()
                    android.util.Log.d("SettingsViewModel", "UserId obtenido desde preferences: $userId")
                }
                android.util.Log.d("SettingsViewModel", "Actualizando imagen para userId: $userId, imageUrl: $imageUrl")
                if (userId != null) {
                    val success = userRepository.updateProfileImage(userId, imageUrl)
                    android.util.Log.d("SettingsViewModel", "Update success: $success")
                    if (success) {
                        val updatedUser = userRepository.getUserById(userId)
                        android.util.Log.d("SettingsViewModel", "Usuario actualizado: ${updatedUser?.profileImageUrl}")
                        _settingsState.value = _settingsState.value.copy(
                            currentUser = updatedUser,
                            isLoading = false,
                            successMessage = "Imagen de perfil actualizada"
                        )
                    } else {
                        _settingsState.value = _settingsState.value.copy(
                            isLoading = false,
                            errorMessage = "Error al actualizar imagen"
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "Error al actualizar imagen", e)
                _settingsState.value = _settingsState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }
    
    fun updatePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            _settingsState.value = _settingsState.value.copy(isLoading = true)
            
            try {
                if (newPassword != confirmPassword) {
                    _settingsState.value = _settingsState.value.copy(
                        isLoading = false,
                        errorMessage = "Las contraseñas no coinciden"
                    )
                    return@launch
                }
                
                val userId = _settingsState.value.currentUser?.id
                if (userId != null) {
                    val result = userRepository.updatePassword(userId, currentPassword, newPassword)
                    
                    if (result.isSuccess) {
                        _settingsState.value = _settingsState.value.copy(
                            isLoading = false,
                            successMessage = "Contraseña actualizada correctamente"
                        )
                    } else {
                        _settingsState.value = _settingsState.value.copy(
                            isLoading = false,
                            errorMessage = result.exceptionOrNull()?.message
                        )
                    }
                }
            } catch (e: Exception) {
                _settingsState.value = _settingsState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }
    
    fun clearMessages() {
        _settingsState.value = _settingsState.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }
}
