package cl.duoc.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.app.model.data.config.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SettingsState(
    val isDarkMode: Boolean = false,
    val isLoading: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val userPreferences = UserPreferences(application)
    
    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            val isDarkMode = userPreferences.isDarkModeEnabled.first()
            _settingsState.value = SettingsState(isDarkMode = isDarkMode)
        }
    }
    
    fun toggleDarkMode() {
        viewModelScope.launch {
            val newValue = !_settingsState.value.isDarkMode
            userPreferences.setDarkMode(newValue)
            _settingsState.value = _settingsState.value.copy(isDarkMode = newValue)
        }
    }
}
