package cl.duoc.app.model.data.repositories

import cl.duoc.app.model.data.data.ProductDao
import cl.duoc.app.model.data.entities.ProductEntity
import cl.duoc.app.model.domain.Category
import cl.duoc.app.model.domain.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class ProductRepository(private val productDao: ProductDao) {
    
    // Conversiones entre Entity y Domain
    private fun ProductEntity.toDomain() = Product(
        id = id,
        name = name,
        description = description,
        price = price,
        category = category,
        imageUrl = imageUrl,
        stock = stock,
        rating = rating,
        wateringCycleDays = wateringCycleDays
    )
    
    private fun Product.toEntity() = ProductEntity(
        id = id,
        name = name,
        description = description,
        price = price,
        category = category,
        imageUrl = imageUrl,
        stock = stock,
        rating = rating,
        wateringCycleDays = wateringCycleDays
    )
    
    // Operaciones
    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    suspend fun getProductById(productId: Int): Product? {
        return productDao.getProductById(productId)?.toDomain()
    }
    
    fun getProductsByCategory(category: String): Flow<List<Product>> {
        return productDao.getProductsByCategory(category).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchProducts(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    fun getCategories(): Flow<List<Category>> = flow {
        emit(
            listOf(
                Category("all", "Todas", "🌿"),
                Category("Arbustos", "Arbustos", "🌳"),
                Category("Perennes", "Perennes", "🌸"),
                Category("Aromáticas", "Aromáticas", "🌱"),
                Category("Ornamentales", "Ornamentales", "🪴")
            )
        )
    }
    
    suspend fun insertProduct(product: Product): Long {
        return productDao.insertProduct(product.toEntity())
    }
    
    suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product.toEntity())
    }
    
    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product.toEntity())
    }
}
