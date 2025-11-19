package cl.duoc.app.model.data.dao

import androidx.room.*
import cl.duoc.app.model.data.entities.PurchaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserPurchases(userId: Int): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE id = :purchaseId")
    fun getPurchaseById(purchaseId: Int): Flow<PurchaseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    @Update
    suspend fun updatePurchase(purchase: PurchaseEntity)

    @Delete
    suspend fun deletePurchase(purchase: PurchaseEntity)

    @Query("DELETE FROM purchases WHERE userId = :userId")
    suspend fun deleteAllUserPurchases(userId: Int)
}
