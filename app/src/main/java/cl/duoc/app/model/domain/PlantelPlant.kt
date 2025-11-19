package cl.duoc.app.model.domain

import java.time.LocalDateTime

data class PlantelPlant(
    val id: Int = 0,
    val product: Product,
    val userId: Int,
    val addedDate: LocalDateTime = LocalDateTime.now(),
    val lastWateredDate: LocalDateTime? = null,
    val assistanceStarted: Boolean = false,
    val customTitle: String? = null,
    val wateringHistory: List<LocalDateTime> = emptyList(),
    val notificationsEnabled: Boolean = false
) {
    fun getCurrentState(): PlantState {
        if (!assistanceStarted) {
            return PlantState.NOT_STARTED
        }
        
        if (lastWateredDate == null) {
            return PlantState.NEEDS_WATER
        }
        
        val daysSinceLastWatering = java.time.Duration.between(lastWateredDate, LocalDateTime.now()).toDays()
        val wateringCycle = product.wateringCycleDays
        
        return when {
            daysSinceLastWatering >= wateringCycle * 2 -> PlantState.URGENT_CARE
            daysSinceLastWatering >= wateringCycle -> PlantState.NEEDS_WATER
            else -> PlantState.HEALTHY
        }
    }
    
    fun getStateText(): String {
        return when (getCurrentState()) {
            PlantState.NOT_STARTED -> "No iniciado"
            PlantState.HEALTHY -> "Saludable"
            PlantState.NEEDS_WATER -> "Necesita agua"
            PlantState.URGENT_CARE -> "Atender Urgente"
        }
    }
}

enum class PlantState {
    NOT_STARTED,
    HEALTHY,
    NEEDS_WATER,
    URGENT_CARE
}
