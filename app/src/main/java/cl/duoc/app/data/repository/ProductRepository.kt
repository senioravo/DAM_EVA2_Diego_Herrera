package cl.duoc.app.data.repository

import cl.duoc.app.data.model.Category
import cl.duoc.app.data.model.Product
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProductRepository {
    
    // Mock data - 7 productos para la base de datos
    private val mockProducts = listOf(
        Product(
            id = 1,
            name = "Viburnum Lucidum",
            description = "Arbusto perenne de hojas brillantes, ideal para interiores o exteriores, sombra parcial o sol, muy decorativo.",
            price = 24990.0,
            category = "Arbustos",
            imageUrl = "viburnum_lucidum",
            stock = 10,
            rating = 4.8f,
            wateringCycleDays = 3
        ),
        Product(
            id = 2,
            name = "Kniphofia Uvaria",
            description = "Planta perenne de floración llamativa tipo \"antorcha\", ideal para bordes soleados y suelo bien drenado.",
            price = 19990.0,
            category = "Perennes",
            imageUrl = "kniphofia_uvaria",
            stock = 8,
            rating = 4.6f,
            wateringCycleDays = 4
        ),
        Product(
            id = 3,
            name = "Rhus Crenata",
            description = "Arbusto de hoja perenne, resistente al viento y al sol, ideal para cercos o jardines costeros.",
            price = 17990.0,
            category = "Arbustos",
            imageUrl = "rhus_crenata",
            stock = 12,
            rating = 4.5f,
            wateringCycleDays = 5
        ),
        Product(
            id = 4,
            name = "Lavanda Dentata",
            description = "Lavanda francesa de hojas dentadas, arbusto aromático para sol pleno y suelos secos.",
            price = 15990.0,
            category = "Aromáticas",
            imageUrl = "lavanda_dentata",
            stock = 20,
            rating = 4.9f,
            wateringCycleDays = 7
        ),
        Product(
            id = 5,
            name = "Laurel de Flor Enano",
            description = "Arbusto ornamental enano de floración, ideal para jardines pequeños o macetas.",
            price = 13990.0,
            category = "Ornamentales",
            imageUrl = "laurel_flor_enano",
            stock = 15,
            rating = 4.4f,
            wateringCycleDays = 2
        ),
        Product(
            id = 6,
            name = "Pitosporo Tobira Enano",
            description = "Versión enana del Pittosporum tobira, arbusto ornamental perenne, resistente, ideal para cercos, macetas o jardines.",
            price = 16990.0,
            category = "Ornamentales",
            imageUrl = "pitosporo_tobira_enano",
            stock = 18,
            rating = 4.7f,
            wateringCycleDays = 3
        ),
        Product(
            id = 7,
            name = "Bignonia Naranja",
            description = "Planta trepadora de flores naranjas, crecimiento rápido, ideal para pérgolas o muros al sol.",
            price = 21990.0,
            category = "Trepadoras",
            imageUrl = "bignonia_naranja",
            stock = 9,
            rating = 4.8f,
            wateringCycleDays = 2
        )
    )
    
    private val categories = listOf(
        Category("all", "Todas"),
        Category("arbustos", "Arbustos"),
        Category("perennes", "Perennes"),
        Category("aromáticas", "Aromáticas"),
        Category("ornamentales", "Ornamentales"),
        Category("trepadoras", "Trepadoras")
    )
    
    // Simula una llamada a la API
    suspend fun getProducts(): Result<List<Product>> {
        return try {
            delay(500) // Simula latencia de red
            Result.success(mockProducts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchProducts(query: String): Result<List<Product>> {
        return try {
            delay(300)
            val filtered = mockProducts.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getProductsByCategory(category: String): Result<List<Product>> {
        return try {
            delay(300)
            if (category == "all" || category.isEmpty()) {
                Result.success(mockProducts)
            } else {
                val filtered = mockProducts.filter {
                    it.category.equals(category, ignoreCase = true)
                }
                Result.success(filtered)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getCategories(): List<Category> = categories
    
    // Flow para observar productos en tiempo real
    fun observeProducts(): Flow<List<Product>> = flow {
        emit(mockProducts)
    }
}
