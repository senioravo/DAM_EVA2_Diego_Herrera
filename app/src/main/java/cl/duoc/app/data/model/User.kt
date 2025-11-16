package cl.duoc.app.data.model

import java.time.LocalDateTime

data class User(
    val id: Int,
    val email: String,
    val password: String,
    val profileImageUrl: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isAdmin: Boolean = false
)

data class Purchase(
    val id: Int,
    val userId: Int,
    val productId: Int,
    val productName: String,
    val productImage: String,
    val price: Double,
    val quantity: Int,
    val totalAmount: Double,
    val purchaseDate: LocalDateTime,
    val status: PurchaseStatus = PurchaseStatus.COMPLETED
)

enum class PurchaseStatus {
    PENDING,
    COMPLETED,
    CANCELLED
}
