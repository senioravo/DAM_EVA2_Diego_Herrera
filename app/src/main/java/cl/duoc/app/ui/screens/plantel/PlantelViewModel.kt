package cl.duoc.app.ui.screens.plantel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.app.data.repository.PlantelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlantelViewModel(
    private val plantelRepository: PlantelRepository = PlantelRepository.getInstance()
) : ViewModel() {
    
    private val _estado = MutableStateFlow(PlantelUIState())
    val estado: StateFlow<PlantelUIState> = _estado.asStateFlow()
    
    init {
        observePlantelPlants()
    }
    
    private fun observePlantelPlants() {
        viewModelScope.launch {
            plantelRepository.plantelPlants.collect { plants ->
                _estado.value = _estado.value.copy(
                    plants = plants,
                    isLoading = false
                )
            }
        }
    }
    
    fun startAssistance(productId: Int) {
        viewModelScope.launch {
            plantelRepository.startAssistance(productId)
        }
    }
    
    fun waterPlant(productId: Int) {
        viewModelScope.launch {
            plantelRepository.waterPlant(productId)
        }
    }
    
    fun removePlant(productId: Int) {
        viewModelScope.launch {
            plantelRepository.removePlantFromPlantel(productId)
        }
    }
    
    fun updateCustomTitle(productId: Int, newTitle: String) {
        viewModelScope.launch {
            plantelRepository.updateCustomTitle(productId, newTitle)
        }
    }
    
    fun toggleNotifications(productId: Int) {
        viewModelScope.launch {
            plantelRepository.toggleNotifications(productId)
        }
    }
}
