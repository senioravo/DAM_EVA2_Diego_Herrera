package cl.duoc.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.app.data.model.*
import cl.duoc.app.data.preferences.UserPreferences
import cl.duoc.app.data.repository.CartRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CartState(
    val items: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val itemCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val orderCompleted: Order? = null
)

class CartViewModel(
    private val context: Context
) : ViewModel() {
    
    private val cartRepository = CartRepository.getInstance()
    private val userPreferences = UserPreferences(context)
    
    private val _cartState = MutableStateFlow(CartState())
    val cartState: StateFlow<CartState> = _cartState.asStateFlow()
    
    init {
        observeCart()
    }
    
    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.cartItems.collect { items ->
                _cartState.value = _cartState.value.copy(
                    items = items,
                    total = cartRepository.cartTotal,
                    itemCount = cartRepository.cartItemCount
                )
            }
        }
    }
    
    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            try {
                cartRepository.addToCart(product, quantity)
            } catch (e: Exception) {
                _cartState.value = _cartState.value.copy(
                    error = "Error al agregar producto: ${e.message}"
                )
            }
        }
    }
    
    fun removeFromCart(productId: Int) {
        viewModelScope.launch {
            try {
                cartRepository.removeFromCart(productId)
            } catch (e: Exception) {
                _cartState.value = _cartState.value.copy(
                    error = "Error al eliminar producto: ${e.message}"
                )
            }
        }
    }
    
    fun updateQuantity(productId: Int, quantity: Int) {
        viewModelScope.launch {
            try {
                cartRepository.updateQuantity(productId, quantity)
            } catch (e: Exception) {
                _cartState.value = _cartState.value.copy(
                    error = "Error al actualizar cantidad: ${e.message}"
                )
            }
        }
    }
    
    fun clearCart() {
        viewModelScope.launch {
            cartRepository.clearCart()
        }
    }
    
    fun processOrder(shippingAddress: String, paymentMethod: PaymentMethod) {
        viewModelScope.launch {
            _cartState.value = _cartState.value.copy(isLoading = true, error = null)
            
            try {
                val userId = userPreferences.currentUserId.first() ?: throw Exception("Usuario no autenticado")
                
                // Simular procesamiento de pago
                kotlinx.coroutines.delay(2000)
                
                val order = cartRepository.createOrder(userId, shippingAddress, paymentMethod)
                
                _cartState.value = _cartState.value.copy(
                    isLoading = false,
                    orderCompleted = order
                )
            } catch (e: Exception) {
                _cartState.value = _cartState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al procesar orden"
                )
            }
        }
    }
    
    fun clearError() {
        _cartState.value = _cartState.value.copy(error = null)
    }
    
    fun clearOrderCompleted() {
        _cartState.value = _cartState.value.copy(orderCompleted = null)
    }
}
