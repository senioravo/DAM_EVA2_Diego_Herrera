# 🔌 Guía de Integración Backend - Android App

## ✅ Integración Completada

La aplicación Android ahora está completamente integrada con el backend Spring Boot.

---

## 📁 Archivos Creados

### 1. **Servicios API** (`data/api/`)
- ✅ `AuthApiService.kt` - Autenticación (login, register)
- ✅ `ProductoApiService.kt` - Catálogo de productos
- ✅ `PlantelApiService.kt` - Gestión de plantel personal
- ✅ `CompraApiService.kt` - Sistema de compras
- ✅ `AuthInterceptor.kt` - Interceptor JWT automático

### 2. **DTOs** (`data/api/dto/`)
- ✅ `ApiModels.kt` - Todos los DTOs para comunicación con backend

### 3. **Gestión de Tokens** (`data/preferences/`)
- ✅ `TokenManager.kt` - Manejo de JWT con DataStore

### 4. **Repositories Actualizados** (`data/repository/`)
- ✅ `AuthRepository.kt` - Autenticación con API
- ✅ `ProductRepositoryAPI.kt` - Productos desde backend
- ✅ `PlantelRepositoryAPI.kt` - Plantel sincronizado
- ⚠️ **Repositories antiguos mantienen sufijo sin API** (para compatibilidad)

### 5. **Configuración**
- ✅ `RetrofitClient.kt` - Actualizado con URL del backend
- ✅ `PlantBuddyApplication.kt` - Inicialización de Retrofit
- ✅ `AndroidManifest.xml` - Permisos y Application class

---

## 🌐 Configuración de URL del Backend

### Para Emulador Android:
```kotlin
// RetrofitClient.kt (línea 12)
private const val BASE_URL = "http://10.0.2.2:8080/api/"
```
> `10.0.2.2` es la IP especial que el emulador usa para `localhost` de tu PC

### Para Dispositivo Físico:
1. Conecta el dispositivo a la misma red WiFi que tu PC
2. Obtén la IP de tu PC:
   ```powershell
   ipconfig
   # Busca: "Dirección IPv4" en "Adaptador de LAN inalámbrica Wi-Fi"
   ```
3. Actualiza la URL:
   ```kotlin
   private const val BASE_URL = "http://192.168.1.X:8080/api/"
   ```

### Para Producción:
```kotlin
private const val BASE_URL = "https://tu-backend-produccion.com/api/"
```

---

## 🔄 Cómo Usar los Nuevos Repositories

### 1. **AuthRepository** (Reemplaza UserRepository para login/register)

```kotlin
// En tu ViewModel
class AuthViewModel(context: Context) : ViewModel() {
    
    private val authRepository = AuthRepository(context)
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            
            result.onSuccess { user ->
                // Usuario autenticado, token guardado automáticamente
                _authState.value = AuthState(isLoggedIn = true, currentUser = user)
            }
            
            result.onFailure { error ->
                _authState.value = AuthState(error = error.message)
            }
        }
    }
    
    fun register(email: String, username: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.register(email, username, password)
            // Manejo similar...
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState(isLoggedIn = false)
        }
    }
}
```

### 2. **ProductRepositoryAPI** (Reemplaza ProductRepository)

```kotlin
// En tu CatalogViewModel
class CatalogViewModel : ViewModel() {
    
    private val productRepository = ProductRepositoryAPI()
    
    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            
            val result = productRepository.getProducts()
            
            result.onSuccess { products ->
                _products.value = products
                _isLoading.value = false
            }
            
            result.onFailure { error ->
                _error.value = error.message
                _isLoading.value = false
            }
        }
    }
    
    fun searchProducts(query: String) {
        viewModelScope.launch {
            val result = productRepository.searchProducts(query)
            result.onSuccess { _products.value = it }
        }
    }
    
    fun getProductsByCategory(category: String) {
        viewModelScope.launch {
            val result = productRepository.getProductsByCategory(category)
            result.onSuccess { _products.value = it }
        }
    }
}
```

### 3. **PlantelRepositoryAPI** (Reemplaza PlantelRepository)

```kotlin
// En tu PlantelViewModel
class PlantelViewModel(private val userId: Int) : ViewModel() {
    
    private val plantelRepository = PlantelRepositoryAPI.getInstance()
    val plantelPlants = plantelRepository.plantelPlants.asStateFlow()
    
    init {
        loadPlantel()
    }
    
    fun loadPlantel() {
        viewModelScope.launch {
            plantelRepository.loadPlantelForUser(userId)
        }
    }
    
    fun addPlant(productId: Int) {
        viewModelScope.launch {
            val result = plantelRepository.addPlantToPlantel(userId, productId)
            
            result.onSuccess {
                // Planta agregada, el StateFlow se actualiza automáticamente
            }
        }
    }
    
    fun waterPlant(plantelId: Int) {
        viewModelScope.launch {
            plantelRepository.waterPlant(plantelId)
        }
    }
    
    fun removePlant(plantelId: Int) {
        viewModelScope.launch {
            plantelRepository.removePlant(plantelId)
        }
    }
}
```

---

## 🔐 Sistema de Autenticación JWT

### Flujo Automático:
1. Usuario hace login → `AuthRepository.login()`
2. Backend devuelve JWT token
3. `TokenManager` guarda el token en DataStore
4. `AuthInterceptor` **agrega automáticamente** el token en todas las requests
5. Backend valida el token en cada petición

### Endpoints Públicos (sin token):
- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/productos`
- `GET /api/productos/{id}`

### Endpoints Protegidos (requieren token):
- `GET /api/auth/me`
- `GET /api/plantel/usuario/{userId}`
- `POST /api/plantel/agregar`
- `POST /api/compras/crear`
- Todos los PUT y DELETE

---

## 🧪 Testing de la Integración

### 1. Iniciar Backend:
```powershell
cd C:\Users\Alex\Documents\BackendPlantBuddy
.\run-new-window.bat
```

### 2. Verificar Backend:
```powershell
# Probar endpoint público
Invoke-WebRequest -Uri "http://localhost:8080/api/productos" -Method GET
```

### 3. Ejecutar App Android:
- **Emulador**: La URL `http://10.0.2.2:8080/api/` funciona automáticamente
- **Dispositivo físico**: Actualizar URL con IP de tu PC

### 4. Probar Flujo Completo:
1. ✅ Registrar nuevo usuario
2. ✅ Hacer login (guarda JWT)
3. ✅ Ver catálogo de productos (desde backend)
4. ✅ Agregar planta al plantel (requiere JWT)
5. ✅ Regar planta (requiere JWT)

---

## ⚠️ Problemas Comunes

### Error: "Unable to resolve host"
**Causa**: Backend no está corriendo o URL incorrecta
**Solución**: 
```powershell
cd BackendPlantBuddy
.\run-new-window.bat
```

### Error: "401 Unauthorized"
**Causa**: Token expirado o inválido
**Solución**: Hacer logout y login nuevamente

### Error: "Network Security Exception"
**Causa**: Android bloquea HTTP (no HTTPS)
**Solución**: ✅ Ya agregado `android:usesCleartextTraffic="true"` en AndroidManifest.xml

### Error: "Connection refused" en dispositivo físico
**Causa**: Firewall de Windows bloqueando puerto 8080
**Solución**:
```powershell
# Permitir puerto 8080 en Firewall
New-NetFirewallRule -DisplayName "Backend Plant Buddy" -Direction Inbound -LocalPort 8080 -Protocol TCP -Action Allow
```

---

## 📝 Migración de ViewModels

Para actualizar tus ViewModels existentes:

### AuthViewModel:
```kotlin
// ANTES (con UserRepository local)
private val userRepository = UserRepository.getInstance()
val user = userRepository.login(email, password)

// DESPUÉS (con AuthRepository + API)
private val authRepository = AuthRepository(context)
viewModelScope.launch {
    val result = authRepository.login(email, password)
    result.onSuccess { user -> /* ... */ }
}
```

### CatalogViewModel:
```kotlin
// ANTES
private val productRepository = ProductRepository()

// DESPUÉS
private val productRepository = ProductRepositoryAPI()
// El resto del código es igual (Result<T> se mantiene)
```

### PlantelViewModel:
```kotlin
// ANTES
private val plantelRepository = PlantelRepository.getInstance()

// DESPUÉS
private val plantelRepository = PlantelRepositoryAPI.getInstance()
plantelRepository.loadPlantelForUser(userId) // Cargar desde backend
```

---

## 🚀 Próximos Pasos

1. ✅ Backend corriendo en `localhost:8080`
2. ⏳ Actualizar ViewModels para usar nuevos repositories
3. ⏳ Probar login/register desde la app
4. ⏳ Verificar que el catálogo carga desde backend
5. ⏳ Probar agregar plantas al plantel
6. 🎯 Desplegar backend en producción (Railway/Render)
7. 🎯 Actualizar URL en app para producción

---

## 📊 Resumen de Cambios

| Componente | Estado | Acción Requerida |
|------------|--------|------------------|
| API Services | ✅ Creados | Ninguna |
| DTOs | ✅ Creados | Ninguna |
| TokenManager | ✅ Creado | Ninguna |
| AuthInterceptor | ✅ Creado | Ninguna |
| RetrofitClient | ✅ Actualizado | Cambiar URL para dispositivo físico/producción |
| Repositories | ✅ Creados (con sufijo API) | Actualizar ViewModels para usarlos |
| AndroidManifest | ✅ Actualizado | Ninguna |
| Application Class | ✅ Creada | Ninguna |

---

## 💡 Notas Importantes

- **Los repositories antiguos NO se eliminaron** para mantener compatibilidad
- Los nuevos repositories tienen sufijo **"API"** para distinguirlos
- El **JWT se maneja automáticamente** por el AuthInterceptor
- Todos los métodos usan **suspend** functions y **Result<T>** para manejo de errores
- El backend debe estar corriendo en `localhost:8080` para desarrollo

---

¿Necesitas ayuda actualizando algún ViewModel específico? 🤔
