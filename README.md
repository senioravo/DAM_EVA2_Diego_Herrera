# Plant Buddy 🌱

Aplicación Android nativa desarrollada en Kotlin con Jetpack Compose para la gestión y cuidado de plantas ornamentales. Implementa arquitectura **Clean Architecture + MVVM + Room Database** con persistencia local y preparada para integración backend.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5.4-green.svg)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/Room-2.6.1-orange.svg)](https://developer.android.com/training/data-storage/room)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![License](https://img.shields.io/badge/License-Educational-yellow.svg)]()

## 📱 Características Principales

### ✅ Implementado
- 🏠 **Pantalla de Bienvenida**: Onboarding inicial
- 🔐 **Sistema de Autenticación**: Login y registro con validaciones
- 🛒 **Catálogo de Productos**: Búsqueda, filtros por categoría, scroll collapse
- 🛍️ **Carrito de Compras**: Gestión de carrito con checkout
- 🌿 **Plantel Personal**: Gestión de plantas del usuario con recordatorios
- ⚙️ **Ajustes**: Configuración de perfil y preferencias
- 🗄️ **Room Database**: Persistencia local con 4 tablas relacionales
- 🎨 **Material Design 3**: UI moderna con dynamic colors
- 🔄 **Navegación**: Navigation Compose con bottom bar
- ⚡ **Alto Rendimiento**: Configuración 120Hz optimizada

### 🚧 En Desarrollo
- 📊 **Dashboard de Estadísticas**: Visualización de datos del plantel
- 🔔 **Notificaciones**: Recordatorios de riego con WorkManager
- 💾 **Sincronización Backend**: Integración con Neon Postgres

## 🏗️ Arquitectura

### Clean Architecture + MVVM + Room Database

```
┌─────────────────────────────────────────────────────┐
│                    UI LAYER                          │
│  • Jetpack Compose (@Composable)                    │
│  • Material Design 3                                │
│  • ViewModels (StateFlow)                           │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│                 DOMAIN LAYER                         │
│  • Pure Kotlin Models (Product, User, etc)          │
│  • UI States (LoginUIState, RegisterUIState)        │
│  • Business Logic                                   │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│                  DATA LAYER                          │
│  • Repositories (Entity ↔ Domain)                   │
│  • Room Database (SQLite)                           │
│  • DAOs (@Query, @Insert, @Update, @Delete)         │
│  • Entities (@Entity, @PrimaryKey, @ForeignKey)     │
└─────────────────────────────────────────────────────┘
```

### Capas de Datos

| Capa | Responsabilidad | Tecnología |
|------|----------------|------------|
| **UI** | Renderizado y eventos | Jetpack Compose + Material3 |
| **Presentation** | Estado y lógica UI | ViewModel + StateFlow |
| **Domain** | Modelos de negocio | Kotlin Data Classes |
| **Data** | Persistencia y acceso | Room Database + Retrofit (preparado) |

## 🛠️ Stack Tecnológico

### Core
- **Kotlin**: 2.0.21
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 14)
- **JVM Target**: 17

### UI
- **Jetpack Compose**: 1.5.4 (BOM 2024.02.00)
- **Material Design 3**: Última versión estable
- **Navigation Compose**: 2.7.6
- **Coil**: 2.5.0 (carga de imágenes)

### Persistencia
- **Room Database**: 2.6.1
  - room-runtime
  - room-ktx (Coroutines + Flow)
  - room-compiler (KSP)
- **DataStore**: 1.0.0 (preferencias)

### Procesamiento
- **KSP** (Kotlin Symbol Processing): 2.0.21-1.0.27
  - Reemplazo de KAPT
  - 2x más rápido en compilación
  - Usado por Room para generar DAOs

### Asincronía
- **Coroutines**: 1.7.3
- **Flow**: Reactive streams
- **StateFlow**: Estado observable

### Networking (Preparado)
- **Retrofit**: 2.9.0
- **OkHttp**: 4.12.0
- **Gson**: Serialización JSON

### Testing
- **JUnit**: 4.13.2
- **Espresso**: 3.5.1
- **Compose UI Test**: incluido

## 📦 Estructura del Proyecto

```
app/src/main/java/cl/duoc/app/
├── MainActivity.kt                           # Activity principal
│
├── model/                                    # 🆕 ROOM DATABASE
│   ├── data/
│   │   ├── config/
│   │   │   ├── AppDatabase.kt               # Singleton Room DB (4 tablas)
│   │   │   └── Converters.kt                # TypeConverters (Date ↔ Long)
│   │   ├── entities/
│   │   │   ├── UserEntity.kt                # @Entity users
│   │   │   ├── ProductEntity.kt             # @Entity products
│   │   │   ├── PlantelPlantEntity.kt        # @Entity plantel_plants
│   │   │   └── PurchaseEntity.kt            # @Entity purchases
│   │   ├── dao/
│   │   │   ├── UserDao.kt                   # CRUD usuarios
│   │   │   ├── ProductDao.kt                # CRUD productos + búsqueda
│   │   │   ├── PlantelPlantDao.kt           # CRUD plantel
│   │   │   └── PurchaseDao.kt               # CRUD compras
│   │   └── repositories/
│   │       ├── UserRepository.kt            # Entity ↔ Domain
│   │       ├── ProductRepository.kt         # Entity ↔ Domain
│   │       └── PlantelRepository.kt         # Entity ↔ Domain
│   └── domain/
│       ├── User.kt                          # Modelo de negocio
│       ├── Product.kt                       # Modelo de negocio
│       ├── PlantelPlant.kt                  # Modelo de negocio
│       ├── Purchase.kt                      # Modelo + Enums
│       ├── CartItem.kt                      # Modelo carrito
│       ├── LoginUIState.kt                  # Estado login
│       └── RegisterUIState.kt               # Estado registro
│
├── data/                                     # 📦 LEGACY (coexiste)
│   ├── model/                               # Modelos antiguos
│   ├── repository/                          # Repositorios mock
│   ├── api/                                 # Retrofit services
│   └── preferences/                         # DataStore
│
├── ui/
│   ├── screens/
│   │   ├── auth/
│   │   │   ├── LoginScreen.kt
│   │   │   └── RegisterScreen.kt
│   │   ├── catalog/
│   │   │   ├── CatalogScreen.kt
│   │   │   ├── CatalogViewModel.kt
│   │   │   └── CatalogUIState.kt
│   │   ├── cart/
│   │   │   └── CartScreen.kt
│   │   ├── plantel/
│   │   │   ├── PlantelScreen.kt
│   │   │   ├── PlantelViewModel.kt
│   │   │   └── PlantelUIState.kt
│   │   ├── BienvenidaScreen.kt
│   │   ├── HomeScreen.kt
│   │   └── AjustesScreen.kt
│   ├── components/
│   │   └── BottomNavigationBar.kt
│   ├── viewmodel/
│   │   ├── AuthViewModel.kt
│   │   ├── CartViewModel.kt
│   │   └── SettingsViewModel.kt
│   └── theme/
│       ├── Theme.kt
│       └── Type.kt
│
├── navigation/
│   ├── Navigation.kt
│   └── AppRoutes.kt
│
└── notifications/
    ├── NotificationHelper.kt
    ├── NotificationPermissionHelper.kt
    └── WateringReminderReceiver.kt
```

## 🚀 Tecnologías Utilizadas

### Room Database

**AppDatabase** (4 tablas relacionales):
```kotlin
@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        PlantelPlantEntity::class,
        PurchaseEntity::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase()
```

**Características**:
- ✅ TypeConverters para `Date` ↔ `Long`
- ✅ Foreign Keys con `CASCADE` delete
- ✅ Flow reactivo para observar cambios
- ✅ Singleton pattern thread-safe
- ✅ Validación de queries en compilación

### DAOs con Flow Reactivo

```kotlin
@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>
    
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<ProductEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)
}
```

### Repository Pattern

```kotlin
class ProductRepository(private val productDao: ProductDao) {
    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { it.toDomain() }  // Entity → Domain
        }
    }
    
    suspend fun insertProduct(product: Product) {
        productDao.insertProduct(product.toEntity())  // Domain → Entity
    }
}
```

## ⚙️ Configuración del Proyecto

### Requisitos Previos

- **Android Studio**: Hedgehog (2023.1.1) o superior
- **JDK**: 17 (Eclipse Adoptium o Oracle)
- **Gradle**: 8.13 (incluido en wrapper)
- **SDK Android**:
  - Min SDK: 24 (Android 7.0 Nougat)
  - Target SDK: 36 (Android 14)
  - Compile SDK: 36

### Instalación y Configuración

1. **Clonar el repositorio**
```bash
git clone https://github.com/senioravo/DAM_EVA2_Diego_Herrera.git
cd DAM_EVA2_Diego_Herrera
```

2. **Abrir en Android Studio**
   - File → Open → Seleccionar carpeta del proyecto
   - Esperar a que Gradle sincronice

3. **Configurar JDK 17** (si no está configurado)
   - File → Project Structure → SDK Location
   - JDK Location: Seleccionar JDK 17

4. **Verificar configuración de Gradle**
```bash
./gradlew --version
```

5. **Sincronizar dependencias**
   - File → Sync Project with Gradle Files
   - O hacer clic en "Sync Now" cuando aparezca el banner

### Compilación y Ejecución

#### Compilación Debug
```bash
./gradlew assembleDebug
# APK generado en: app/build/outputs/apk/debug/app-debug.apk
```

#### Compilación Release
```bash
./gradlew assembleRelease
# APK optimizado en: app/build/outputs/apk/release/app-release.apk
```

#### Instalación en dispositivo
```bash
# Conectar dispositivo por USB o emulador
./gradlew installDebug
```

#### Limpiar proyecto
```bash
./gradlew clean
```

#### Build completo
```bash
./gradlew clean build
```

### Inicializar Base de Datos Room

La base de datos se inicializa automáticamente en el primer lanzamiento. Para prellenar datos de prueba:

```kotlin
// En MainActivity.onCreate() o Application.onCreate()
lifecycleScope.launch {
    val db = AppDatabase.getDatabase(applicationContext)
    
    // Verificar si hay datos
    val productCount = db.productDao().getAllProducts().first().size
    
    if (productCount == 0) {
        // Insertar productos iniciales
        val products = listOf(
            ProductEntity(
                name = "Viburnum Lucidum",
                description = "Arbusto perenne",
                price = 24990.0,
                category = "Arbustos",
                imageUrl = "viburnum_lucidum",
                stock = 10,
                rating = 4.8
            ),
            // ... más productos
        )
        db.productDao().insertAll(products)
    }
}
```

### Configuración de 120Hz (Opcional)

Para dispositivos compatibles con alta frecuencia de actualización:

1. **Verificar en código** (ya implementado en MainActivity):
```kotlin
private fun setupHighRefreshRate() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val highestRefreshRateMode = windowManager.defaultDisplay
            .supportedModes
            .maxByOrNull { it.refreshRate }
        
        window.attributes.preferredDisplayModeId = highestRefreshRateMode?.modeId ?: 0
    }
}
```

2. **Configuración en MIUI (Xiaomi)**:
   - Ajustes → Pantalla → Frecuencia de actualización
   - Establecer frecuencia por aplicación → Plant Buddy → 120Hz

### Verificar Instalación

```bash
# Ver logs en tiempo real
adb logcat | grep "PlantBuddy"

# Verificar refresh rate configurado
adb logcat | grep "RefreshRate"

# Inspeccionar base de datos (requiere dispositivo rooteado o emulador)
adb shell
run-as cl.duoc.app
ls databases/
# Debería mostrar: plant_buddy_db, plant_buddy_db-shm, plant_buddy_db-wal
```

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Ejecutar test específico
```bash
./gradlew test --tests "ProductRepositoryTest.testGetAllProducts"
```

## 📚 Documentación Adicional

- **[FUNCIONAMIENTO_APP.md](FUNCIONAMIENTO_APP.md)**: Documentación técnica completa
  - Arquitectura detallada
  - Room Database (Entities, DAOs, Repositories)
  - Guía de desarrollo
  - Glosario de términos
  
- **[SISTEMA_AUTENTICACION.md](SISTEMA_AUTENTICACION.md)**: Sistema de login y registro

- **[database/README.md](database/README.md)**: Esquema SQL para Neon Postgres (backend)

- **[BACKEND_SETUP.md](database/BACKEND_SETUP.md)**: Guía de integración backend

## 🔧 Troubleshooting

### Problema: "Cannot access database on the main thread"
**Solución**: Usar `viewModelScope.launch` o `suspend fun`
```kotlin
// ❌ Incorrecto
fun loadData() {
    val data = dao.getData()  // Crash!
}

// ✅ Correcto
fun loadData() {
    viewModelScope.launch {
        dao.getData().collect { data ->
            // Procesar datos
        }
    }
}
```

### Problema: "Room not generating DAOs"
**Solución**:
1. Build → Rebuild Project
2. Verificar que KSP esté en `build.gradle.kts`:
```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.0.21-1.0.27"
}
```
3. File → Invalidate Caches → Invalidate and Restart

### Problema: "Unresolved reference: R"
**Solución**:
```bash
./gradlew clean
# Si persiste, eliminar manualmente:
rm -rf app/build/intermediates/compile_and_runtime_not_namespaced_r_class_jar/
./gradlew assembleDebug
```

### Problema: Gradle sync fails
**Solución**:
1. Verificar conexión a Internet
2. Invalidar caché: `rm -rf ~/.gradle/caches/`
3. Actualizar Gradle wrapper: `./gradlew wrapper --gradle-version=8.13`

## 📊 Métricas del Proyecto

- **Líneas de código**: ~15,000 (Kotlin)
- **Pantallas**: 8 (Login, Register, Home, Catalog, Cart, Plantel, Settings, etc)
- **Componentes Compose**: 50+
- **ViewModels**: 5
- **Repositories**: 4
- **Entities**: 4
- **DAOs**: 4
- **APK size**: ~12 MB (debug) / ~8 MB (release con R8)

## 🗺️ Roadmap

### Versión 2.0.0 (Actual) ✅
- [x] Room Database con 4 tablas
- [x] Clean Architecture (Domain + Data + UI)
- [x] Sistema de carrito de compras
- [x] Scroll collapse en toolbar
- [x] KSP para procesamiento de anotaciones

### Versión 2.1.0 (En Progreso) 🚧
- [ ] Migración completa de ViewModels a Room
- [ ] PurchaseRepository implementado
- [ ] CartRepository con Room
- [ ] Notificaciones de riego con WorkManager

### Versión 3.0.0 (Planeada) 📅
- [ ] Backend API con Neon Postgres
- [ ] Sincronización Room ↔ API
- [ ] Imágenes remotas con Cloudinary
- [ ] Sistema de reviews y ratings
- [ ] Panel de administración web

## 🤝 Contribución

Este es un proyecto educativo. Si encuentras bugs o tienes sugerencias:

1. Fork el repositorio
2. Crea una rama: `git checkout -b feature/nueva-funcionalidad`
3. Commit: `git commit -am 'Agregar nueva funcionalidad'`
4. Push: `git push origin feature/nueva-funcionalidad`
5. Crea un Pull Request

### Guía de Estilo

- **Kotlin**: Seguir [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- **Compose**: Usar `@Composable` en PascalCase
- **Comentarios**: Documentar funciones públicas con KDoc
- **Commits**: Usar [Conventional Commits](https://www.conventionalcommits.org/)

### Estructura de Commits
```
feat: agregar búsqueda avanzada en catálogo
fix: corregir crash al eliminar planta
docs: actualizar README con nuevas features
refactor: reorganizar estructura de repositorios
test: agregar tests unitarios para ProductRepository
```

## 📄 Licencia

Proyecto Educativo - DAM EVA2 Diego Herrera

Este proyecto fue desarrollado como parte de una evaluación académica en el contexto del curso de Desarrollo de Aplicaciones Móviles.

## 👨‍💻 Autor

**Diego Herrera**
- GitHub: [@senioravo](https://github.com/senioravo)
- Proyecto: DAM_EVA2_Diego_Herrera

## 🙏 Agradecimientos

- **Jetpack Compose Team**: Por el excelente framework UI
- **Room Database**: Por simplificar la persistencia en Android
- **Material Design**: Por las guías de diseño
- **Android Developer Community**: Por recursos y documentación

## 📞 Soporte

Para preguntas o problemas:
1. Revisar [FUNCIONAMIENTO_APP.md](FUNCIONAMIENTO_APP.md) (documentación técnica completa)
2. Buscar en [Issues](https://github.com/senioravo/DAM_EVA2_Diego_Herrera/issues)
3. Crear un nuevo Issue con detalles del problema

---

**Plant Buddy** - *Cuida tus plantas con tecnología* 🌱📱

## Contribución

Este proyecto es parte de una evaluación académica. Las mejoras y sugerencias son bienvenidas.

## Licencia

Proyecto educativo - DAM EVA2 Diego Herrera