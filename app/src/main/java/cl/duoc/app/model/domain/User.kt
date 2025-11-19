package cl.duoc.app.model.domain

data class User(
    val id: Int = 0,
    val username: String,
    val email: String,
    val password: String = "",
    val profileImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
