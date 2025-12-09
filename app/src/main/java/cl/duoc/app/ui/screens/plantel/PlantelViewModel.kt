package cl.duoc.app.ui.screens.plantel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.app.data.repository.PlantelRepository
import cl.duoc.app.data.repository.PlantelRepositoryAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlantelViewModel(
    private val userId: Int? = null, // Opcional para compatibilidad
    private val useAPI: Boolean = false // Flag para usar API o local
) : ViewModel() {
    
    private val plantelRepository: PlantelRepository = PlantelRepository.getInstance()
    private val plantelRepositoryAPI: PlantelRepositoryAPI? = if (useAPI) PlantelRepositoryAPI.getInstance() else null
    
    private val _estado = MutableStateFlow(PlantelUIState())
    val estado: StateFlow<PlantelUIState> = _estado.asStateFlow()
    
    init {
        if (useAPI && userId != null) {
            loadPlantelFromAPI()
        }
        observePlantelPlants()
    }
    
    private fun loadPlantelFromAPI() {
        userId?.let { uid ->
            viewModelScope.launch {
                _estado.value = _estado.value.copy(isLoading = true)
                val result = plantelRepositoryAPI?.loadPlantelForUser(uid)
                result?.onFailure { error ->
                    _estado.value = _estado.value.copy(
                        isLoading = false,
                        error = "Error al cargar plantel: ${error.message}"
                    )
                }
            }
        }
    }
    
    private fun observePlantelPlants() {
        viewModelScope.launch {
            if (useAPI && plantelRepositoryAPI != null) {
                plantelRepositoryAPI.plantelPlants.collect { plants ->
                    _estado.value = _estado.value.copy(
                        plants = plants,
                        isLoading = false
                    )
                }
            } else {
                plantelRepository.plantelPlants.collect { plants ->
                    _estado.value = _estado.value.copy(
                        plants = plants,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun startAssistance(productId: Int) {
        viewModelScope.launch {
            if (useAPI) {
                // En API, solo necesitamos regar la planta para iniciar asistencia
                // El backend maneja la lógica de "primera vez"
            } else {
                plantelRepository.startAssistance(productId)
            }
        }
    }
    
    fun waterPlant(plantelId: Int) {
        viewModelScope.launch {
            if (useAPI) {
                plantelRepositoryAPI?.waterPlant(plantelId)
            } else {
                plantelRepository.waterPlant(plantelId)
            }
        }
    }
    
    fun removePlant(plantelId: Int) {
        viewModelScope.launch {
            if (useAPI) {
                plantelRepositoryAPI?.removePlant(plantelId)
            } else {
                plantelRepository.removePlantFromPlantel(plantelId)
            }
        }
    }
    
    fun updateCustomTitle(plantelId: Int, newTitle: String) {
        viewModelScope.launch {
            if (useAPI) {
                plantelRepositoryAPI?.updateCustomTitle(plantelId, newTitle)
            } else {
                plantelRepository.updateCustomTitle(plantelId, newTitle)
            }
        }
    }
    
    fun toggleNotifications(plantelId: Int) {
        viewModelScope.launch {
            if (useAPI) {
                plantelRepositoryAPI?.toggleNotifications(plantelId)
            } else {
                plantelRepository.toggleNotifications(plantelId)
            }
        }
    }
    
    fun addPlantToPlantel(productId: Int) {
        viewModelScope.launch {
            if (useAPI && userId != null) {
                plantelRepositoryAPI?.addPlantToPlantel(userId, productId)
            }
        }
    }
    
    fun retry() {
        if (useAPI && userId != null) {
            loadPlantelFromAPI()
        }
    }
}
