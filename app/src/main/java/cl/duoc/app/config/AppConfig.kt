package cl.duoc.app.config

/**
 * Configuración global de la aplicación
 * 
 * Cambia USE_API_BACKEND a true para conectar con el backend REST
 * Cambia a false para usar datos locales (modo offline)
 */
object AppConfig {
    
    /**
     * Flag para habilitar/deshabilitar el uso del backend API
     * 
     * true  = Usa AuthRepository, ProductRepositoryAPI, PlantelRepositoryAPI (conecta con backend)
     * false = Usa UserRepository, ProductRepository, PlantelRepository (datos locales)
     */
    const val USE_API_BACKEND = true
    
    /**
     * URL del backend para diferentes entornos
     */
    object Backend {
        // Para emulador Android (10.0.2.2 apunta a localhost de tu PC)
        const val EMULATOR_URL = "http://10.0.2.2:8080/api/"
        
        // Para dispositivo físico (reemplaza con la IP de tu PC en la red local)
        const val PHYSICAL_DEVICE_URL = "http://192.168.1.100:8080/api/"
        
        // Para producción
        const val PRODUCTION_URL = "https://tu-backend.com/api/"
    }
    
    /**
     * Configuración de debug
     */
    const val DEBUG_MODE = true
    const val LOG_HTTP_REQUESTS = true
}
