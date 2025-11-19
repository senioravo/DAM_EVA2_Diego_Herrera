package cl.duoc.app.model.data.repositories

import cl.duoc.app.model.data.dao.PlantelPlantDao
import cl.duoc.app.model.data.entities.PlantelPlantEntity
import cl.duoc.app.model.domain.PlantelPlant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlantelRepository(private val plantelPlantDao: PlantelPlantDao) {

    fun getUserPlants(userId: Int): Flow<List<PlantelPlant>> {
        return plantelPlantDao.getUserPlants(userId).map { list -> list.map { it.toDomain() } }
    }

    fun getPlantById(plantId: Int): Flow<PlantelPlant?> {
        return plantelPlantDao.getPlantById(plantId).map { it?.toDomain() }
    }

    suspend fun getPlantByUserAndProduct(userId: Int, productId: Int): PlantelPlant? {
        return plantelPlantDao.getPlantByUserAndProduct(userId, productId)?.toDomain()
    }

    suspend fun insertPlant(plant: PlantelPlant): Long {
        return plantelPlantDao.insertPlant(plant.toEntity())
    }

    suspend fun updatePlant(plant: PlantelPlant) {
        plantelPlantDao.updatePlant(plant.toEntity())
    }

    suspend fun deletePlant(plant: PlantelPlant) {
        plantelPlantDao.deletePlant(plant.toEntity())
    }

    suspend fun updateLastWatered(plantId: Int, date: Long) {
        plantelPlantDao.updateLastWatered(plantId, date)
    }

    suspend fun deleteAllUserPlants(userId: Int) {
        plantelPlantDao.deleteAllUserPlants(userId)
    }

    // Conversiones Entity ↔ Domain
    private fun PlantelPlantEntity.toDomain() = PlantelPlant(
        id = id,
        userId = userId,
        productId = productId,
        plantName = plantName,
        plantDescription = plantDescription,
        plantImageUrl = plantImageUrl,
        addedAt = addedAt,
        lastWateredDate = lastWateredDate,
        wateringFrequencyDays = wateringFrequencyDays,
        notes = notes
    )

    private fun PlantelPlant.toEntity() = PlantelPlantEntity(
        id = id,
        userId = userId,
        productId = productId,
        plantName = plantName,
        plantDescription = plantDescription,
        plantImageUrl = plantImageUrl,
        addedAt = addedAt,
        lastWateredDate = lastWateredDate,
        wateringFrequencyDays = wateringFrequencyDays,
        notes = notes
    )
}
