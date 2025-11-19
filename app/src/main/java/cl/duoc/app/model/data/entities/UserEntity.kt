package cl.duoc.app.model.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val email: String,
    val password: String,
    val profileImageUrl: String? = null,
    val createdAt: Long = Date().time,
    val isAdmin: Boolean = false
)
