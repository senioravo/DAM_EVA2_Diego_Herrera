package cl.duoc.app.model.domain

import java.util.Date

data class Purchase(
    val id: Int = 0,
    val userId: Int,
    val total: Double,
    val shippingAddress: String,
    val paymentMethod: PaymentMethod,
    val status: PurchaseStatus,
    val createdAt: Date
)

enum class PaymentMethod {
    DEBIT_CARD,
    CREDIT_CARD
}

enum class PurchaseStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    CANCELLED
}
