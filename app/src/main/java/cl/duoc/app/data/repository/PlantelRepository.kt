package cl.duoc.app.data.repository

import cl.duoc.app.data.model.PlantelPlant
import cl.duoc.app.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

class PlantelRepository {
    private val _plantelPlants = MutableStateFlow<List<PlantelPlant>>(emptyList())
    val plantelPlants: StateFlow<List<PlantelPlant>> = _plantelPlants.asStateFlow()
    
    fun addPlantToPlantel(product: Product) {
        val currentPlants = _plantelPlants.value.toMutableList()
        
        // Evitar duplicados
        if (currentPlants.any { it.product.id == product.id }) {
            return
        }
        
        val newPlantelPlant = PlantelPlant(
            product = product,
            addedDate = LocalDateTime.now(),
            assistanceStarted = false
        )
        
        currentPlants.add(newPlantelPlant)
        _plantelPlants.value = currentPlants
    }
    
    fun startAssistance(productId: Int) {
        val currentPlants = _plantelPlants.value.toMutableList()
        val index = currentPlants.indexOfFirst { it.product.id == productId }
        
        if (index != -1) {
            val plant = currentPlants[index]
            val now = LocalDateTime.now()
            currentPlants[index] = plant.copy(
                assistanceStarted = true,
                lastWateredDate = now,
                wateringHistory = listOf(now)
            )
            _plantelPlants.value = currentPlants
        }
    }
    
    fun waterPlant(productId: Int) {
        val currentPlants = _plantelPlants.value.toMutableList()
        val index = currentPlants.indexOfFirst { it.product.id == productId }
        
        if (index != -1) {
            val plant = currentPlants[index]
            val now = LocalDateTime.now()
            currentPlants[index] = plant.copy(
                lastWateredDate = now,
                wateringHistory = plant.wateringHistory + now
            )
            _plantelPlants.value = currentPlants
        }
    }
    
    fun removePlantFromPlantel(productId: Int) {
        _plantelPlants.value = _plantelPlants.value.filter { it.product.id != productId }
    }
    
    fun updateCustomTitle(productId: Int, newTitle: String) {
        val currentPlants = _plantelPlants.value.toMutableList()
        val index = currentPlants.indexOfFirst { it.product.id == productId }
        
        if (index != -1) {
            val plant = currentPlants[index]
            currentPlants[index] = plant.copy(
                customTitle = newTitle.ifBlank { null }
            )
            _plantelPlants.value = currentPlants
        }
    }
    
    fun toggleNotifications(productId: Int) {
        val currentPlants = _plantelPlants.value.toMutableList()
        val index = currentPlants.indexOfFirst { it.product.id == productId }
        
        if (index != -1) {
            val plant = currentPlants[index]
            currentPlants[index] = plant.copy(
                notificationsEnabled = !plant.notificationsEnabled
            )
            _plantelPlants.value = currentPlants
        }
    }
    
    fun isPlantInPlantel(productId: Int): Boolean {
        return _plantelPlants.value.any { it.product.id == productId }
    }
    
    companion object {
        @Volatile
        private var instance: PlantelRepository? = null
        
        fun getInstance(): PlantelRepository {
            return instance ?: synchronized(this) {
                instance ?: PlantelRepository().also { instance = it }
            }
        }
    }
}
