package cl.duoc.app.data.repository

import cl.duoc.app.data.model.Purchase
import cl.duoc.app.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

class UserRepository {
    
    // Base de datos en memoria (simulación)
    private val _users = MutableStateFlow<List<User>>(listOf(
        User(
            id = 1,
            email = "admin@plantbuddy.com",
            password = "admin123",
            profileImageUrl = null,
            createdAt = LocalDateTime.now(),
            isAdmin = true
        )
    ))
    val users: StateFlow<List<User>> = _users.asStateFlow()
    
    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    val purchases: StateFlow<List<Purchase>> = _purchases.asStateFlow()
    
    private var nextUserId = 2
    private var nextPurchaseId = 1
    
    // Autenticación
    fun login(email: String, password: String): User? {
        return _users.value.find { 
            it.email.equals(email, ignoreCase = true) && it.password == password 
        }
    }
    
    fun register(email: String, password: String): Result<User> {
        // Validaciones
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Email y contraseña son requeridos"))
        }
        
        if (!email.contains("@")) {
            return Result.failure(Exception("Email inválido"))
        }
        
        if (password.length < 6) {
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
        }
        
        if (_users.value.any { it.email.equals(email, ignoreCase = true) }) {
            return Result.failure(Exception("El email ya está registrado"))
        }
        
        val newUser = User(
            id = nextUserId++,
            email = email,
            password = password,
            profileImageUrl = null,
            createdAt = LocalDateTime.now(),
            isAdmin = false
        )
        
        _users.value = _users.value + newUser
        return Result.success(newUser)
    }
    
    fun getUserById(userId: Int): User? {
        return _users.value.find { it.id == userId }
    }
    
    fun updateProfileImage(userId: Int, imageUrl: String): Boolean {
        val user = _users.value.find { it.id == userId } ?: return false
        val updatedUser = user.copy(profileImageUrl = imageUrl)
        _users.value = _users.value.map { if (it.id == userId) updatedUser else it }
        return true
    }
    
    fun updatePassword(userId: Int, currentPassword: String, newPassword: String): Result<Unit> {
        val user = _users.value.find { it.id == userId } 
            ?: return Result.failure(Exception("Usuario no encontrado"))
        
        if (user.password != currentPassword) {
            return Result.failure(Exception("Contraseña actual incorrecta"))
        }
        
        if (newPassword.length < 6) {
            return Result.failure(Exception("La nueva contraseña debe tener al menos 6 caracteres"))
        }
        
        val updatedUser = user.copy(password = newPassword)
        _users.value = _users.value.map { if (it.id == userId) updatedUser else it }
        return Result.success(Unit)
    }
    
    // Compras
    fun getPurchasesByUserId(userId: Int): List<Purchase> {
        return _purchases.value.filter { it.userId == userId }
            .sortedByDescending { it.purchaseDate }
    }
    
    fun addPurchase(purchase: Purchase): Purchase {
        val newPurchase = purchase.copy(id = nextPurchaseId++)
        _purchases.value = _purchases.value + newPurchase
        return newPurchase
    }
    
    fun getPurchaseHistory(userId: Int): List<Purchase> {
        return _purchases.value
            .filter { it.userId == userId }
            .sortedByDescending { it.purchaseDate }
    }
}
