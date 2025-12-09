package cl.duoc.app.data.api

import cl.duoc.app.data.api.dto.*
import retrofit2.Response
import retrofit2.http.*

interface CompraApiService {
    
    @POST("compras/crear")
    suspend fun createCompra(@Body request: CreateCompraRequest): Response<CreateCompraResponse>
    
    @GET("compras/usuario/{userId}")
    suspend fun getComprasByUserId(@Path("userId") userId: Int): Response<List<CompraDTO>>
    
    @GET("compras/{id}")
    suspend fun getCompraById(@Path("id") id: Int): Response<CompraDTO>
    
    @PUT("compras/{id}/estado")
    suspend fun updateCompraStatus(
        @Path("id") id: Int,
        @Body request: UpdateStatusRequest
    ): Response<CompraDTO>
    
    @DELETE("compras/{id}/cancelar")
    suspend fun cancelCompra(@Path("id") id: Int): Response<Unit>
}
