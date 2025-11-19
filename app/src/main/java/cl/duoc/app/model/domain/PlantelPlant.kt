package cl.duoc.app.model.domain

data class PlantelPlant(
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
