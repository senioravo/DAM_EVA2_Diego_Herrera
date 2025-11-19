package cl.duoc.app.model.data.repositories

import cl.duoc.app.model.data.dao.ProductDao
import cl.duoc.app.model.data.entities.ProductEntity
import cl.duoc.app.model.domain.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepository(private val productDao: ProductDao) {

    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { list -> list.map { it.toDomain() } }
    }

    fun getProductById(productId: Int): Flow<Product?> {
        return productDao.getProductById(productId).map { it?.toDomain() }
    }

    fun getProductsByCategory(category: String): Flow<List<Product>> {
        return productDao.getProductsByCategory(category).map { list -> list.map { it.toDomain() } }
    }

    fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchProducts(query).map { list -> list.map { it.toDomain() } }
    }

    suspend fun insertProduct(product: Product) {
        productDao.insertProduct(product.toEntity())
    }

    suspend fun insertAll(products: List<Product>) {
        productDao.insertAll(products.map { it.toEntity() })
    }

    suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product.toEntity())
    }

    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product.toEntity())
    }

    suspend fun deleteAll() {
        productDao.deleteAll()
    }

    // Conversiones Entity ↔ Domain
    private fun ProductEntity.toDomain() = Product(
        id = id,
        name = name,
        description = description,
        price = price,
        imageUrl = imageUrl,
        category = category,
        stock = stock,
        rating = rating
    )

    private fun Product.toEntity() = ProductEntity(
        id = id,
        name = name,
        description = description,
        price = price,
        imageUrl = imageUrl,
        category = category,
        stock = stock,
        rating = rating
    )
}
