package cl.duoc.app.data.api

import android.content.Context
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Backend URL - Usar 10.0.2.2 para emulador Android conectándose a localhost
    // Para dispositivo físico, usar la IP de tu PC en la red local (ej: "http://192.168.1.X:8080/api/")
    private const val BASE_URL = "http://10.0.2.2:8080/api/"
    
    // Para producción:
    // private const val BASE_URL = "https://tu-backend-produccion.com/api/"
    
    private lateinit var authInterceptor: AuthInterceptor
    
    fun initialize(context: Context) {
        authInterceptor = AuthInterceptor(context.applicationContext)
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val client by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    private val gson by lazy {
        GsonBuilder()
            .setLenient()
            .create()
    }
    
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    // API Services
    val authApi: AuthApiService by lazy {
        instance.create(AuthApiService::class.java)
    }
    
    val productoApi: ProductoApiService by lazy {
        instance.create(ProductoApiService::class.java)
    }
    
    val plantelApi: PlantelApiService by lazy {
        instance.create(PlantelApiService::class.java)
    }
    
    val compraApi: CompraApiService by lazy {
        instance.create(CompraApiService::class.java)
    }
}
