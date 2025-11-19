package cl.duoc.app.data.repository

import cl.duoc.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

class CartRepository private constructor() {
    
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()
    
    private val orders = mutableListOf<Order>()
    private var nextOrderId = 1
    
    val cartTotal: Double
        get() = _cartItems.value.sumOf { it.subtotal }
    
    val cartItemCount: Int
        get() = _cartItems.value.sumOf { it.quantity }
    
    fun addToCart(product: Product, quantity: Int = 1) {
        val currentItems = _cartItems.value.toMutableList()
        val existingItemIndex = currentItems.indexOfFirst { it.product.id == product.id }
        
        if (existingItemIndex != -1) {
            // Si el producto ya existe, actualizar cantidad
            val existingItem = currentItems[existingItemIndex]
            currentItems[existingItemIndex] = existingItem.copy(
                quantity = existingItem.quantity + quantity
            )
        } else {
            // Agregar nuevo producto
            currentItems.add(CartItem(product, quantity))
        }
        
        _cartItems.value = currentItems
    }
    
    fun removeFromCart(productId: Int) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }
    
    fun updateQuantity(productId: Int, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }
        
        val currentItems = _cartItems.value.toMutableList()
        val itemIndex = currentItems.indexOfFirst { it.product.id == productId }
        
        if (itemIndex != -1) {
            currentItems[itemIndex] = currentItems[itemIndex].copy(quantity = quantity)
            _cartItems.value = currentItems
        }
    }
    
    fun clearCart() {
        _cartItems.value = emptyList()
    }
    
    fun createOrder(
        userId: Int,
        shippingAddress: String,
        paymentMethod: PaymentMethod
    ): Order {
        val order = Order(
            id = nextOrderId++,
            userId = userId,
            items = _cartItems.value,
            total = cartTotal,
            shippingAddress = shippingAddress,
            paymentMethod = paymentMethod,
            status = OrderStatus.PENDING,
            createdAt = Date()
        )
        
        orders.add(order)
        clearCart()
        
        return order
    }
    
    fun getOrdersByUser(userId: Int): List<Order> {
        return orders.filter { it.userId == userId }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: CartRepository? = null
        
        fun getInstance(): CartRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CartRepository().also { INSTANCE = it }
            }
        }
    }
}
