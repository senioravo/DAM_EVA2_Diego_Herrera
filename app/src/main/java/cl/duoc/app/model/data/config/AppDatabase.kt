package cl.duoc.app.model.data.config

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.duoc.app.model.data.data.PlantelPlantDao
import cl.duoc.app.model.data.data.ProductDao
import cl.duoc.app.model.data.data.PurchaseDao
import cl.duoc.app.model.data.data.UserDao
import cl.duoc.app.model.data.entities.PlantelPlantEntity
import cl.duoc.app.model.data.entities.ProductEntity
import cl.duoc.app.model.data.entities.PurchaseEntity
import cl.duoc.app.model.data.entities.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        PlantelPlantEntity::class,
        PurchaseEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun plantelPlantDao(): PlantelPlantDao
    abstract fun purchaseDao(): PurchaseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "plant_buddy_db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(database: AppDatabase) {
            val userDao = database.userDao()
            val productDao = database.productDao()

            // Insertar usuario admin por defecto
            val adminUser = UserEntity(
                email = "admin@plantbuddy.com",
                password = "admin123",
                profileImageUrl = null,
                createdAt = Date().time,
                isAdmin = true
            )
            userDao.insertUser(adminUser)

            // Insertar productos de muestra
            val mockProducts = listOf(
                ProductEntity(
                    name = "Viburnum Lucidum",
                    description = "Arbusto perenne de hojas brillantes, ideal para interiores o exteriores, sombra parcial o sol, muy decorativo.",
                    price = 24990.0,
                    category = "Arbustos",
                    imageUrl = "viburnum_lucidum",
                    stock = 10,
                    rating = 4.8f,
                    wateringCycleDays = 3
                ),
                ProductEntity(
                    name = "Kniphofia Uvaria",
                    description = "Planta perenne de floración llamativa tipo \"antorcha\", ideal para bordes soleados y suelo bien drenado.",
                    price = 19990.0,
                    category = "Perennes",
                    imageUrl = "kniphofia_uvaria",
                    stock = 8,
                    rating = 4.6f,
                    wateringCycleDays = 4
                ),
                ProductEntity(
                    name = "Rhus Crenata",
                    description = "Arbusto de hoja perenne, resistente al viento y al sol, ideal para cercos o jardines costeros.",
                    price = 17990.0,
                    category = "Arbustos",
                    imageUrl = "rhus_crenata",
                    stock = 12,
                    rating = 4.5f,
                    wateringCycleDays = 5
                ),
                ProductEntity(
                    name = "Lavanda Dentata",
                    description = "Lavanda francesa de hojas dentadas, arbusto aromático para sol pleno y suelos secos.",
                    price = 15990.0,
                    category = "Aromáticas",
                    imageUrl = "lavanda_dentata",
                    stock = 20,
                    rating = 4.9f,
                    wateringCycleDays = 7
                ),
                ProductEntity(
                    name = "Laurel de Flor Enano",
                    description = "Arbusto ornamental enano de floración, ideal para jardines pequeños o macetas.",
                    price = 13990.0,
                    category = "Ornamentales",
                    imageUrl = "laurel_flor_enano",
                    stock = 15,
                    rating = 4.4f,
                    wateringCycleDays = 2
                ),
                ProductEntity(
                    name = "Pitosporo Tobira Enano",
                    description = "Versión enana del Pittosporum tobira, arbusto ornamental perenne, resistente, ideal para cercos, macetas o jardines.",
                    price = 16990.0,
                    category = "Ornamentales",
                    imageUrl = "pitosporo_tobira_enano",
                    stock = 18,
                    rating = 4.7f,
                    wateringCycleDays = 3
                ),
                ProductEntity(
                    name = "Euphorbia Characias",
                    description = "Planta perenne mediterránea de floración vistosa, resistente a la sequía y al sol directo.",
                    price = 18990.0,
                    category = "Perennes",
                    imageUrl = "euphorbia_characias",
                    stock = 14,
                    rating = 4.3f,
                    wateringCycleDays = 6
                )
            )

            productDao.insertProducts(mockProducts)
        }
    }
}
