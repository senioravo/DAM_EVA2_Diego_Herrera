package cl.duoc.app.data.model

data class CartItem(
    val product: Product,
    val quantity: Int = 1
) {
    val subtotal: Double
        get() = product.price * quantity
}

data class Order(
    val id: Int,
    val userId: Int,
    val items: List<CartItem>,
    val total: Double,
    val shippingAddress: String,
    val paymentMethod: PaymentMethod,
    val status: OrderStatus,
    val createdAt: java.util.Date
)

enum class PaymentMethod {
    DEBIT_CARD,
    CREDIT_CARD
}

enum class OrderStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    CANCELLED
}
