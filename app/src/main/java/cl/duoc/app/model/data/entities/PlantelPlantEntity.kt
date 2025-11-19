package cl.duoc.app.model.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "plantel_plants",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class PlantelPlantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val productId: Int,
    val plantName: String,
    val plantDescription: String,
    val plantImageUrl: String,
    val addedAt: Long = System.currentTimeMillis(),
    val lastWateredDate: Long? = null,
    val wateringFrequencyDays: Int = 7,
    val notes: String? = null
)
