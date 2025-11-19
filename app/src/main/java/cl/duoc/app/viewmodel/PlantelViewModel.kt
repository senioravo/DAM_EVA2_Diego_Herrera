package cl.duoc.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.app.model.data.config.AppDatabase
import cl.duoc.app.model.data.repositories.PlantelRepository
import cl.duoc.app.model.domain.PlantelPlant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlantelUIState(
    val plants: List<PlantelPlant> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class PlantelViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val plantelRepository = PlantelRepository(
        database.plantelPlantDao(),
        database.productDao()
    )
    
    private val _estado = MutableStateFlow(PlantelUIState())
    val estado: StateFlow<PlantelUIState> = _estado.asStateFlow()
    
    fun observePlantelPlants(userId: Int) {
        viewModelScope.launch {
            plantelRepository.getUserPlants(userId).collect { plants ->
                _estado.value = _estado.value.copy(
                    plants = plants,
                    isLoading = false
                )
            }
        }
    }
    
    fun startAssistance(plantId: Int) {
        viewModelScope.launch {
            try {
                plantelRepository.startAssistance(plantId)
            } catch (e: Exception) {
                _estado.value = _estado.value.copy(error = e.message)
            }
        }
    }
    
    fun waterPlant(plantId: Int) {
        viewModelScope.launch {
            try {
                plantelRepository.waterPlant(plantId)
            } catch (e: Exception) {
                _estado.value = _estado.value.copy(error = e.message)
            }
        }
    }
    
    fun removePlant(plantId: Int) {
        viewModelScope.launch {
            try {
                val plant = plantelRepository.getPlantById(plantId)
                plant?.let {
                    plantelRepository.deletePlant(it)
                }
            } catch (e: Exception) {
                _estado.value = _estado.value.copy(error = e.message)
            }
        }
    }
    
    fun updateCustomTitle(plantId: Int, newTitle: String) {
        viewModelScope.launch {
            try {
                val plant = plantelRepository.getPlantById(plantId)
                plant?.let {
                    val updatedPlant = it.copy(customTitle = newTitle)
                    plantelRepository.updatePlant(updatedPlant)
                }
            } catch (e: Exception) {
                _estado.value = _estado.value.copy(error = e.message)
            }
        }
    }
    
    fun toggleNotifications(plantId: Int) {
        viewModelScope.launch {
            try {
                val plant = plantelRepository.getPlantById(plantId)
                plant?.let {
                    val updatedPlant = it.copy(notificationsEnabled = !it.notificationsEnabled)
                    plantelRepository.updatePlant(updatedPlant)
                }
            } catch (e: Exception) {
                _estado.value = _estado.value.copy(error = e.message)
            }
        }
    }
}
