package cl.duoc.app.data

data class LoginUIState(
    val username: String = "",
    val password: String = "",
    val error: String? = null,
    val loading: Boolean = false
)
