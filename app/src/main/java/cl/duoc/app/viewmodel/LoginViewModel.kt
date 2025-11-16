package cl.duoc.app.viewmodel

import androidx.lifecycle.ViewModel
import cl.duoc.app.data.LoginUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {

    private val _estado = MutableStateFlow(LoginUIState())
    val estado: StateFlow<LoginUIState> = _estado.asStateFlow()

    fun onUsernameChange(v: String) = _estado.update { it.copy(username = v, error = null) }
    fun onPasswordChange(v: String) = _estado.update { it.copy(password = v, error = null) }

    fun autenticar(onSuccess: () -> Unit) {
        val s = _estado.value
        if (s.username == "admin" && s.password == "admin") {
            _estado.update { it.copy(error = null) }
            onSuccess()
        } else {
            _estado.update { it.copy(error = "Credenciales inválidas") }
        }
    }
}
