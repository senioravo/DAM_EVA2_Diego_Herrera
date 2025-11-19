package cl.duoc.app.model.data.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cl.duoc.app.model.data.entities.PlantelPlantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantelPlantDao {
    
    @Query("SELECT * FROM plantel_plants WHERE userId = :userId")
    fun getUserPlants(userId: Int): Flow<List<PlantelPlantEntity>>
    
    @Query("SELECT * FROM plantel_plants WHERE id = :plantId")
    suspend fun getPlantById(plantId: Int): PlantelPlantEntity?
    
    @Query("SELECT * FROM plantel_plants WHERE userId = :userId AND productId = :productId")
    suspend fun getPlantByUserAndProduct(userId: Int, productId: Int): PlantelPlantEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: PlantelPlantEntity): Long
    
    @Update
    suspend fun updatePlant(plant: PlantelPlantEntity)
    
    @Delete
    suspend fun deletePlant(plant: PlantelPlantEntity)
    
    @Query("DELETE FROM plantel_plants WHERE userId = :userId")
    suspend fun deleteUserPlants(userId: Int)
}
