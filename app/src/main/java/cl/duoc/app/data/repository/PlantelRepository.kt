package cl.duoc.app.data.repository

import cl.duoc.app.data.model.PlantelPlant
import cl.duoc.app.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

object PlantelRepository {
    
    private val _plants = MutableStateFlow<List<PlantelPlant>>(emptyList())
    val plants: StateFlow<List<PlantelPlant>> = _plants.asStateFlow()
    
    fun addPlant(product: Product) {
        val existingPlant = _plants.value.find { it.product.id == product.id }
        if (existingPlant == null) {
            val newPlant = PlantelPlant(product = product)
            _plants.value = _plants.value + newPlant
        }
    }
    
    fun removePlant(productId: Int) {
        _plants.value = _plants.value.filter { it.product.id != productId }
    }
    
    fun startAssistance(productId: Int) {
        _plants.value = _plants.value.map { plant ->
            if (plant.product.id == productId) {
                plant.copy(
                    assistanceStarted = true,
                    lastWateredDate = LocalDateTime.now(),
                    wateringHistory = listOf(LocalDateTime.now())
                )
            } else {
                plant
            }
        }
    }
    
    fun waterPlant(productId: Int) {
        _plants.value = _plants.value.map { plant ->
            if (plant.product.id == productId) {
                plant.copy(
                    lastWateredDate = LocalDateTime.now(),
                    wateringHistory = plant.wateringHistory + LocalDateTime.now()
                )
            } else {
                plant
            }
        }
    }
    
    fun updateCustomTitle(productId: Int, title: String) {
        _plants.value = _plants.value.map { plant ->
            if (plant.product.id == productId) {
                plant.copy(customTitle = title.ifBlank { null })
            } else {
                plant
            }
        }
    }
    
    fun toggleNotifications(productId: Int) {
        _plants.value = _plants.value.map { plant ->
            if (plant.product.id == productId) {
                plant.copy(notificationsEnabled = !plant.notificationsEnabled)
            } else {
                plant
            }
        }
    }
    
    fun getPlantById(productId: Int): PlantelPlant? {
        return _plants.value.find { it.product.id == productId }
    }
    
    fun isInPlantel(productId: Int): Boolean {
        return _plants.value.any { it.product.id == productId }
    }
}
