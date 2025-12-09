package cl.duoc.app.data.repository

import android.util.Log
import cl.duoc.app.data.api.RetrofitClient
import cl.duoc.app.data.api.dto.ProductoDTO
import cl.duoc.app.data.model.Category
import cl.duoc.app.data.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProductRepositoryAPI {
    
    private val api = RetrofitClient.productoApi
    private val TAG = "ProductRepositoryAPI"
    
    private val categories = listOf(
        Category("all", "Todas"),
        Category("arbustos", "Arbustos"),
        Category("perennes", "Perennes"),
        Category("aromáticas", "Aromáticas"),
        Category("ornamentales", "Ornamentales"),
        Category("trepadoras", "Trepadoras")
    )
    
    // Obtener todos los productos desde el backend
    suspend fun getProducts(): Result<List<Product>> {
        return try {
            val response = RetrofitClient.productoApi.getAllProductos()
            if (response.isSuccessful && response.body() != null) {
                val products = response.body()!!.map { it.toProduct() }
                Log.d(TAG, "Productos obtenidos: ${products.size}")
                Result.success(products)
            } else {
                Log.e(TAG, "Error en respuesta: ${response.code()} - ${response.message()}")
                Result.failure(Exception("Error al obtener productos: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al obtener productos", e)
            Result.failure(e)
        }
    }
    
    // Buscar productos por nombre
    suspend fun searchProducts(query: String): Result<List<Product>> {
        return try {
            val response = api.searchProductos(query)
            if (response.isSuccessful && response.body() != null) {
                val products = response.body()!!.map { it.toProduct() }
                Log.d(TAG, "Búsqueda '$query': ${products.size} resultados")
                Result.success(products)
            } else {
                Result.failure(Exception("Error en búsqueda: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en búsqueda", e)
            Result.failure(e)
        }
    }
    
    // Obtener productos por categoría
    suspend fun getProductsByCategory(category: String): Result<List<Product>> {
        return try {
            if (category == "all" || category.isEmpty()) {
                return getProducts()
            }
            
            val response = api.getProductosByCategoria(category)
            if (response.isSuccessful && response.body() != null) {
                val products = response.body()!!.map { it.toProduct() }
                Log.d(TAG, "Categoría '$category': ${products.size} productos")
                Result.success(products)
            } else {
                Result.failure(Exception("Error al filtrar por categoría: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener por categoría", e)
            Result.failure(e)
        }
    }
    
    // Obtener productos destacados
    suspend fun getFeaturedProducts(): Result<List<Product>> {
        return try {
            val response = api.getProductosDestacados()
            if (response.isSuccessful && response.body() != null) {
                val products = response.body()!!.map { it.toProduct() }
                Log.d(TAG, "Productos destacados: ${products.size}")
                Result.success(products)
            } else {
                Result.failure(Exception("Error al obtener destacados: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en productos destacados", e)
            Result.failure(e)
        }
    }
    
    // Obtener producto por ID
    suspend fun getProductById(id: Int): Result<Product> {
        return try {
            val response = api.getProductoById(id)
            if (response.isSuccessful && response.body() != null) {
                val product = response.body()!!.toProduct()
                Log.d(TAG, "Producto obtenido: ${product.name}")
                Result.success(product)
            } else {
                Result.failure(Exception("Producto no encontrado"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener producto", e)
            Result.failure(e)
        }
    }
    
    fun getCategories(): List<Category> = categories
    
    // Flow para observar productos en tiempo real
    fun observeProducts(): Flow<List<Product>> = flow {
        val result = getProducts()
        if (result.isSuccess) {
            emit(result.getOrNull() ?: emptyList())
        }
    }
    
    // Mapper: ProductoDTO -> Product
    private fun ProductoDTO.toProduct(): Product {
        return Product(
            id = this.id,
            name = this.nombre,
            description = this.descripcion ?: "",
            price = this.precio,
            category = this.categoria?.nombre ?: "Sin categoría",
            imageUrl = this.imagenUrl ?: "",
            stock = this.stock,
            rating = this.rating ?: 0f,
            wateringCycleDays = this.plantaDetalle?.frecuenciaRiego?.toIntOrNull() ?: 3
        )
    }
}
