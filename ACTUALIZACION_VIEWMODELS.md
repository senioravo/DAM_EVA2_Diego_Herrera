# 📝 Resumen de Actualización - Integración Backend

## ✅ ViewModels Actualizados

### 1. **AuthViewModel.kt**
**Cambios:**
- ✅ Reemplazado `UserRepository` por `AuthRepository`
- ✅ Login ahora usa API REST y guarda JWT automáticamente
- ✅ Register incluye campo `username` requerido por backend
- ✅ `checkLoginStatus()` verifica JWT y refresca usuario desde backend
- ✅ `logout()` limpia token JWT del TokenManager
- ✅ Manejo de errores mejorado con mensajes del backend

**Uso:**
```kotlin
// En tu Screen/Composable
val viewModel = AuthViewModel(context)

// Login
viewModel.login("user@email.com", "password123", rememberSession = true)

// Register  
viewModel.register("user@email.com", "password123", "password123")

// Logout
viewModel.logout()
```

---

### 2. **CatalogViewModel.kt**
**Cambios:**
- ✅ Reemplazado `ProductRepository()` por `ProductRepositoryAPI()`
- ✅ Carga productos desde backend vía REST API
- ✅ Mantiene misma interfaz (Result<T>), cambio transparente
- ✅ Filtrado y búsqueda funcionan igual

**Uso:**
```kotlin
// No requiere cambios en el código existente
val viewModel = CatalogViewModel()

// Los métodos son idénticos:
viewModel.onSearchQueryChange("lavanda")
viewModel.onCategorySelected("Aromáticas")
viewModel.retry() // Recarga desde backend
```

---

### 3. **PlantelViewModel.kt**
**Cambios:**
- ✅ Ahora acepta `userId` y `useAPI` como parámetros
- ✅ Modo dual: puede usar datos locales O API
- ✅ Carga plantel desde backend con `loadPlantelForUser(userId)`
- ✅ Todas las operaciones sincronizadas con backend cuando useAPI=true

**Uso:**
```kotlin
// Modo API (conectado con backend)
val viewModel = PlantelViewModel(
    userId = currentUser.id, 
    useAPI = true
)

// Modo Local (sin backend)
val viewModel = PlantelViewModel() // Por defecto useAPI=false

// Métodos actualizados:
viewModel.waterPlant(plantelId)      // Usa plantelId (no productId)
viewModel.removePlant(plantelId)     // Requiere plantelId desde backend
viewModel.updateCustomTitle(plantelId, "Mi Lavanda")
viewModel.toggleNotifications(plantelId)
viewModel.addPlantToPlantel(productId) // Nuevo método para agregar
```

---

## ⚙️ Archivo de Configuración

### **AppConfig.kt** (NUEVO)
Archivo central para habilitar/deshabilitar el backend:

```kotlin
object AppConfig {
    // Cambia esto para activar/desactivar el backend
    const val USE_API_BACKEND = true  // true = API, false = Local
    
    object Backend {
        const val EMULATOR_URL = "http://10.0.2.2:8080/api/"
        const val PHYSICAL_DEVICE_URL = "http://192.168.1.X:8080/api/"
        const val PRODUCTION_URL = "https://tu-backend.com/api/"
    }
}
```

**Beneficios:**
- ✅ Un solo lugar para cambiar entre modo local/API
- ✅ Fácil testing sin backend
- ✅ Configuración por entorno (dev/prod)

---

## 🔄 Pasos para Migración Completa

### Opción 1: Migración Gradual (RECOMENDADO)
1. **Mantén** `AppConfig.USE_API_BACKEND = false` durante desarrollo
2. **Prueba** cada pantalla individualmente con backend
3. **Cambia** a `true` cuando todo esté probado

### Opción 2: Migración Inmediata
1. **Cambia** `AppConfig.USE_API_BACKEND = true`
2. **Actualiza** instancias de ViewModels para pasar userId
3. **Ejecuta** backend con `.\run-new-window.bat`
4. **Prueba** la app

---

## 🎯 Cambios Requeridos en Composables

### **En CatalogScreen.kt**
```kotlin
// ANTES
val viewModel: CatalogViewModel = viewModel()

// DESPUÉS - Sin cambios necesarios ✅
val viewModel: CatalogViewModel = viewModel()
```

### **En PlantelScreen.kt**
```kotlin
// ANTES
val viewModel: PlantelViewModel = viewModel()

// DESPUÉS - Agregar userId si usas API
val context = LocalContext.current
val userPreferences = UserPreferences(context)
val userId = userPreferences.currentUserId.collectAsState(initial = null)

val viewModel: PlantelViewModel = viewModel(
    factory = viewModelFactory {
        PlantelViewModel(
            userId = userId.value,
            useAPI = AppConfig.USE_API_BACKEND
        )
    }
)
```

### **En AuthScreen.kt / LoginScreen.kt**
```kotlin
// ANTES
val viewModel: AuthViewModel = viewModel()

// DESPUÉS - Sin cambios necesarios ✅
val context = LocalContext.current
val viewModel = remember { AuthViewModel(context) }
```

---

## 🚀 Cómo Probar

### 1. Backend Local (Desarrollo)
```powershell
# Terminal 1: Iniciar Backend
cd C:\Users\Alex\Documents\BackendPlantBuddy
.\run-new-window.bat

# Terminal 2: Verificar que funciona
Invoke-WebRequest http://localhost:8080/api/productos
```

### 2. Configurar App
```kotlin
// AppConfig.kt
const val USE_API_BACKEND = true  // ✅ Activar API
```

### 3. Ejecutar App
- **Emulador**: Funciona automáticamente con `http://10.0.2.2:8080/api/`
- **Dispositivo físico**: 
  1. Obtén IP de tu PC: `ipconfig`
  2. Actualiza en `RetrofitClient.kt`: `BASE_URL = "http://192.168.1.X:8080/api/"`

### 4. Flujo de Prueba
1. ✅ Registrar nuevo usuario
2. ✅ Login (verifica que guarda JWT)
3. ✅ Ver catálogo (debe cargar desde backend)
4. ✅ Agregar planta al plantel
5. ✅ Regar planta
6. ✅ Cerrar sesión

---

## 🐛 Solución de Problemas

### Error: "Unable to resolve host"
```kotlin
// Causa: Backend no está corriendo
// Solución: Ejecuta .\run-new-window.bat
```

### Error: "401 Unauthorized"
```kotlin
// Causa: Token JWT expirado o inválido
// Solución: 
viewModel.logout()
// Luego hacer login nuevamente
```

### App crashea al abrir Plantel
```kotlin
// Causa: PlantelViewModel necesita userId cuando useAPI=true
// Solución: Pasar userId al crear el ViewModel
PlantelViewModel(userId = currentUser.id, useAPI = true)
```

### Catálogo no muestra productos
```kotlin
// Causa: Backend no tiene datos iniciales
// Solución: Los 7 productos se crean automáticamente en el backend
// al ejecutarlo por primera vez. Verifica los logs del backend.
```

---

## 📊 Comparación: Local vs API

| Funcionalidad | Local (useAPI=false) | API (useAPI=true) |
|---------------|----------------------|-------------------|
| Login/Register | ❌ Usuario admin hardcoded | ✅ Base de datos real |
| Productos | ✅ 7 productos mock | ✅ Desde PostgreSQL/Neon |
| Plantel | ✅ StateFlow local | ✅ Sincronizado con backend |
| Persistencia | ❌ Se pierde al cerrar app | ✅ Guardado en base de datos |
| Multi-dispositivo | ❌ No compartido | ✅ Sincronización automática |
| JWT Auth | ❌ No usa | ✅ Automático con interceptor |

---

## ✨ Ventajas de la Nueva Arquitectura

1. **Transparente**: Los ViewModels siguen usando `Result<T>`, no rompe código existente
2. **Flexible**: Flag `USE_API_BACKEND` para cambiar entre modos
3. **Seguro**: JWT manejado automáticamente por `AuthInterceptor`
4. **Escalable**: Fácil agregar más endpoints sin cambiar ViewModels
5. **Testeable**: Puedes probar sin backend (modo local)

---

## 📝 Archivos Modificados

### ViewModels Actualizados (3)
- ✅ `AuthViewModel.kt` - Login/Register con API
- ✅ `CatalogViewModel.kt` - Productos desde backend
- ✅ `PlantelViewModel.kt` - Plantel sincronizado

### Archivos Nuevos Creados (10+)
- ✅ `AppConfig.kt` - Configuración centralizada
- ✅ `AuthRepository.kt` - Autenticación API
- ✅ `ProductRepositoryAPI.kt` - Productos API
- ✅ `PlantelRepositoryAPI.kt` - Plantel API
- ✅ `AuthApiService.kt` - Endpoints auth
- ✅ `ProductoApiService.kt` - Endpoints productos
- ✅ `PlantelApiService.kt` - Endpoints plantel
- ✅ `CompraApiService.kt` - Endpoints compras
- ✅ `ApiModels.kt` - 20+ DTOs
- ✅ `TokenManager.kt` - Gestión JWT
- ✅ `AuthInterceptor.kt` - Inyección automática token
- ✅ `PlantBuddyApplication.kt` - Init Retrofit

### Archivos Configurados (2)
- ✅ `RetrofitClient.kt` - URL + interceptores
- ✅ `AndroidManifest.xml` - Permisos + Application class

---

## 🎯 Próximos Pasos Sugeridos

1. ✅ **Actualizar Composables** para pasar userId a PlantelViewModel
2. ⏳ **Implementar CompraViewModel** con CompraApiService
3. ⏳ **Agregar refresh pull-to-refresh** en CatalogScreen
4. ⏳ **Implementar offline-first** con Room + sincronización
5. ⏳ **Desplegar backend** en Railway/Render para producción

---

¿Necesitas ayuda actualizando los Composables o implementando alguna otra funcionalidad? 🚀
