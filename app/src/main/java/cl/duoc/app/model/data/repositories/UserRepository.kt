package cl.duoc.app.model.data.repositories

import cl.duoc.app.model.data.dao.UserDao
import cl.duoc.app.model.data.entities.UserEntity
import cl.duoc.app.model.domain.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(private val userDao: UserDao) {

    fun getUserById(userId: Int): Flow<User?> {
        return userDao.getUserById(userId).map { it?.toDomain() }
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)?.toDomain()
    }

    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)?.toDomain()
    }

    suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user.toEntity())
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user.toEntity())
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user.toEntity())
    }

    suspend fun emailExists(email: String): Boolean {
        return userDao.emailExists(email) > 0
    }

    suspend fun usernameExists(username: String): Boolean {
        return userDao.usernameExists(username) > 0
    }

    // Conversiones Entity ↔ Domain
    private fun UserEntity.toDomain() = User(
        id = id,
        username = username,
        email = email,
        password = password,
        profileImageUrl = profileImageUrl,
        createdAt = createdAt
    )

    private fun User.toEntity() = UserEntity(
        id = id,
        username = username,
        email = email,
        password = password,
        profileImageUrl = profileImageUrl,
        createdAt = createdAt
    )
}
