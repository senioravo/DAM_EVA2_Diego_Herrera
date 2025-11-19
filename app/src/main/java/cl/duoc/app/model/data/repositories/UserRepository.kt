package cl.duoc.app.model.data.repositories

import cl.duoc.app.model.data.data.UserDao
import cl.duoc.app.model.data.entities.UserEntity
import cl.duoc.app.model.domain.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date

class UserRepository(private val userDao: UserDao) {
    
    // Conversiones entre Entity y Domain
    private fun UserEntity.toDomain() = User(
        id = id,
        email = email,
        password = password,
        profileImageUrl = profileImageUrl,
        createdAt = Date(createdAt),
        isAdmin = isAdmin
    )
    
    private fun User.toEntity() = UserEntity(
        id = id,
        email = email,
        password = password,
        profileImageUrl = profileImageUrl,
        createdAt = createdAt.time,
        isAdmin = isAdmin
    )
    
    // Operaciones
    fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    suspend fun getUserById(userId: Int): User? {
        return userDao.getUserById(userId)?.toDomain()
    }
    
    suspend fun login(email: String, password: String): User? {
        return userDao.login(email, password)?.toDomain()
    }
    
    suspend fun register(email: String, password: String): Result<User> {
        // Validar que el email no exista
        val existingUser = userDao.getUserByEmail(email)
        if (existingUser != null) {
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
        
        val newUser = UserEntity(
            email = email,
            password = password,
            createdAt = Date().time,
            isAdmin = false
        )
        
        val userId = userDao.insertUser(newUser)
        val insertedUser = userDao.getUserById(userId.toInt())
        
        return if (insertedUser != null) {
            Result.success(insertedUser.toDomain())
        } else {
            Result.failure(Exception("Error al crear usuario"))
        }
    }
    
    suspend fun updateUser(user: User) {
        userDao.updateUser(user.toEntity())
    }
    
    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user.toEntity())
    }
}
