package cl.duoc.app.data.repository

import android.util.Log
import cl.duoc.app.data.api.RetrofitClient
import cl.duoc.app.data.api.dto.*
import cl.duoc.app.data.model.PlantelPlant
import cl.duoc.app.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PlantelRepositoryAPI {
    
    private val api = RetrofitClient.plantelApi
    private val TAG = "PlantelRepositoryAPI"
    
    private val _plantelPlants = MutableStateFlow<List<PlantelPlant>>(emptyList())
    val plantelPlants: StateFlow<List<PlantelPlant>> = _plantelPlants.asStateFlow()
    
    // Cargar plantas del plantel desde el backend
    suspend fun loadPlantelForUser(userId: Int): Result<List<PlantelPlant>> {
        return try {
            val response = api.getPlantelByUserId(userId)
            if (response.isSuccessful && response.body() != null) {
                val plants = response.body()!!.map { it.toPlantelPlant() }
                _plantelPlants.value = plants
                Log.d(TAG, "Plantel cargado: ${plants.size} plantas")
                Result.success(plants)
            } else {
                Log.e(TAG, "Error al cargar plantel: ${response.code()}")
                Result.failure(Exception("Error al cargar plantel"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al cargar plantel", e)
            Result.failure(e)
        }
    }
    
    // Agregar planta al plantel
    suspend fun addPlantToPlantel(userId: Int, productId: Int): Result<PlantelPlant> {
        return try {
            val request = AddPlantRequest(userId, productId)
            val response = api.addPlantToPlantel(request)
            
            if (response.isSuccessful && response.body() != null) {
                val newPlant = response.body()!!.toPlantelPlant()
                
                // Actualizar estado local
                val currentPlants = _plantelPlants.value.toMutableList()
                currentPlants.add(newPlant)
                _plantelPlants.value = currentPlants
                
                Log.d(TAG, "Planta agregada al plantel: ${newPlant.product.name}")
                Result.success(newPlant)
            } else {
                Log.e(TAG, "Error al agregar planta: ${response.code()}")
                Result.failure(Exception("No se pudo agregar la planta"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al agregar planta", e)
            Result.failure(e)
        }
    }
    
    // Regar planta
    suspend fun waterPlant(plantelId: Int): Result<PlantelPlant> {
        return try {
            val response = api.waterPlant(plantelId)
            
            if (response.isSuccessful && response.body() != null) {
                val updatedPlant = response.body()!!.toPlantelPlant()
                
                // Actualizar estado local
                updateLocalPlant(plantelId, updatedPlant)
                
                Log.d(TAG, "Planta regada exitosamente")
                Result.success(updatedPlant)
            } else {
                Result.failure(Exception("Error al regar planta"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al regar planta", e)
            Result.failure(e)
        }
    }
    
    // Actualizar título personalizado
    suspend fun updateCustomTitle(plantelId: Int, newTitle: String): Result<PlantelPlant> {
        return try {
            val request = UpdateTitleRequest(newTitle)
            val response = api.updateCustomTitle(plantelId, request)
            
            if (response.isSuccessful && response.body() != null) {
                val updatedPlant = response.body()!!.toPlantelPlant()
                updateLocalPlant(plantelId, updatedPlant)
                Result.success(updatedPlant)
            } else {
                Result.failure(Exception("Error al actualizar título"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar título", e)
            Result.failure(e)
        }
    }
    
    // Actualizar notas (no soportado en el modelo actual - eliminar o comentar)
    // La clase PlantelPlant no tiene propiedad 'notes', solo 'customTitle'
    /*
    suspend fun updateNotes(plantelId: Int, notes: String): Result<PlantelPlant> {
        return try {
            val request = UpdateNotesRequest(notes)
            val response = api.updateNotes(plantelId, request)
            
            if (response.isSuccessful && response.body() != null) {
                val updatedPlant = response.body()!!.toPlantelPlant()
                updateLocalPlant(plantelId, updatedPlant)
                Result.success(updatedPlant)
            } else {
                Result.failure(Exception("Error al actualizar notas"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar notas", e)
            Result.failure(e)
        }
    }
    */
    
    // Toggle notificaciones
    suspend fun toggleNotifications(plantelId: Int): Result<PlantelPlant> {
        return try {
            val response = api.toggleNotifications(plantelId)
            
            if (response.isSuccessful && response.body() != null) {
                val updatedPlant = response.body()!!.toPlantelPlant()
                updateLocalPlant(plantelId, updatedPlant)
                Result.success(updatedPlant)
            } else {
                Result.failure(Exception("Error al cambiar notificaciones"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al toggle notificaciones", e)
            Result.failure(e)
        }
    }
    
    // Remover planta del plantel
    suspend fun removePlant(plantelId: Int): Result<Unit> {
        return try {
            val response = api.removePlant(plantelId)
            
            if (response.isSuccessful) {
                // Actualizar estado local
                _plantelPlants.value = _plantelPlants.value.filter { 
                    it.plantelId != plantelId 
                }
                Log.d(TAG, "Planta removida del plantel")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al remover planta"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al remover planta", e)
            Result.failure(e)
        }
    }
    
    fun isPlantInPlantel(productId: Int): Boolean {
        return _plantelPlants.value.any { it.product.id == productId }
    }
    
    // Helpers
    private fun updateLocalPlant(plantelId: Int, updatedPlant: PlantelPlant) {
        val currentPlants = _plantelPlants.value.toMutableList()
        val index = currentPlants.indexOfFirst { it.plantelId == plantelId }
        if (index != -1) {
            currentPlants[index] = updatedPlant
            _plantelPlants.value = currentPlants
        }
    }
    
    // Mapper: PlantelPlantDTO -> PlantelPlant
    private fun PlantelPlantDTO.toPlantelPlant(): PlantelPlant {
        val lastWatered = if (lastWateredDate != null) {
            try {
                LocalDateTime.parse(lastWateredDate, DateTimeFormatter.ISO_DATE_TIME)
            } catch (e: Exception) {
                null
            }
        } else null
        
        return PlantelPlant(
            plantelId = id,  // Guardar el ID del plantel
            product = Product(
                id = producto.id,
                name = producto.nombre,
                description = producto.descripcion ?: "",
                price = producto.precio,
                category = producto.categoria?.nombre ?: "",
                imageUrl = producto.imagenUrl ?: "",
                stock = producto.stock,
                rating = producto.rating ?: 0f,
                wateringCycleDays = wateringFrequencyDays
            ),
            addedDate = try {
                LocalDateTime.parse(addedAt, DateTimeFormatter.ISO_DATE_TIME)
            } catch (e: Exception) {
                LocalDateTime.now()
            },
            customTitle = customTitle,
            lastWateredDate = lastWatered,
            notificationsEnabled = notificationsEnabled,
            assistanceStarted = lastWatered != null,
            wateringHistory = if (lastWatered != null) listOf(lastWatered) else emptyList()
        )
    }
    
    companion object {
        @Volatile
        private var instance: PlantelRepositoryAPI? = null
        
        fun getInstance(): PlantelRepositoryAPI {
            return instance ?: synchronized(this) {
                instance ?: PlantelRepositoryAPI().also { instance = it }
            }
        }
    }
}
