package cl.duoc.app.model.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plantel_plants")
data class PlantelPlantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productId: Int,
    val userId: Int,
    val addedDate: Long,
    val lastWateredDate: Long? = null,
    val assistanceStarted: Boolean = false,
    val customTitle: String? = null,
    val wateringHistory: String = "", // JSON string of timestamps
    val notificationsEnabled: Boolean = false
)
