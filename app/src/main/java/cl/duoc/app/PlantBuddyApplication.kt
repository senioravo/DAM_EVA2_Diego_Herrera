package cl.duoc.app

import android.app.Application
import cl.duoc.app.data.api.RetrofitClient

class PlantBuddyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar RetrofitClient con contexto para el AuthInterceptor
        RetrofitClient.initialize(this)
    }
}
