package cl.duoc.app.model.data.repositories

import cl.duoc.app.model.data.data.PlantelPlantDao
import cl.duoc.app.model.data.data.ProductDao
import cl.duoc.app.model.data.entities.PlantelPlantEntity
import cl.duoc.app.model.domain.PlantelPlant
import cl.duoc.app.model.domain.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class PlantelRepository(
    private val plantelPlantDao: PlantelPlantDao,
    private val productDao: ProductDao
) {
    
    private val gson = Gson()
    
    // Conversiones entre Entity y Domain
    private suspend fun PlantelPlantEntity.toDomain(): PlantelPlant? {
        val product = productDao.getProductById(productId) ?: return null
        
        val wateringHistoryList: List<Long> = if (wateringHistory.isNotEmpty()) {
            try {
                val type = object : TypeToken<List<Long>>() {}.type
                gson.fromJson(wateringHistory, type)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
        
        return PlantelPlant(
            id = id,
            product = Product(
                id = product.id,
                name = product.name,
                description = product.description,
                price = product.price,
                category = product.category,
                imageUrl = product.imageUrl,
                stock = product.stock,
                rating = product.rating,
                wateringCycleDays = product.wateringCycleDays
            ),
            userId = userId,
            addedDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(addedDate), ZoneId.systemDefault()),
            lastWateredDate = lastWateredDate?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
            },
            assistanceStarted = assistanceStarted,
            customTitle = customTitle,
            wateringHistory = wateringHistoryList.map {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
            },
            notificationsEnabled = notificationsEnabled
        )
    }
    
    private fun PlantelPlant.toEntity(): PlantelPlantEntity {
        val wateringHistoryJson = gson.toJson(
            wateringHistory.map { it.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() }
        )
        
        return PlantelPlantEntity(
            id = id,
            productId = product.id,
            userId = userId,
            addedDate = addedDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            lastWateredDate = lastWateredDate?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
            assistanceStarted = assistanceStarted,
            customTitle = customTitle,
            wateringHistory = wateringHistoryJson,
            notificationsEnabled = notificationsEnabled
        )
    }
    
    // Operaciones
    fun getUserPlants(userId: Int): Flow<List<PlantelPlant>> {
        return plantelPlantDao.getUserPlants(userId).map { entities ->
            entities.mapNotNull { it.toDomain() }
        }
    }
    
    suspend fun getPlantById(plantId: Int): PlantelPlant? {
        return plantelPlantDao.getPlantById(plantId)?.toDomain()
    }
    
    suspend fun addPlantToUser(userId: Int, product: Product): Long {
        val plant = PlantelPlant(
            product = product,
            userId = userId,
            addedDate = LocalDateTime.now(),
            assistanceStarted = false
        )
        return plantelPlantDao.insertPlant(plant.toEntity())
    }
    
    suspend fun updatePlant(plant: PlantelPlant) {
        plantelPlantDao.updatePlant(plant.toEntity())
    }
    
    suspend fun deletePlant(plant: PlantelPlant) {
        plantelPlantDao.deletePlant(plant.toEntity())
    }
    
    suspend fun waterPlant(plantId: Int) {
        val plant = getPlantById(plantId) ?: return
        val now = LocalDateTime.now()
        val updatedPlant = plant.copy(
            lastWateredDate = now,
            wateringHistory = plant.wateringHistory + now
        )
        updatePlant(updatedPlant)
    }
    
    suspend fun startAssistance(plantId: Int) {
        val plant = getPlantById(plantId) ?: return
        val updatedPlant = plant.copy(assistanceStarted = true)
        updatePlant(updatedPlant)
    }
}
