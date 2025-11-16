package cl.duoc.app.ui.screens.plantel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.app.data.model.PlantelPlant
import cl.duoc.app.data.repository.PlantelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlantelState(
    val plants: List<PlantelPlant> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class PlantelViewModel : ViewModel() {
    
    private val repository = PlantelRepository
    
    private val _estado = MutableStateFlow(PlantelState())
    val estado: StateFlow<PlantelState> = _estado.asStateFlow()
    
    init {
        viewModelScope.launch {
            repository.plants.collect { plants ->
                _estado.value = _estado.value.copy(plants = plants)
            }
        }
    }
    
    fun startAssistance(productId: Int) {
        viewModelScope.launch {
            repository.startAssistance(productId)
        }
    }
    
    fun waterPlant(productId: Int) {
        viewModelScope.launch {
            repository.waterPlant(productId)
        }
    }
    
    fun removePlant(productId: Int) {
        viewModelScope.launch {
            repository.removePlant(productId)
        }
    }
    
    fun updateCustomTitle(productId: Int, newTitle: String) {
        viewModelScope.launch {
            repository.updateCustomTitle(productId, newTitle)
        }
    }
    
    fun toggleNotifications(productId: Int) {
        viewModelScope.launch {
            repository.toggleNotifications(productId)
        }
    }
    
}
