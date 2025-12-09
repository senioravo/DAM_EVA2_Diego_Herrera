package cl.duoc.app.data.api

import cl.duoc.app.data.api.dto.ProductoDTO
import retrofit2.Response
import retrofit2.http.*

interface ProductoApiService {
    
    @GET("productos")
    suspend fun getAllProductos(): Response<List<ProductoDTO>>
    
    @GET("productos/{id}")
    suspend fun getProductoById(@Path("id") id: Int): Response<ProductoDTO>
    
    @GET("productos/buscar")
    suspend fun searchProductos(@Query("nombre") nombre: String): Response<List<ProductoDTO>>
    
    @GET("productos/categoria/{categoria}")
    suspend fun getProductosByCategoria(@Path("categoria") categoria: String): Response<List<ProductoDTO>>
    
    @GET("productos/destacados")
    suspend fun getProductosDestacados(): Response<List<ProductoDTO>>
}
