package cl.duoc.app.data.repository

import cl.duoc.app.data.model.Purchase
import cl.duoc.app.data.model.PurchaseStatus
import cl.duoc.app.data.model.User
import java.util.Date

class UserRepository private constructor() {
    
    private val users = mutableListOf<User>()
    private val purchases = mutableMapOf<Int, MutableList<Purchase>>() // userId to purchases
    private var nextUserId = 2
    private var nextPurchaseId = 1
    
    init {
        android.util.Log.d("UserRepository", "Inicializando UserRepository (Singleton)")
        // Crear cuenta de Admin pre-definida
        users.add(
            User(
                id = 1,
                email = "admin@plantbuddy.com",
                password = "admin123",
                profileImageUrl = null,
                createdAt = Date(),
                isAdmin = true
            )
        )
        purchases[1] = mutableListOf()
    }
    
    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null
        
        fun getInstance(): UserRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserRepository().also { INSTANCE = it }
            }
        }
    }
    
    // Autenticación
    fun login(email: String, password: String): User? {
        return users.find { it.email == email && it.password == password }
    }
    
    fun register(email: String, password: String): Result<User> {
        // Validar que el email no exista
        if (users.any { it.email == email }) {
            return Result.failure(Exception("El email ya está registrado"))
        }
        
        // Validar formato de email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("Email inválido"))
        }
        
        // Validar contraseña
        if (password.length < 6) {
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
        }
        
        val newUser = User(
            id = nextUserId++,
            email = email,
            password = password,
            createdAt = Date(),
            isAdmin = false
        )
        
        users.add(newUser)
        purchases[newUser.id] = mutableListOf()
        
        return Result.success(newUser)
    }
    
    // Gestión de usuario
    fun getUserById(userId: Int): User? {
        return users.find { it.id == userId }
    }
    
    fun updateProfileImage(userId: Int, imageUrl: String): Boolean {
        android.util.Log.d("UserRepository", "Buscando userId: $userId en lista de ${users.size} usuarios")
        users.forEachIndexed { index, user ->
            android.util.Log.d("UserRepository", "Usuario[$index]: id=${user.id}, email=${user.email}")
        }
        val userIndex = users.indexOfFirst { it.id == userId }
        if (userIndex != -1) {
            users[userIndex] = users[userIndex].copy(profileImageUrl = imageUrl)
            android.util.Log.d("UserRepository", "Usuario actualizado exitosamente: ${users[userIndex]}")
            return true
        }
        android.util.Log.e("UserRepository", "Usuario con id=$userId NO encontrado!")
        return false
    }
    
    fun updatePassword(userId: Int, currentPassword: String, newPassword: String): Result<Boolean> {
        val user = users.find { it.id == userId }
        
        if (user == null) {
            return Result.failure(Exception("Usuario no encontrado"))
        }
        
        if (user.password != currentPassword) {
            return Result.failure(Exception("Contraseña actual incorrecta"))
        }
        
        if (newPassword.length < 6) {
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
        }
        
        val userIndex = users.indexOf(user)
        users[userIndex] = user.copy(password = newPassword)
        
        return Result.success(true)
    }
    
    // Gestión de compras
    fun addPurchase(purchase: Purchase) {
        purchases[purchase.userId]?.add(purchase)
    }
    
    fun getUserPurchases(userId: Int): List<Purchase> {
        return purchases[userId] ?: emptyList()
    }
    
    fun getNextPurchaseId(): Int {
        return nextPurchaseId++
    }
}
