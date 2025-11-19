package cl.duoc.app.model.domain.formularios

data class RegisterUIState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val error: String? = null,
    val loading: Boolean = false,
    val success: Boolean = false
)
