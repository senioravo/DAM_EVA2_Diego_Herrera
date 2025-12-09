package cl.duoc.app.data.api

import android.content.Context
import cl.duoc.app.data.preferences.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    
    private val tokenManager = TokenManager(context)
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Obtener token guardado
        val token = tokenManager.getToken()
        
        // Si no hay token o la request es a endpoints públicos, proceder sin token
        val publicEndpoints = listOf("/auth/login", "/auth/register", "/productos")
        val isPublicEndpoint = publicEndpoints.any { originalRequest.url.encodedPath.contains(it) }
        
        return if (token != null && !isPublicEndpoint) {
            // Agregar header de autorización
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}
