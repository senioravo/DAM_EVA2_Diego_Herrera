package cl.duoc.app.data.model

import java.time.LocalDateTime
import java.time.Duration

data class PlantelPlant(
    val product: Product,
    val addedDate: LocalDateTime = LocalDateTime.now(),
    val assistanceStarted: Boolean = false,
    val lastWateredDate: LocalDateTime? = null,
    val customTitle: String? = null,
    val notificationsEnabled: Boolean = false,
    val wateringHistory: List<LocalDateTime> = emptyList()
) {
    fun getCurrentState(): PlantState {
        if (!assistanceStarted) return PlantState.NOT_STARTED
        if (lastWateredDate == null) return PlantState.NOT_STARTED
        
        val daysSinceWatering = Duration.between(lastWateredDate, LocalDateTime.now()).toDays()
        val wateringCycleDays = product.wateringCycleDays
        
        return when {
            daysSinceWatering >= wateringCycleDays + 2 -> PlantState.URGENT_CARE
            daysSinceWatering >= wateringCycleDays -> PlantState.NEEDS_WATER
            else -> PlantState.HEALTHY
        }
    }
    
    fun getStateText(): String {
        return when (getCurrentState()) {
            PlantState.NOT_STARTED -> "Sin asistencia"
            PlantState.HEALTHY -> "Saludable"
            PlantState.NEEDS_WATER -> "Necesita agua"
            PlantState.URGENT_CARE -> "Atención urgente"
        }
    }
}

enum class PlantState {
    NOT_STARTED,
    HEALTHY,
    NEEDS_WATER,
    URGENT_CARE
}
