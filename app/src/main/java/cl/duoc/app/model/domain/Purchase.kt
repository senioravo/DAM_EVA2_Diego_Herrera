package cl.duoc.app.model.domain

import java.util.Date

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
