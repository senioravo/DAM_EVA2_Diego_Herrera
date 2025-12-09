package cl.duoc.app.data.api

import cl.duoc.app.data.api.dto.*
import retrofit2.Response
import retrofit2.http.*

interface PlantelApiService {
    
    @GET("plantel/usuario/{userId}")
    suspend fun getPlantelByUserId(@Path("userId") userId: Int): Response<List<PlantelPlantDTO>>
    
    @POST("plantel/agregar")
    suspend fun addPlantToPlantel(@Body request: AddPlantRequest): Response<PlantelPlantDTO>
    
    @PUT("plantel/{id}/regar")
    suspend fun waterPlant(@Path("id") id: Int): Response<PlantelPlantDTO>
    
    @PUT("plantel/{id}/titulo")
    suspend fun updateCustomTitle(
        @Path("id") id: Int,
        @Body request: UpdateTitleRequest
    ): Response<PlantelPlantDTO>
    
    @PUT("plantel/{id}/notas")
    suspend fun updateNotes(
        @Path("id") id: Int,
        @Body request: UpdateNotesRequest
    ): Response<PlantelPlantDTO>
    
    @PUT("plantel/{id}/notificaciones")
    suspend fun toggleNotifications(@Path("id") id: Int): Response<PlantelPlantDTO>
    
    @DELETE("plantel/{id}")
    suspend fun removePlant(@Path("id") id: Int): Response<Unit>
}
