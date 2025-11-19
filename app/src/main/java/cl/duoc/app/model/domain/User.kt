package cl.duoc.app.model.domain

import java.util.Date

data class User(
    val id: Int,
    val email: String,
    val password: String,
    val profileImageUrl: String? = null,
    val createdAt: Date = Date(),
    val isAdmin: Boolean = false
)
