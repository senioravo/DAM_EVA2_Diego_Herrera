# 📐 Nueva Estructura del Proyecto - Plant Buddy

## Resumen de Reorganización

El proyecto ha sido completamente reorganizado siguiendo una **arquitectura MVVM limpia** con **Room Database** para persistencia local.

---

## 🗂️ Estructura de Carpetas Principal

```
app/src/main/java/cl/duoc/app/
├── MainActivity.kt                    # Actividad principal
├── model/                             # Capa de Modelo (NUEVO)
│   ├── data/                         # Capa de Datos
│   │   ├── config/                   # Configuraciones
│   │   │   ├── AppDatabase.kt       # Base de datos Room
│   │   │   ├── RetrofitClient.kt    # Cliente API REST
│   │   │   └── UserPreferences.kt   # DataStore preferences
│   │   ├── data/                     # DAOs (Data Access Objects)
│   │   │   ├── UserDao.kt
│   │   │   ├── ProductDao.kt
│   │   │   ├── PlantelPlantDao.kt
│   │   │   └── PurchaseDao.kt
│   │   ├── entities/                 # Entidades de Room
│   │   │   ├── UserEntity.kt
│   │   │   ├── ProductEntity.kt
│   │   │   ├── PlantelPlantEntity.kt
│   │   │   └── PurchaseEntity.kt
│   │   └── repositories/             # Repositorios
│   │       ├── UserRepository.kt
│   │       ├── ProductRepository.kt
│   │       └── PlantelRepository.kt
│   └── domain/                       # Modelos de Dominio
│       ├── formularios/              # Estados de formularios UI
│       │   ├── LoginUIState.kt
│       │   └── RegisterUIState.kt
│       ├── User.kt
│       ├── Product.kt
│       ├── PlantelPlant.kt
│       ├── Purchase.kt
│       └── Category.kt
├── navigation/                        # Navegación (sin cambios)
│   ├── AppRoutes.kt
│   └── Navigation.kt
├── ui/                               # Capa de UI
│   ├── components/
│   │   └── BottomNavigationBar.kt
│   ├── screens/
│   │   ├── auth/
│   │   │   ├── LoginScreen.kt
│   │   │   └── RegisterScreen.kt
│   │   ├── catalog/
│   │   │   └── CatalogScreen.kt
│   │   ├── plantel/
│   │   │   └── PlantelScreen.kt
│   │   ├── HomeScreen.kt
│   │   ├── AjustesScreen.kt
│   │   └── BienvenidaScreen.kt
│   └── theme/
│       ├── Theme.kt
│       └── Type.kt
├── viewmodel/                         # ViewModels (REORGANIZADO)
│   ├── AuthViewModel.kt
│   ├── CatalogViewModel.kt
│   ├── PlantelViewModel.kt
│   └── SettingsViewModel.kt
└── notifications/                     # Notificaciones (sin cambios)
    ├── NotificationHelper.kt
    ├── NotificationPermissionHelper.kt
    └── WateringReminderReceiver.kt
```

---

## 🏗️ Arquitectura MVVM con Room

### **Model (Modelo)**

#### **model/data/** - Capa de Datos
- **config/**: Configuraciones de infraestructura
  - `AppDatabase.kt`: Base de datos Room con singleton
  - `RetrofitClient.kt`: Cliente para llamadas API REST
  - `UserPreferences.kt`: Almacenamiento de preferencias con DataStore

- **data/**: DAOs - Interfaces para acceso a datos
  - Operaciones CRUD para cada entidad
  - Uso de Flow para datos reactivos
  - Queries personalizadas (búsqueda, filtros)

- **entities/**: Entidades de Room
  - Representación de tablas en SQLite
  - Anotaciones `@Entity`, `@PrimaryKey`
  - Conversión de tipos complejos a tipos primitivos

- **repositories/**: Repositorios
  - Capa de abstracción entre ViewModels y DAOs
  - Conversión entre Entities (datos) y Domain models (lógica)
  - Manejo de lógica de negocio

#### **model/domain/** - Modelos de Dominio
- **formularios/**: Estados de formularios UI
  - `LoginUIState`: Estado del formulario de login
  - `RegisterUIState`: Estado del formulario de registro

- **Modelos de dominio**: Representación de la lógica de negocio
  - `User`, `Product`, `PlantelPlant`, `Purchase`, `Category`
  - Sin dependencias de Room ni Android
  - Contienen lógica de negocio (ej: `getCurrentState()` en `PlantelPlant`)

### **View (Vista)**
- **ui/screens/**: Composables de pantallas
- **ui/components/**: Componentes reutilizables
- **ui/theme/**: Tema y tipografía

### **ViewModel**
- **viewmodel/**: ViewModels con AndroidViewModel
  - Acceso a Application context para database
  - Manejo de estados con StateFlow
  - Operaciones asíncronas con coroutines

---

## 🗄️ Room Database

### Configuración

```kotlin
@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        PlantelPlantEntity::class,
        PurchaseEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun plantelPlantDao(): PlantelPlantDao
    abstract fun purchaseDao(): PurchaseDao
    
    companion object {
        fun getDatabase(context: Context): AppDatabase // Singleton
    }
}
```

### Entidades Principales

1. **UserEntity**: Usuarios de la aplicación
   - Campos: id, email, password, profileImageUrl, createdAt, isAdmin

2. **ProductEntity**: Productos del catálogo
   - Campos: id, name, description, price, category, imageUrl, stock, rating, wateringCycleDays

3. **PlantelPlantEntity**: Plantas del usuario
   - Campos: id, productId, userId, addedDate, lastWateredDate, assistanceStarted, customTitle, wateringHistory, notificationsEnabled

4. **PurchaseEntity**: Compras realizadas
   - Campos: id, userId, productId, productName, quantity, totalPrice, purchaseDate, status

### DAOs (Data Access Objects)

Cada DAO proporciona:
- **Flow** para observación reactiva de datos
- **suspend functions** para operaciones asíncronas
- **CRUD** completo (Create, Read, Update, Delete)
- **Queries personalizadas** (búsqueda, filtros, joins)

---

## 🔄 Flujo de Datos

```
UI (Compose) 
    ↕ observa StateFlow
ViewModel 
    ↕ llama métodos
Repository 
    ↕ convierte Entity ↔ Domain
DAO 
    ↕ queries SQL
Room Database (SQLite)
```

### Ejemplo: Cargar Productos

1. **CatalogScreen** observa `viewModel.estado`
2. **CatalogViewModel** inicia `loadData()`
3. **ProductRepository** usa `productDao.getAllProducts()`
4. **ProductDao** devuelve `Flow<List<ProductEntity>>`
5. **Repository** convierte a `Flow<List<Product>>` (domain model)
6. **ViewModel** actualiza `_estado` con `StateFlow`
7. **UI** se recompone automáticamente

---

## 📦 Dependencias Añadidas

```kotlin
// Room Database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// Gson para serialización
implementation("com.google.code.gson:gson:2.10.1")

// KSP Plugin
id("com.google.devtools.ksp") version "2.0.21-1.0.27"
```

---

## 🔑 Características Implementadas

### 1. **Persistencia Local**
- Todos los datos se guardan en SQLite vía Room
- Datos disponibles offline
- Sincronización reactiva con Flow

### 2. **Separación de Responsabilidades**
- **Entities**: Representación de base de datos
- **Domain Models**: Lógica de negocio
- **Repositories**: Conversión y abstracción
- **ViewModels**: Estado de UI y coordinación

### 3. **Inicialización de Base de Datos**
- Usuario admin por defecto: `admin@plantbuddy.com / admin123`
- 7 productos pre-cargados en primera ejecución
- Callback `onCreate` en AppDatabase

### 4. **Preferencias de Usuario**
- DataStore para configuraciones
- Sesiones guardadas (email/password)
- Tema oscuro/claro

---

## 🚀 Uso de ViewModels

### AuthViewModel
```kotlin
// En MainActivity o LoginScreen
val authViewModel: AuthViewModel = viewModel()

// Observar estado
val authState by authViewModel.authState.collectAsState()

// Login
authViewModel.login(email, password, rememberSession = true)

// Register
authViewModel.register(email, password, confirmPassword)

// Logout
authViewModel.logout()
```

### CatalogViewModel
```kotlin
val catalogViewModel: CatalogViewModel = viewModel()
val state by catalogViewModel.estado.collectAsState()

// Buscar
catalogViewModel.onSearchQueryChange(query)

// Filtrar por categoría
catalogViewModel.onCategorySelected("Arbustos")

// Agregar al plantel
catalogViewModel.addToPlantel(product, userId)
```

### PlantelViewModel
```kotlin
val plantelViewModel: PlantelViewModel = viewModel()
val state by plantelViewModel.estado.collectAsState()

// Observar plantas del usuario
plantelViewModel.observePlantelPlants(userId)

// Regar planta
plantelViewModel.waterPlant(plantId)

// Iniciar asistencia
plantelViewModel.startAssistance(plantId)
```

---

## 📝 Próximos Pasos para Integración

### 1. Actualizar Imports en Screens
Todos los archivos en `ui/screens/` necesitan actualizar sus imports:

**Antes:**
```kotlin
import cl.duoc.app.data.model.Product
import cl.duoc.app.data.repository.ProductRepository
import cl.duoc.app.ui.viewmodel.AuthViewModel
```

**Después:**
```kotlin
import cl.duoc.app.model.domain.Product
import cl.duoc.app.model.data.repositories.ProductRepository
import cl.duoc.app.viewmodel.AuthViewModel
```

### 2. Actualizar Inicialización de ViewModels

**Antes:**
```kotlin
val viewModel: AuthViewModel = viewModel { AuthViewModel(context) }
```

**Después:**
```kotlin
val viewModel: AuthViewModel = viewModel()
// AndroidViewModel tiene acceso automático a Application
```

### 3. Sincronizar Gradle
```powershell
.\gradlew clean
.\gradlew build
```

---

## ✅ Ventajas de la Nueva Estructura

1. **Escalabilidad**: Fácil agregar nuevas features
2. **Testabilidad**: Capas separadas y testeables
3. **Mantenibilidad**: Código organizado y predecible
4. **Persistencia**: Datos guardados localmente
5. **Reactividad**: UI se actualiza automáticamente con Flow
6. **Offline-first**: Funciona sin conexión

---

## 🔍 Archivos Antiguos a Eliminar

Una vez verificada la compilación, puedes eliminar:

```
app/src/main/java/cl/duoc/app/data/
├── LoginUIState.kt                    ❌ (movido a model/domain/formularios/)
├── api/
│   ├── ProductApiService.kt          ❌ (no implementado aún)
│   └── RetrofitClient.kt             ✅ (movido a model/data/config/)
├── model/                            ❌ (movido a model/domain/)
│   ├── Category.kt
│   ├── PlantelPlant.kt
│   ├── PlantState.kt
│   ├── Product.kt
│   └── User.kt
├── preferences/                      ✅ (movido a model/data/config/)
│   └── UserPreferences.kt
└── repository/                       ✅ (movido a model/data/repositories/)
    ├── PlantelRepository.kt
    ├── ProductRepository.kt
    └── UserRepository.kt

app/src/main/java/cl/duoc/app/ui/viewmodel/  ❌ (movido a viewmodel/)
├── AuthViewModel.kt
└── SettingsViewModel.kt

app/src/main/java/cl/duoc/app/ui/screens/
├── catalog/
│   ├── CatalogUIState.kt             ❌ (integrado en CatalogViewModel)
│   └── CatalogViewModel.kt           ❌ (movido a viewmodel/)
└── plantel/
    ├── PlantelUIState.kt             ❌ (integrado en PlantelViewModel)
    └── PlantelViewModel.kt           ❌ (movido a viewmodel/)
```

---

## 📚 Recursos de Referencia

- [Room Database - Android](https://developer.android.com/training/data-storage/room)
- [ViewModel - Android](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [StateFlow - Kotlin](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-state-flow/)
- [MVVM Architecture](https://developer.android.com/topic/architecture)

---

**Fecha de reorganización**: Noviembre 2025  
**Versión de Room**: 2.6.1  
**Arquitectura**: MVVM + Repository Pattern + Room Database
