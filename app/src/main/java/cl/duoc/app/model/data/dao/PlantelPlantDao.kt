package cl.duoc.app.model.data.dao

import androidx.room.*
import cl.duoc.app.model.data.entities.PlantelPlantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantelPlantDao {
    @Query("SELECT * FROM plantel_plants WHERE userId = :userId ORDER BY addedAt DESC")
    fun getUserPlants(userId: Int): Flow<List<PlantelPlantEntity>>

    @Query("SELECT * FROM plantel_plants WHERE id = :plantId")
    fun getPlantById(plantId: Int): Flow<PlantelPlantEntity?>

    @Query("SELECT * FROM plantel_plants WHERE userId = :userId AND productId = :productId LIMIT 1")
    suspend fun getPlantByUserAndProduct(userId: Int, productId: Int): PlantelPlantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: PlantelPlantEntity): Long

    @Update
    suspend fun updatePlant(plant: PlantelPlantEntity)

    @Delete
    suspend fun deletePlant(plant: PlantelPlantEntity)

    @Query("UPDATE plantel_plants SET lastWateredDate = :date WHERE id = :plantId")
    suspend fun updateLastWatered(plantId: Int, date: Long)

    @Query("DELETE FROM plantel_plants WHERE userId = :userId")
    suspend fun deleteAllUserPlants(userId: Int)
}
