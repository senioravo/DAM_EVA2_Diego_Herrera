package cl.duoc.app.model.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val totalPrice: Double,
    val purchaseDate: Long,
    val status: String // PENDING, COMPLETED, CANCELLED
)
