package cl.duoc.app.model.data.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cl.duoc.app.model.data.entities.PurchaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    
    @Query("SELECT * FROM purchases WHERE userId = :userId")
    fun getUserPurchases(userId: Int): Flow<List<PurchaseEntity>>
    
    @Query("SELECT * FROM purchases WHERE id = :purchaseId")
    suspend fun getPurchaseById(purchaseId: Int): PurchaseEntity?
    
    @Query("SELECT * FROM purchases WHERE userId = :userId AND status = :status")
    fun getUserPurchasesByStatus(userId: Int, status: String): Flow<List<PurchaseEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity): Long
    
    @Update
    suspend fun updatePurchase(purchase: PurchaseEntity)
    
    @Delete
    suspend fun deletePurchase(purchase: PurchaseEntity)
}
