package cl.duoc.app.data.model

import java.util.Date

data class User(
    val id: Int,
    val email: String,
    val password: String,
    val profileImageUrl: String? = null,
    val createdAt: Date = Date(),
    val isAdmin: Boolean = false
)

data class Purchase(
    val id: Int,
    val userId: Int,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val totalPrice: Double,
    val purchaseDate: Date,
    val status: PurchaseStatus
)

enum class PurchaseStatus {
    PENDING,
    COMPLETED,
    CANCELLED
}
