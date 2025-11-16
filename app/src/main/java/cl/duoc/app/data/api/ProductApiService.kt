package cl.duoc.app.data.api

import cl.duoc.app.data.model.Product
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductApiService {
    @GET("products")
    suspend fun getProducts(): List<Product>
    
    @GET("products/search")
    suspend fun searchProducts(@Query("q") query: String): List<Product>
    
    @GET("products/category")
    suspend fun getProductsByCategory(@Query("category") category: String): List<Product>
}
