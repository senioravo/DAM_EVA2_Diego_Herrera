# Documentación Técnica - PlantBuddy App

## Índice
1. [Resumen General](#resumen-general)
2. [Arquitectura de la Aplicación](#arquitectura-de-la-aplicación)
3. [Room Database - Arquitectura de Persistencia](#room-database---arquitectura-de-persistencia)
4. [Sistema de Capas y Componentes](#sistema-de-capas-y-componentes)
5. [Estructura de Pantallas](#estructura-de-pantallas)
6. [Sistema de Datos Legado](#sistema-de-datos-legado)
7. [Base de Datos Neon (Backend)](#base-de-datos-neon-backend)
8. [Sistema de Navegación](#sistema-de-navegación)
9. [Configuración de Alto Rendimiento](#configuración-de-alto-rendimiento)
10. [Dependencias y Librerías](#dependencias-y-librerías)
11. [Guía de Mantenimiento](#guía-de-mantenimiento)

---

## Resumen General

**PlantBuddy** es una aplicación Android nativa desarrollada en **Kotlin** utilizando **Jetpack Compose** para la construcción de interfaces de usuario modernas y declarativas. La aplicación está diseñada para gestionar un catálogo de plantas ornamentales con funcionalidades de búsqueda, filtrado por categorías y visualización de productos.

### Tecnologías Principales
- **Lenguaje**: Kotlin 2.0.21
- **Framework UI**: Jetpack Compose con Material Design 3
- **Arquitectura**: MVVM + Clean Architecture (Model-View-ViewModel con capas Domain, Data, UI)
- **Base de Datos Local**: Room Database 2.6.1 con KSP
- **Gestión de Estado**: StateFlow y Coroutines
- **Base de Datos Remota**: Neon Serverless Postgres (preparada para integración backend)
- **HTTP Client**: Retrofit 2.9.0 + OkHttp 4.12.0 (preparado)
- **Navegación**: Navigation Compose 2.7.6
- **Procesamiento de Anotaciones**: KSP (Kotlin Symbol Processing) 2.0.21-1.0.27

### Estado Actual
La aplicación implementa una **arquitectura de tres capas** con Room Database para persistencia local:
1. **Capa de Persistencia (Room)**: Entidades, DAOs y TypeConverters
2. **Capa de Dominio**: Modelos de negocio y estados de UI
3. **Capa de Datos**: Repositorios con conversión Entity ↔ Domain

La infraestructura backend con Neon Postgres está documentada y lista para integración futura.

---

## Arquitectura de la Aplicación

### Clean Architecture + MVVM + Room Database

La aplicación implementa una arquitectura de **tres capas** con separación clara de responsabilidades:

```
┌──────────────────────────────────────────────────────────────┐
│                        UI LAYER                               │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  Composables (CatalogScreen, CartScreen, etc)         │  │
│  │  - Renderizado declarativo con Jetpack Compose        │  │
│  │  - Observa StateFlow desde ViewModels                 │  │
│  └────────────────────┬───────────────────────────────────┘  │
│                       │ Eventos de usuario                    │
│  ┌────────────────────▼───────────────────────────────────┐  │
│  │  ViewModels (CatalogViewModel, CartViewModel, etc)    │  │
│  │  - Gestión de estado UI con StateFlow/MutableState    │  │
│  │  - Lógica de presentación                             │  │
│  │  - Transformación Domain → UI State                   │  │
│  └────────────────────┬───────────────────────────────────┘  │
└───────────────────────┼──────────────────────────────────────┘
                        │ Llamadas a repositorios
┌───────────────────────▼──────────────────────────────────────┐
│                      DOMAIN LAYER                             │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  Domain Models (Product, User, PlantelPlant, etc)     │  │
│  │  - Modelos de negocio puros (sin anotaciones Room)    │  │
│  │  - Representan entidades del dominio de la app        │  │
│  │  - Usados en ViewModels y UI                          │  │
│  └────────────────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  UI States (LoginUIState, RegisterUIState, etc)       │  │
│  │  - Estados de formularios y validaciones              │  │
│  └────────────────────────────────────────────────────────┘  │
└───────────────────────┬──────────────────────────────────────┘
                        │
┌───────────────────────▼──────────────────────────────────────┐
│                       DATA LAYER                              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  Repositories (UserRepository, ProductRepository)     │  │
│  │  - Fuente única de verdad                             │  │
│  │  - Conversión Entity ↔ Domain                         │  │
│  │  - Expone Flow<Domain> y suspend functions            │  │
│  └────────────────────┬───────────────────────────────────┘  │
│                       │ Operaciones CRUD                      │
│  ┌────────────────────▼───────────────────────────────────┐  │
│  │  DAOs (UserDao, ProductDao, PlantelPlantDao, etc)     │  │
│  │  - Interfaces con @Query, @Insert, @Update, @Delete   │  │
│  │  - Retornan Flow<Entity> para reactividad             │  │
│  └────────────────────┬───────────────────────────────────┘  │
│                       │ SQL Queries                           │
│  ┌────────────────────▼───────────────────────────────────┐  │
│  │  Room Database (AppDatabase)                          │  │
│  │  - Singleton con 4 tablas                             │  │
│  │  - TypeConverters para Date ↔ Long                    │  │
│  │  - Versión 1, nombre: "plant_buddy_db"                │  │
│  └────────────────────┬───────────────────────────────────┘  │
│                       │                                       │
│  ┌────────────────────▼───────────────────────────────────┐  │
│  │  Entities (UserEntity, ProductEntity, etc)            │  │
│  │  - Representación de tablas SQLite                    │  │
│  │  - Anotaciones @Entity, @PrimaryKey, @ForeignKey      │  │
│  │  - Mapeo directo a esquema de base de datos           │  │
│  └────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

### Flujo de Datos Unidireccional

#### Lectura de Datos (Query)
1. **UI** observa StateFlow desde **ViewModel**
2. **ViewModel** llama a **Repository** (suspend function o Flow)
3. **Repository** solicita datos al **DAO** → Recibe `Flow<Entity>`
4. **Repository** convierte `Entity` → `Domain` usando extension functions
5. **Repository** retorna `Flow<Domain>` al **ViewModel**
6. **ViewModel** actualiza **StateFlow** → UI recompone automáticamente

#### Escritura de Datos (Insert/Update/Delete)
1. **UI** envía evento al **ViewModel** (ej: onAddToCart())
2. **ViewModel** llama a **Repository** (suspend function)
3. **Repository** convierte `Domain` → `Entity`
4. **Repository** llama a **DAO.insert/update/delete**
5. **DAO** ejecuta operación SQL en Room Database
6. **Room** notifica cambios → Flow se actualiza automáticamente
7. **UI** recompone con nuevos datos

---

## Room Database - Arquitectura de Persistencia

### ¿Qué es Room Database?

**Room** es la biblioteca de persistencia oficial de Android que proporciona una capa de abstracción sobre SQLite. Facilita:
- Validación de queries SQL en tiempo de compilación
- Conversión automática entre objetos Kotlin y filas de base de datos
- Soporte para LiveData y Flow (reactividad)
- Manejo de migraciones de esquema

### Arquitectura de Room en PlantBuddy

```
model/
├── data/
│   ├── config/
│   │   ├── AppDatabase.kt          # Singleton de Room Database
│   │   └── Converters.kt           # TypeConverters (Date ↔ Long)
│   ├── entities/
│   │   ├── UserEntity.kt           # @Entity - Tabla users
│   │   ├── ProductEntity.kt        # @Entity - Tabla products
│   │   ├── PlantelPlantEntity.kt   # @Entity - Tabla plantel_plants
│   │   └── PurchaseEntity.kt       # @Entity - Tabla purchases
│   ├── dao/
│   │   ├── UserDao.kt              # @Dao - CRUD operations usuarios
│   │   ├── ProductDao.kt           # @Dao - CRUD operations productos
│   │   ├── PlantelPlantDao.kt      # @Dao - CRUD operations plantel
│   │   └── PurchaseDao.kt          # @Dao - CRUD operations compras
│   └── repositories/
│       ├── UserRepository.kt       # Conversión Entity ↔ Domain
│       ├── ProductRepository.kt    # Conversión Entity ↔ Domain
│       └── PlantelRepository.kt    # Conversión Entity ↔ Domain
└── domain/
    ├── User.kt                     # Modelo de negocio
    ├── Product.kt                  # Modelo de negocio
    ├── PlantelPlant.kt             # Modelo de negocio
    ├── Purchase.kt                 # Modelo de negocio con enums
    ├── CartItem.kt                 # Modelo para carrito
    ├── LoginUIState.kt             # Estado de formulario login
    └── RegisterUIState.kt          # Estado de formulario registro
```

---

### Componentes de Room

#### 1. AppDatabase.kt - Base de Datos Singleton

**Ubicación**: `model/data/config/AppDatabase.kt`

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
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun plantelPlantDao(): PlantelPlantDao
    abstract fun purchaseDao(): PurchaseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "plant_buddy_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

**Características**:
- **Singleton Pattern**: Una sola instancia en toda la aplicación
- **4 Entidades**: Users, Products, PlantelPlants, Purchases
- **TypeConverters**: Conversión automática de Date ↔ Long
- **Thread-safe**: Uso de `@Volatile` y `synchronized`
- **Nombre de BD**: `plant_buddy_db.db` en storage interno

---

#### 2. TypeConverters - Conversión de Tipos

**Ubicación**: `model/data/config/Converters.kt`

```kotlin
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
```

**Propósito**: Room no puede almacenar `Date` directamente. Los TypeConverters convierten automáticamente:
- `Date` → `Long` al guardar (timestamp)
- `Long` → `Date` al leer

---

#### 3. Entities - Representación de Tablas

##### UserEntity.kt

**Ubicación**: `model/data/entities/UserEntity.kt`

```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val email: String,
    val password: String,
    val profileImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
```

**Tabla SQL generada**:
```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    username TEXT NOT NULL,
    email TEXT NOT NULL,
    password TEXT NOT NULL,
    profileImageUrl TEXT,
    createdAt INTEGER NOT NULL
)
```

**Anotaciones**:
- `@Entity(tableName = "users")`: Define la tabla
- `@PrimaryKey(autoGenerate = true)`: ID autoincrementable
- Campos opcionales con `?` permiten NULL en SQLite

---

##### ProductEntity.kt

```kotlin
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String,
    val stock: Int = 0,
    val rating: Double = 0.0
)
```

---

##### PlantelPlantEntity.kt (con Foreign Key)

```kotlin
@Entity(
    tableName = "plantel_plants",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlantelPlantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val productId: Int,
    val plantName: String,
    val plantDescription: String?,
    val plantImageUrl: String?,
    val addedAt: Long = System.currentTimeMillis(),
    val lastWateredDate: Long? = null,
    val wateringFrequencyDays: Int = 7,
    val notes: String? = null
)
```

**Foreign Key**:
- `userId` referencia a `users.id`
- `onDelete = CASCADE`: Si se elimina el usuario, se eliminan sus plantas automáticamente

---

##### PurchaseEntity.kt

```kotlin
@Entity(
    tableName = "purchases",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val total: Double,
    val shippingAddress: String,
    val paymentMethod: String,  // Guardado como String en SQLite
    val status: String,          // Guardado como String en SQLite
    val createdAt: Long = System.currentTimeMillis()
)
```

---

#### 4. DAOs - Data Access Objects

Los DAOs son interfaces que definen las operaciones de base de datos. Room genera la implementación automáticamente.

##### UserDao.kt

**Ubicación**: `model/data/dao/UserDao.kt`

```kotlin
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: Int): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    suspend fun emailExists(email: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username)")
    suspend fun usernameExists(username: String): Boolean
}
```

**Tipos de Retorno**:
- `Flow<T>`: Para observación reactiva (recompone UI automáticamente)
- `suspend fun`: Para operaciones únicas (insert, update, delete)
- `Boolean`: Para validaciones (emailExists, usernameExists)

**Estrategias de Conflicto**:
- `OnConflictStrategy.ABORT`: Lanza excepción si existe (para registros únicos)
- `OnConflictStrategy.REPLACE`: Sobrescribe el registro existente
- `OnConflictStrategy.IGNORE`: No hace nada si existe

---

##### ProductDao.kt

```kotlin
@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :productId")
    fun getProductById(productId: Int): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE category = :category")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :searchQuery || '%'")
    fun searchProducts(searchQuery: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products")
    suspend fun deleteAll()
}
```

**Búsqueda con LIKE**:
```kotlin
@Query("SELECT * FROM products WHERE name LIKE '%' || :searchQuery || '%'")
```
- `||` es el operador de concatenación en SQLite
- `'%text%'` busca "text" en cualquier posición

---

##### PlantelPlantDao.kt

```kotlin
@Dao
interface PlantelPlantDao {
    @Query("SELECT * FROM plantel_plants WHERE userId = :userId ORDER BY addedAt DESC")
    fun getUserPlants(userId: Int): Flow<List<PlantelPlantEntity>>

    @Query("SELECT * FROM plantel_plants WHERE id = :plantId")
    fun getPlantById(plantId: Int): Flow<PlantelPlantEntity?>

    @Query("SELECT * FROM plantel_plants WHERE userId = :userId AND productId = :productId LIMIT 1")
    suspend fun getPlantByUserAndProduct(userId: Int, productId: Int): PlantelPlantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: PlantelPlantEntity)

    @Update
    suspend fun updatePlant(plant: PlantelPlantEntity)

    @Delete
    suspend fun deletePlant(plant: PlantelPlantEntity)

    @Query("UPDATE plantel_plants SET lastWateredDate = :date WHERE id = :plantId")
    suspend fun updateLastWatered(plantId: Int, date: Long)

    @Query("DELETE FROM plantel_plants WHERE userId = :userId")
    suspend fun deleteAllUserPlants(userId: Int)
}
```

**Ordenamiento**:
```kotlin
ORDER BY addedAt DESC  // Más recientes primero
```

---

##### PurchaseDao.kt

```kotlin
@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserPurchases(userId: Int): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE id = :purchaseId")
    fun getPurchaseById(purchaseId: Int): Flow<PurchaseEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    @Update
    suspend fun updatePurchase(purchase: PurchaseEntity)

    @Delete
    suspend fun deletePurchase(purchase: PurchaseEntity)

    @Query("DELETE FROM purchases WHERE userId = :userId")
    suspend fun deleteAllUserPurchases(userId: Int)
}
```

---

#### 5. Domain Models - Modelos de Negocio

Los modelos de dominio son clases puras sin anotaciones de Room. Se usan en ViewModels y UI.

##### User.kt (Domain)

**Ubicación**: `model/domain/User.kt`

```kotlin
data class User(
    val id: Int = 0,
    val username: String,
    val email: String,
    val password: String = "",
    val profileImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
```

**Diferencia con UserEntity**:
- Sin anotaciones `@Entity`, `@PrimaryKey`
- Puede tener propiedades computadas
- Representa el modelo de negocio, no la persistencia

---

##### Product.kt (Domain)

```kotlin
data class Product(
    val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String,
    val stock: Int = 0,
    val rating: Double = 0.0
)
```

---

##### Purchase.kt (Domain con Enums)

```kotlin
enum class PaymentMethod {
    DEBIT_CARD,
    CREDIT_CARD
}

enum class PurchaseStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    CANCELLED
}

data class Purchase(
    val id: Int = 0,
    val userId: Int,
    val total: Double,
    val shippingAddress: String,
    val paymentMethod: PaymentMethod,
    val status: PurchaseStatus,
    val createdAt: Date = Date()
)
```

**Nota**: Los enums se guardan como `String` en SQLite y se convierten en el Repository.

---

##### CartItem.kt (Domain específico de UI)

```kotlin
data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val subtotal: Double
        get() = product.price * quantity
}
```

**Propiedad Computada**: `subtotal` se calcula automáticamente.

---

##### LoginUIState.kt y RegisterUIState.kt

Estados para formularios de autenticación:

```kotlin
data class LoginUIState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val loginError: String? = null
)

data class RegisterUIState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val registerError: String? = null
)
```

---

#### 6. Repositories - Capa de Abstracción

Los repositorios convierten entre `Entity` (base de datos) y `Domain` (negocio).

##### UserRepository.kt

**Ubicación**: `model/data/repositories/UserRepository.kt`

```kotlin
class UserRepository(private val userDao: UserDao) {

    fun getUserById(userId: Int): Flow<User?> {
        return userDao.getUserById(userId).map { it?.toDomain() }
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)?.toDomain()
    }

    suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user.toEntity())
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user.toEntity())
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user.toEntity())
    }

    suspend fun emailExists(email: String): Boolean {
        return userDao.emailExists(email)
    }

    suspend fun usernameExists(username: String): Boolean {
        return userDao.usernameExists(username)
    }

    // Extension functions para conversión
    private fun UserEntity.toDomain(): User {
        return User(
            id = id,
            username = username,
            email = email,
            password = password,
            profileImageUrl = profileImageUrl,
            createdAt = createdAt
        )
    }

    private fun User.toEntity(): UserEntity {
        return UserEntity(
            id = id,
            username = username,
            email = email,
            password = password,
            profileImageUrl = profileImageUrl,
            createdAt = createdAt
        )
    }
}
```

**Patrón de Conversión**:
- **Entity → Domain**: `entity.toDomain()`
- **Domain → Entity**: `domain.toEntity()`
- Extension functions privadas dentro del Repository

---

##### ProductRepository.kt

```kotlin
class ProductRepository(private val productDao: ProductDao) {

    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getProductById(productId: Int): Flow<Product?> {
        return productDao.getProductById(productId).map { it?.toDomain() }
    }

    fun getProductsByCategory(category: String): Flow<List<Product>> {
        return productDao.getProductsByCategory(category).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun searchProducts(searchQuery: String): Flow<List<Product>> {
        return productDao.searchProducts(searchQuery).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun insertProduct(product: Product) {
        productDao.insertProduct(product.toEntity())
    }

    suspend fun insertAll(products: List<Product>) {
        productDao.insertAll(products.map { it.toEntity() })
    }

    suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product.toEntity())
    }

    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product.toEntity())
    }

    suspend fun deleteAll() {
        productDao.deleteAll()
    }

    private fun ProductEntity.toDomain(): Product {
        return Product(
            id = id,
            name = name,
            description = description,
            price = price,
            imageUrl = imageUrl,
            category = category,
            stock = stock,
            rating = rating
        )
    }

    private fun Product.toEntity(): ProductEntity {
        return ProductEntity(
            id = id,
            name = name,
            description = description,
            price = price,
            imageUrl = imageUrl,
            category = category,
            stock = stock,
            rating = rating
        )
    }
}
```

---

### Uso en ViewModels

#### Ejemplo: CatalogViewModel con Repository

```kotlin
class CatalogViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(CatalogUIState())
    val estado: StateFlow<CatalogUIState> = _estado.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            productRepository.getAllProducts()
                .catch { e ->
                    _estado.update { it.copy(error = e.message) }
                }
                .collect { products ->
                    _estado.update {
                        it.copy(
                            products = products,
                            filteredProducts = products,
                            isLoading = false
                        )
                    }
                }
        }
    }
}
```

**Flow Reactivo**:
1. `productRepository.getAllProducts()` retorna `Flow<List<Product>>`
2. `.collect { }` observa cambios automáticamente
3. Cuando Room actualiza datos, Flow emite nuevo valor
4. StateFlow se actualiza → UI recompone

---

### Ventajas de esta Arquitectura

#### 1. Separación de Responsabilidades
- **Entity**: Representa tabla SQLite (anotaciones Room)
- **Domain**: Representa modelo de negocio (sin anotaciones)
- **Repository**: Conversión y lógica de acceso a datos
- **ViewModel**: Lógica de presentación
- **UI**: Solo renderizado

#### 2. Testabilidad
```kotlin
// Mock del Repository sin tocar Room
class FakeProductRepository : ProductRepository {
    override fun getAllProducts(): Flow<List<Product>> {
        return flowOf(listOf(
            Product(id = 1, name = "Test Product", ...)
        ))
    }
}
```

#### 3. Reactividad Automática
- Room emite nuevos valores en Flow cuando cambian los datos
- No es necesario refrescar manualmente
- UI siempre sincronizada con base de datos

#### 4. Thread Safety
- `suspend fun`: Operaciones se ejecutan en background automáticamente
- `Flow`: Emisiones en dispatcher IO por defecto
- Room maneja threading internamente

#### 5. Type Safety
- Queries validados en tiempo de compilación
- Errores de SQL se detectan antes de ejecutar la app
- KSP genera código optimizado

---

### Migración de Datos (Futuro)

Cuando se necesite actualizar el esquema (ej: agregar columna):

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE products ADD COLUMN discount REAL DEFAULT 0.0"
        )
    }
}

// En AppDatabase
Room.databaseBuilder(...)
    .addMigrations(MIGRATION_1_2)
    .build()
```

---

## Sistema de Capas y Componentes

### Resumen de Responsabilidades

| Capa | Componente | Responsabilidad | Ejemplo |
|------|-----------|-----------------|---------|
| **UI** | Composables | Renderizado declarativo | `CatalogScreen.kt` |
| **UI** | ViewModels | Estado UI y lógica de presentación | `CatalogViewModel.kt` |
| **Domain** | Models | Representación de entidades de negocio | `Product.kt`, `User.kt` |
| **Domain** | UI States | Estados de formularios y validaciones | `LoginUIState.kt` |
| **Data** | Repositories | Conversión Entity ↔ Domain, fuente única de verdad | `ProductRepository.kt` |
| **Data** | DAOs | Operaciones CRUD con SQL | `ProductDao.kt` |
| **Data** | Entities | Mapeo a tablas SQLite | `ProductEntity.kt` |
| **Data** | Database | Singleton Room DB | `AppDatabase.kt` |
| **Data** | Converters | Conversión de tipos complejos | `Converters.kt` (Date ↔ Long) |

---

## Estructura de Pantallas

### 1. MainActivity.kt

**Propósito**: Punto de entrada de la aplicación y configuración de rendimiento.

**Funcionalidades**:
- Configuración de alta tasa de refresco (120Hz)
- Inicialización del sistema de navegación
- Configuración de tema Material 3 con soporte oscuro/claro

**Código Clave**:
```kotlin
private fun setupHighRefreshRate() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val display = windowManager.defaultDisplay
        val refreshRates = display.supportedModes
        // Intenta seleccionar el modo con mayor refresh rate
        val highestRefreshRateMode = refreshRates.maxByOrNull { it.refreshRate }
        // Configura el modo preferido
        window.attributes.preferredDisplayModeId = highestRefreshRateMode?.modeId ?: 0
    }
}
```

**Nota Importante**: En dispositivos Xiaomi (MIUI), puede ser necesario habilitar 120Hz manualmente desde:
`Ajustes → Pantalla → Frecuencia de actualización → Establecer frecuencia por app`

---

### 2. HomeScreen.kt

**Propósito**: Pantalla principal de bienvenida.

**Componentes**:
- Mensaje de bienvenida
- Estadísticas básicas (pendiente de implementación)
- Accesos rápidos a otras secciones

**Estado Actual**: Pantalla básica con ViewModel preparado para expansión futura.

---

### 3. CatalogScreen.kt

**Propósito**: Catálogo completo de productos con búsqueda y filtros.

#### Componentes UI Principales

##### SearchBar
- **Ubicación**: Parte superior de la pantalla
- **Funcionalidad**: Búsqueda en tiempo real por nombre de producto
- **Diseño**: TextField con fondo personalizado, esquinas redondeadas (12.dp)
- **Icono**: Lupa (Icons.Default.Search)

```kotlin
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.9f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp)
    )
}
```

##### CategoryFilters
- **Ubicación**: Debajo del SearchBar
- **Funcionalidad**: Filtro por categorías con chips interactivos
- **Diseño**: LazyRow horizontal con 6 categorías
- **Categorías Disponibles**:
  - 🌿 Todas
  - 🌳 Arbustos
  - 🌸 Perennes
  - 🍃 Aromáticas
  - 🌺 Ornamentales
  - 🌿 Trepadoras

```kotlin
@Composable
private fun CategoryFilters(
    categories: List<Category>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("🌿 Todas") }
            )
        }
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category.name,
                onClick = { onCategorySelected(category.name) },
                label = { Text("${category.icon} ${category.name}") }
            )
        }
    }
}
```

##### ProductGrid
- **Ubicación**: Cuerpo principal de la pantalla
- **Funcionalidad**: Visualización de productos en grilla
- **Diseño**: LazyVerticalGrid con 2 columnas
- **Espaciado**: 12.dp entre elementos

##### ProductCard
- **Dimensiones**: 280.dp altura total
- **Estructura**:
  - **Imagen**: 160.dp altura, ContentScale.Fit (sin recorte)
  - **Sección de información**: Fondo con color `surface` de Material Theme
    - Nombre del producto (Medium weight)
    - Precio en pesos chilenos (CLP)
    - Rating con estrellas (pendiente datos reales)

```kotlin
@Composable
private fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Imagen del producto (160.dp)
            ProductImage(
                imageUrl = product.imageUrl,
                contentDescription = product.name
            )
            
            // Información con fondo surface
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$${String.format("%,d", product.price)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    // Rating con estrellas
                    Row {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "%.1f".format(product.rating),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
```

##### ProductImage
- **Sistema de carga**: Recursos locales desde `drawable`
- **Optimización**: `remember()` para cachear el resourceId
- **Escalado**: `ContentScale.Fit` para mostrar imagen completa sin recortes
- **Fallback**: Color de fondo si la imagen no existe

```kotlin
@Composable
private fun ProductImage(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resourceId = remember(imageUrl) {
        context.resources.getIdentifier(
            imageUrl.substringAfterLast("/").substringBeforeLast("."),
            "drawable",
            context.packageName
        )
    }

    if (resourceId != 0) {
        Image(
            painter = painterResource(id = resourceId),
            contentDescription = contentDescription,
            modifier = modifier
                .fillMaxWidth()
                .height(160.dp),
            contentScale = ContentScale.Fit
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(Color.LightGray)
        )
    }
}
```

#### Estados de la Pantalla

1. **Loading**: Indicador circular en el centro
2. **Success**: Grilla de productos con búsqueda/filtros activos
3. **Empty**: Mensaje cuando no hay resultados
4. **Error**: Mensaje de error con botón de reintento

---

### 4. CatalogViewModel.kt

**Propósito**: Gestión de estado y lógica de negocio del catálogo.

#### StateFlow Principal

```kotlin
data class CatalogUIState(
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
```

#### Métodos Principales

**onSearchQueryChange(query: String)**
- Actualiza el texto de búsqueda
- Dispara `filterProducts()` automáticamente
- Búsqueda insensible a mayúsculas/minúsculas

**onCategorySelected(category: String?)**
- Actualiza la categoría seleccionada
- `null` = Todas las categorías
- Dispara `filterProducts()` automáticamente

**filterProducts()**
- Combina búsqueda por nombre + filtro por categoría
- Lógica:
  1. Si hay categoría seleccionada → filtra por categoría
  2. Si hay texto de búsqueda → filtra por nombre (contains)
  3. Si ambos → aplica ambos filtros
  4. Si ninguno → muestra todos los productos

```kotlin
private fun filterProducts() {
    val currentState = _estado.value
    val filtered = currentState.products
        .filter { product ->
            val matchesCategory = currentState.selectedCategory == null || 
                                product.category == currentState.selectedCategory
            val matchesSearch = product.name.contains(currentState.searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    
    _estado.value = currentState.copy(filteredProducts = filtered)
}
```

**retry()**
- Recarga los datos desde el repositorio
- Manejo de errores con try-catch

#### Scroll Collapse en Toolbar (CatalogScreen)

Implementación de comportamiento de ocultación automática del toolbar (búsqueda, filtros y categorías) al hacer scroll hacia abajo en el catálogo de productos. El toolbar reaparece al hacer scroll hacia arriba.

**Componentes**: `CatalogScreen.kt`, `CatalogViewModel.kt`, `CatalogUIState.kt`

**Implementación técnica**:

**1. Estado en CatalogUIState.kt**:
```kotlin
data class CatalogUIState(
    // ... otros campos
    val toolbarOffset: Float = 0f,        // Offset actual del toolbar (0-200dp)
    val lastScrollIndex: Int = 0,          // Último índice visible del grid
    val lastScrollOffset: Int = 0          // Último offset del scroll
)
```

**2. Lógica en CatalogViewModel.kt**:
```kotlin
fun onScrollPositionChange(firstVisibleIndex: Int, firstVisibleOffset: Int) {
    val currentState = _estado.value
    val previousIndex = currentState.lastScrollIndex
    val previousOffset = currentState.lastScrollOffset
    
    // Detectar dirección del scroll
    val isScrollingDown = if (firstVisibleIndex != previousIndex) {
        firstVisibleIndex > previousIndex
    } else {
        firstVisibleOffset > previousOffset
    }
    
    // Calcular nuevo offset del toolbar (máximo 200dp)
    val maxOffset = 200f
    val newOffset = if (isScrollingDown) {
        (currentState.toolbarOffset + 10f).coerceAtMost(maxOffset)
    } else {
        (currentState.toolbarOffset - 10f).coerceAtLeast(0f)
    }
    
    _estado.update {
        it.copy(
            toolbarOffset = newOffset,
            lastScrollIndex = firstVisibleIndex,
            lastScrollOffset = firstVisibleOffset
        )
    }
}
```

**3. UI en CatalogScreen.kt**:
```kotlin
val gridState = rememberLazyGridState()
val toolbarOffsetDp by animateDpAsState(
    targetValue = (-estado.toolbarOffset).dp,
    label = "toolbarOffset"
)

// Observar cambios en el scroll
LaunchedEffect(gridState) {
    snapshotFlow {
        Pair(
            gridState.firstVisibleItemIndex,
            gridState.firstVisibleItemScrollOffset
        )
    }
        .distinctUntilChanged()
        .collect { (index, offset) ->
            viewModel.onScrollPositionChange(index, offset)
        }
}

// Aplicar offset al toolbar
Column(
    modifier = Modifier
        .fillMaxWidth()
        .offset(y = toolbarOffsetDp)
        .background(...)
        .padding(16.dp)
) {
    // SearchBar, CategoryFilters...
}
```

**Comportamiento**:
- **Scroll hacia abajo**: Toolbar se oculta progresivamente hacia arriba (máximo 200dp)
- **Scroll hacia arriba**: Toolbar reaparece con animación suave
- **Animación**: `animateDpAsState` proporciona transición fluida
- **Detección**: `LazyGridState` con `snapshotFlow` detecta cambios en posición del scroll
- **Performance**: `distinctUntilChanged()` evita recomposiciones innecesarias

**APIs utilizadas**:
- `LazyGridState`: Estado del scroll del LazyVerticalGrid
- `rememberLazyGridState()`: Recordar estado entre recomposiciones
- `snapshotFlow`: Convertir estado de Compose a Flow reactivo
- `animateDpAsState`: Animación declarativa de valores Dp
- `Modifier.offset()`: Desplazamiento del componente UI

---

## Sistema de Datos

### Modelos de Datos

#### Product.kt

```kotlin
data class Product(
    val id: Int,
    val name: String,
    val description: String,
    val price: Int,
    val category: String,
    val imageUrl: String,
    val stock: Int = 0,
    val rating: Float = 0f
)
```

**Campos**:
- `id`: Identificador único del producto
- `name`: Nombre comercial de la planta
- `description`: Descripción detallada (uso, características)
- `price`: Precio en pesos chilenos (Int sin decimales)
- `category`: Categoría de la planta (Arbustos, Perennes, etc.)
- `imageUrl`: Referencia al recurso drawable (ej: "viburnum_lucidum")
- `stock`: Cantidad disponible (preparado para inventario futuro)
- `rating`: Calificación de 0.0 a 5.0 (preparado para reviews)

#### Category.kt

```kotlin
data class Category(
    val id: Int,
    val name: String,
    val icon: String
)
```

---

### ProductRepository.kt

**Propósito**: Fuente única de verdad para los datos de productos.

#### Productos Actuales (Mock Data)

| ID | Nombre                    | Precio    | Categoría    | Imagen                   |
|----|---------------------------|-----------|--------------|--------------------------|
| 1  | Viburnum Lucidum         | $24,990   | Arbustos     | viburnum_lucidum         |
| 2  | Kniphofia Uvaria         | $19,990   | Perennes     | kniphofia_uvaria         |
| 3  | Rhus Crenata             | $17,990   | Arbustos     | rhus_crenata             |
| 4  | Lavanda Dentata          | $15,990   | Aromáticas   | lavanda_dentata          |
| 5  | Laurel de Flor Enano     | $13,990   | Ornamentales | laurel_flor_enano        |
| 6  | Pitosporo Tobira Enano   | $16,990   | Arbustos     | pitosporo_tobira_enano   |
| 7  | Bignonia Naranja         | $21,990   | Trepadoras   | bignonia_naranja         |

#### Métodos del Repositorio

**getProducts(): List<Product>**
- Retorna la lista completa de productos
- Simula delay de red (500ms)

**searchProducts(query: String): List<Product>**
- Búsqueda por nombre
- Insensible a mayúsculas/minúsculas

**getProductsByCategory(category: String): List<Product>**
- Filtra por categoría específica

**observeProducts(): Flow<List<Product>>**
- Emite la lista como Flow para observación reactiva

---

### Sistema de Imágenes

#### Ubicación de Archivos

Las imágenes deben colocarse en:
```
app/src/main/res/drawable/
```

#### Nombres de Archivo Requeridos

1. `viburnum_lucidum.jpg` (o .png, .webp)
2. `kniphofia_uvaria.jpg`
3. `rhus_crenata.jpg`
4. `lavanda_dentata.jpg`
5. `laurel_flor_enano.jpg`
6. `pitosporo_tobira_enano.jpg`
7. `bignonia_naranja.jpg`

#### Convención de Nombres

- **Minúsculas**: Todos los nombres en lowercase
- **Separador**: Guión bajo `_` en lugar de espacios
- **Extensión**: `.jpg`, `.png` o `.webp`
- **Sin acentos**: No usar caracteres especiales (ej: `enano` no `eñano`)

#### Proceso de Carga

1. `ProductImage` composable recibe `imageUrl` (ej: "viburnum_lucidum")
2. `getIdentifier()` busca el recurso en el paquete
3. Si existe → `Image()` con `painterResource()`
4. Si no existe → `Box` con fondo gris como placeholder

---

## Sistema de Datos Legado

### Repositorios y Modelos Antiguos

**Nota**: Los siguientes archivos en `data/` son parte de la implementación anterior y coexisten con la nueva arquitectura Room:

**Ubicación**: `app/src/main/java/cl/duoc/app/data/`

- `data/model/`: Modelos antiguos (Product, User, Category, CartItem, etc)
- `data/repository/`: Repositorios con datos mock (ProductRepository, CartRepository, UserRepository, PlantelRepository)
- `data/api/`: Servicios REST preparados (ProductApiService, RetrofitClient)
- `data/preferences/`: DataStore para preferencias (UserPreferences)

Estos archivos se mantendrán hasta que todos los ViewModels migren a la nueva arquitectura Room.

---

## Base de Datos Neon (Backend)

### Neon Serverless Postgres

**Conexión**:
```
postgresql://neondb_owner:npg_R7m8bHdfNyLW@ep-rapid-rice-a3hr3zr8-pooler.sa-east-1.aws.neon.tech/neondb?sslmode=require
```

### Esquema de Base de Datos

#### Estructura General

```
catalogo (schema)
  ├── categorias (tabla)
  ├── productos (tabla)
  ├── plantas_detalle (tabla)
  ├── idx_productos_categoria (índice)
  ├── idx_productos_precio (índice)
  ├── v_productos_completos (vista)
  └── buscar_productos() (función)
```

---

#### Tabla: catalogo.categorias

```sql
CREATE TABLE catalogo.categorias (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    icono VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Registros**:
- Arbustos
- Perennes
- Aromáticas
- Ornamentales
- Trepadoras

---

#### Tabla: catalogo.productos

```sql
CREATE TABLE catalogo.productos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL,
    categoria_id INTEGER REFERENCES catalogo.categorias(id),
    imagen_url VARCHAR(500),
    stock INTEGER DEFAULT 0,
    rating DECIMAL(3,2) DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Índices**:
```sql
CREATE INDEX idx_productos_categoria ON catalogo.productos(categoria_id);
CREATE INDEX idx_productos_precio ON catalogo.productos(precio);
```

---

#### Tabla: catalogo.plantas_detalle

```sql
CREATE TABLE catalogo.plantas_detalle (
    id SERIAL PRIMARY KEY,
    producto_id INTEGER REFERENCES catalogo.productos(id) ON DELETE CASCADE,
    nombre_cientifico VARCHAR(200),
    familia VARCHAR(100),
    origen VARCHAR(200),
    tipo_riego VARCHAR(50),
    exposicion_solar VARCHAR(100),
    altura_maxima VARCHAR(50),
    tipo_suelo TEXT,
    epoca_floracion VARCHAR(100),
    color_flor VARCHAR(100),
    crecimiento VARCHAR(50),
    rusticidad VARCHAR(100),
    usos TEXT
);
```

---

#### Vista: v_productos_completos

Combina datos de las 3 tablas para consultas completas:

```sql
CREATE VIEW catalogo.v_productos_completos AS
SELECT 
    p.id,
    p.nombre,
    p.descripcion,
    p.precio,
    c.nombre AS categoria,
    p.imagen_url,
    p.stock,
    p.rating,
    pd.nombre_cientifico,
    pd.familia,
    pd.origen,
    pd.tipo_riego,
    pd.exposicion_solar,
    pd.altura_maxima,
    pd.epoca_floracion,
    pd.color_flor
FROM catalogo.productos p
LEFT JOIN catalogo.categorias c ON p.categoria_id = c.id
LEFT JOIN catalogo.plantas_detalle pd ON p.id = pd.producto_id;
```

---

#### Función: buscar_productos()

```sql
CREATE OR REPLACE FUNCTION catalogo.buscar_productos(
    p_busqueda TEXT DEFAULT NULL,
    p_categoria TEXT DEFAULT NULL,
    p_precio_min DECIMAL DEFAULT NULL,
    p_precio_max DECIMAL DEFAULT NULL
)
RETURNS TABLE (
    id INTEGER,
    nombre VARCHAR,
    descripcion TEXT,
    precio DECIMAL,
    categoria VARCHAR,
    imagen_url VARCHAR,
    stock INTEGER,
    rating DECIMAL
)
```

**Parámetros**:
- `p_busqueda`: Texto de búsqueda (nombre o descripción)
- `p_categoria`: Filtro por categoría
- `p_precio_min` / `p_precio_max`: Rango de precios

---

### Datos de Ejemplo (7 Productos)

Los productos están insertados con `ON CONFLICT DO NOTHING` para evitar duplicados:

1. **Viburnum Lucidum** ($24,990) - Arbusto perenne para cercos
2. **Kniphofia Uvaria** ($19,990) - Perenne con flores en forma de antorcha
3. **Rhus Crenata** ($17,990) - Arbusto nativo resistente
4. **Lavanda Dentata** ($15,990) - Aromática con flores violetas
5. **Laurel de Flor Enano** ($13,990) - Ornamental compacto
6. **Pitosporo Tobira Enano** ($16,990) - Arbusto para setos bajos
7. **Bignonia Naranja** ($21,990) - Trepadora con flores naranjas

---

## Sistema de Navegación

### Navigation.kt

**Estructura de Rutas**:

```kotlin
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Catalogo : Screen("catalogo")
    object Plantel : Screen("plantel")
    object Ajustes : Screen("ajustes")
}
```

### Grafo de Navegación

```
Login (startDestination)
  └─→ Home
       ├─→ Catalogo
       ├─→ Plantel
       └─→ Ajustes
```

### Comportamiento de Navegación

- **Login → Home**: Al autenticarse, elimina Login del back stack (`popUpTo`)
- **Home ↔ Otras pantallas**: Navegación libre con Bottom Navigation Bar
- **Back button**: En Home, sale de la app; en otras pantallas, vuelve a Home

---

## Configuración de Alto Rendimiento

### 120Hz Refresh Rate

#### Implementación en MainActivity

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setupHighRefreshRate()
    // ...
}

private fun setupHighRefreshRate() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val display = windowManager.defaultDisplay
        val refreshRates = display.supportedModes
        
        Log.d("RefreshRate", "Modos disponibles:")
        refreshRates.forEach { mode ->
            Log.d("RefreshRate", "Modo: ${mode.modeId}, Refresh Rate: ${mode.refreshRate}Hz")
        }

        val highestRefreshRateMode = refreshRates.maxByOrNull { it.refreshRate }
        
        if (highestRefreshRateMode != null) {
            val layoutParams = window.attributes
            layoutParams.preferredDisplayModeId = highestRefreshRateMode.modeId
            window.attributes = layoutParams
            
            Log.d("RefreshRate", "Configurado a: ${highestRefreshRateMode.refreshRate}Hz")
        }
    }
}
```

#### Configuración en AndroidManifest.xml

```xml
<application
    android:hardwareAccelerated="true"
    ...>
    
    <activity
        android:name=".MainActivity"
        android:configChanges="screenSize|orientation|screenLayout"
        android:exported="true">
        
        <meta-data
            android:name="android.max_aspect"
            android:value="2.4" />
    </activity>
</application>
```

#### Limitaciones en MIUI (Xiaomi)

**Problema**: MIUI puede sobrescribir la configuración de la app.

**Solución Manual**:
1. Ir a `Configuración` → `Pantalla` → `Frecuencia de actualización`
2. Seleccionar `Establecer frecuencia por aplicación`
3. Buscar **PlantBuddy** en la lista
4. Establecer a **120Hz**

#### Verificación en Logcat

Para confirmar que la configuración funciona:

```bash
adb logcat | grep RefreshRate
```

Salida esperada:
```
RefreshRate: Modos disponibles:
RefreshRate: Modo: 1, Refresh Rate: 60.0Hz
RefreshRate: Modo: 2, Refresh Rate: 90.0Hz
RefreshRate: Modo: 3, Refresh Rate: 120.0Hz
RefreshRate: Configurado a: 120.0Hz
```

---

## Integración Backend (Futuro)

### Arquitectura Propuesta

```
Android App (Kotlin)
      ↓ HTTP/REST
Backend API (Node.js/Express)
      ↓ SQL
Neon Postgres Database
```

---

### Backend API (Preparado)

#### ProductApiService.kt

```kotlin
interface ProductApiService {
    @GET("productos")
    suspend fun getProducts(): List<Product>

    @GET("productos/search")
    suspend fun searchProducts(@Query("q") query: String): List<Product>

    @GET("productos/categoria/{category}")
    suspend fun getProductsByCategory(@Path("category") category: String): List<Product>

    @GET("categorias")
    suspend fun getCategories(): List<Category>
}
```

#### RetrofitClient.kt

```kotlin
object RetrofitClient {
    private const val BASE_URL = "https://tu-backend.com/api/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: ProductApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProductApiService::class.java)
    }
}
```

---

### Pasos para Integración Completa

#### 1. Crear Backend en Node.js/Express

**Archivo**: `server.js`

```javascript
const express = require('express');
const { Pool } = require('pg');

const app = express();
const pool = new Pool({
  connectionString: 'postgresql://neondb_owner:npg_R7m8bHdfNyLW@ep-rapid-rice-a3hr3zr8-pooler.sa-east-1.aws.neon.tech/neondb?sslmode=require'
});

// GET /api/productos
app.get('/api/productos', async (req, res) => {
  try {
    const result = await pool.query('SELECT * FROM catalogo.v_productos_completos');
    res.json(result.rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// GET /api/productos/search?q=lavanda
app.get('/api/productos/search', async (req, res) => {
  const { q } = req.query;
  try {
    const result = await pool.query(
      'SELECT * FROM catalogo.v_productos_completos WHERE nombre ILIKE $1 OR descripcion ILIKE $1',
      [`%${q}%`]
    );
    res.json(result.rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// GET /api/categorias
app.get('/api/categorias', async (req, res) => {
  try {
    const result = await pool.query('SELECT * FROM catalogo.categorias');
    res.json(result.rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
```

---

#### 2. Desplegar Backend

**Opciones de Hosting**:
- **Render.com** (Recomendado para Node.js)
- **Railway.app**
- **Heroku**
- **AWS Lambda + API Gateway**

**Guía Render.com**:
1. Crear cuenta en [Render.com](https://render.com)
2. Conectar repositorio de GitHub con el backend
3. Crear nuevo **Web Service**
4. Configurar:
   - **Environment**: Node
   - **Build Command**: `npm install`
   - **Start Command**: `node server.js`
   - **Environment Variables**: Agregar `DATABASE_URL` con la connection string de Neon
5. Desplegar

**URL Resultante**: `https://plantbuddy-api.onrender.com`

---

#### 3. Actualizar ProductRepository.kt

Cambiar de mock data a llamadas API reales:

```kotlin
class ProductRepository(private val api: ProductApiService) {
    suspend fun getProducts(): List<Product> {
        return try {
            api.getProducts()
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error al obtener productos", e)
            emptyList()
        }
    }

    suspend fun searchProducts(query: String): List<Product> {
        return try {
            api.searchProducts(query)
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error en búsqueda", e)
            emptyList()
        }
    }

    fun observeProducts(): Flow<List<Product>> = flow {
        while (true) {
            val products = getProducts()
            emit(products)
            delay(30000) // Actualiza cada 30 segundos
        }
    }
}
```

---

#### 4. Actualizar RetrofitClient.kt

Cambiar `BASE_URL` con la URL del backend desplegado:

```kotlin
private const val BASE_URL = "https://plantbuddy-api.onrender.com/api/"
```

---

#### 5. Agregar Permisos de Red (Ya configurado)

**AndroidManifest.xml**:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

---

#### 6. Manejo de Imágenes en Producción

**Opciones**:

**A. Cloudinary** (Recomendado)
- Crear cuenta gratuita en [Cloudinary](https://cloudinary.com)
- Subir las 7 imágenes de productos
- Obtener URLs públicas
- Actualizar `imagen_url` en la base de datos

**B. AWS S3**
- Crear bucket público
- Subir imágenes
- Actualizar URLs en BD

**C. Backend estático**
- Crear carpeta `public/images/` en el backend
- Servir con `express.static()`
- URLs: `https://plantbuddy-api.onrender.com/images/viburnum_lucidum.jpg`

---

## Dependencias y Librerías

### build.gradle.kts (Module: app)

#### Plugins
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.0.21-1.0.27"  // Kotlin Symbol Processing
}
```

**KSP (Kotlin Symbol Processing)**:
- Reemplazo de KAPT (más rápido y eficiente)
- Procesa anotaciones de Room en tiempo de compilación
- Genera implementaciones de DAOs automáticamente

---

#### Configuración Android

```kotlin
android {
    namespace = "cl.duoc.app"
    compileSdk = 36
    
    defaultConfig {
        applicationId = "cl.duoc.app"
        minSdk = 24          // Android 7.0 (Nougat)
        targetSdk = 36       // Android 14
        versionCode = 1
        versionName = "1.0"
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}
```

---

#### Dependencias Core

##### Core Android
```kotlin
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.appcompat:appcompat:1.6.1")
implementation("com.google.android.material:material:1.11.0")
implementation("androidx.activity:activity:1.8.2")
implementation("androidx.constraintlayout:constraintlayout:2.1.4")
```

**core-ktx**: Extensiones Kotlin para Android SDK
**appcompat**: Compatibilidad con versiones anteriores de Android
**material**: Material Design Components

---

##### Jetpack Compose

```kotlin
implementation(platform("androidx.compose:compose-bom:2024.02.00"))
implementation("androidx.activity:activity-compose:1.8.2")
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.ui:ui-tooling-preview")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.material:material-icons-extended")
```

**compose-bom**: Bill of Materials - gestiona versiones compatibles de Compose
**activity-compose**: Integración de Activity con Compose
**material3**: Material Design 3 (You)
**material-icons-extended**: Íconos adicionales

---

##### Navigation Compose

```kotlin
implementation("androidx.navigation:navigation-compose:2.7.6")
```

**Funcionalidades**:
- Navegación tipo-segura con rutas
- Back stack management
- Animaciones de transición
- Deep links

---

##### Lifecycle & ViewModel

```kotlin
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
```

**lifecycle-viewmodel-compose**: ViewModels optimizados para Compose
**lifecycle-runtime-ktx**: Coroutines para lifecycle

---

##### Room Database ⭐

```kotlin
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")
```

**room-runtime**: Biblioteca principal de Room
**room-ktx**: Extensiones Kotlin (Flow, Coroutines)
**room-compiler**: Procesador de anotaciones (genera código con KSP)

**Por qué Room 2.6.1**:
- Soporte nativo para Flow
- Mejor performance con KSP
- Type safety mejorada
- Migraciones automáticas

---

##### Networking (Preparado)

```kotlin
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
```

**retrofit**: Cliente HTTP tipo-seguro
**converter-gson**: Serialización JSON ↔ Kotlin
**okhttp**: Cliente HTTP low-level
**logging-interceptor**: Logs de requests/responses (debug)

---

##### Image Loading

```kotlin
implementation("io.coil-kt:coil-compose:2.5.0")
```

**Coil**: Carga de imágenes optimizada para Compose
- Caché automático (memoria + disco)
- Transformaciones (rounded, blur, etc)
- Placeholders y error handling

---

##### Coroutines

```kotlin
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

**Funcionalidades**:
- `viewModelScope`: Scope atado al lifecycle del ViewModel
- `launch`: Ejecutar coroutines
- `async/await`: Operaciones paralelas
- `Flow`: Streams reactivos

---

##### DataStore (Preferencias)

```kotlin
implementation("androidx.datastore:datastore-preferences:1.0.0")
```

**Reemplazo de SharedPreferences**:
- Type-safe
- Asíncrono (coroutines)
- Consistencia de datos garantizada

---

#### Testing

```kotlin
testImplementation("junit:junit:4.13.2")
androidTestImplementation("androidx.test.ext:junit:1.1.5")
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
debugImplementation("androidx.compose.ui:ui-tooling")
```

---

### libs.versions.toml

Archivo de gestión centralizada de versiones:

```toml
[versions]
kotlin = "2.0.21"
compose-bom = "2024.02.00"
room = "2.6.1"
navigation = "2.7.6"
lifecycle = "2.7.0"

[libraries]
androidx-room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
androidx-room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
androidx-room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }

[plugins]
android-application = { id = "com.android.application", version = "8.2.0" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
```

---

### Arquitectura de Dependencias

```
┌─────────────────────────────────────────────────────────┐
│                    UI LAYER                              │
│  ┌───────────────────────────────────────────────────┐  │
│  │  Jetpack Compose + Material3 + Navigation        │  │
│  │  - androidx.compose.ui                           │  │
│  │  - androidx.compose.material3                    │  │
│  │  - androidx.navigation.compose                   │  │
│  └───────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                 PRESENTATION LAYER                       │
│  ┌───────────────────────────────────────────────────┐  │
│  │  Lifecycle + ViewModel + StateFlow               │  │
│  │  - androidx.lifecycle.viewmodel-compose          │  │
│  │  - kotlinx.coroutines                            │  │
│  └───────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                    DATA LAYER                            │
│  ┌───────────────────────────────────────────────────┐  │
│  │  Room Database (Local) + Retrofit (Remote)       │  │
│  │  - androidx.room (runtime, ktx, compiler)        │  │
│  │  - com.squareup.retrofit2                        │  │
│  │  - com.squareup.okhttp3                          │  │
│  └───────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────┐  │
│  │  DataStore (Preferences)                         │  │
│  │  - androidx.datastore.preferences                │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

### KSP vs KAPT

#### KAPT (Kotlin Annotation Processing Tool) - Antiguo
- Más lento (procesa todo el código)
- Mayor uso de memoria
- Deprecado desde Kotlin 1.8

#### KSP (Kotlin Symbol Processing) - Actual ⭐
- **2x más rápido** que KAPT
- Mejor integración con Kotlin
- Menos overhead de memoria
- Mantenido por JetBrains

**Migración de KAPT a KSP**:
```kotlin
// Antes (KAPT)
plugins {
    id("kotlin-kapt")
}
dependencies {
    kapt("androidx.room:room-compiler:2.6.1")
}

// Ahora (KSP)
plugins {
    id("com.google.devtools.ksp") version "2.0.21-1.0.27"
}
dependencies {
    ksp("androidx.room:room-compiler:2.6.1")
}
```

---

### Versiones de Kotlin y Compatibilidad

| Componente | Versión | Compatibilidad |
|-----------|---------|----------------|
| Kotlin | 2.0.21 | Última estable |
| KSP | 2.0.21-1.0.27 | Match con Kotlin |
| Compose Compiler | Auto (plugin) | Gestionado por Gradle |
| Room | 2.6.1 | Kotlin 1.9+ |
| Coroutines | 1.7.3 | Kotlin 1.8+ |
| Target JVM | 17 | Android Studio Hedgehog+ |

---

### Configuración de Gradle

#### settings.gradle.kts

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
```

**google()**: Repositorio de Android (Compose, Room, Lifecycle)
**mavenCentral()**: Repositorio principal de Java/Kotlin (Retrofit, Coroutines)

---

### Optimizaciones de Build

#### Habilitar Build Cache

```kotlin
org.gradle.caching=true
org.gradle.parallel=true
kotlin.incremental=true
```

#### Configurar JVM para Gradle

```kotlin
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m
```

---

## Guía de Mantenimiento

### Agregar un Nuevo Producto

#### 1. En Room Database (Local)

**Opción A: Usando Repository en ViewModel**

```kotlin
// En CatalogViewModel o similar
viewModelScope.launch {
    val newProduct = Product(
        name = "Nueva Planta",
        description = "Descripción detallada",
        price = 19990.0,
        category = "Arbustos",
        imageUrl = "nueva_planta",
        stock = 50,
        rating = 4.5
    )
    
    try {
        productRepository.insertProduct(newProduct)
        // Producto insertado exitosamente
    } catch (e: Exception) {
        Log.e("CatalogViewModel", "Error al insertar producto", e)
    }
}
```

**Opción B: Inserción directa en DAO (para inicialización)**

```kotlin
// En MainActivity.onCreate() o Application.onCreate()
lifecycleScope.launch {
    val db = AppDatabase.getDatabase(applicationContext)
    val productDao = db.productDao()
    
    val products = listOf(
        ProductEntity(
            name = "Viburnum Lucidum",
            description = "Arbusto perenne",
            price = 24990.0,
            imageUrl = "viburnum_lucidum",
            category = "Arbustos",
            stock = 10,
            rating = 4.8
        ),
        // ... más productos
    )
    
    productDao.insertAll(products)
}
```

---

#### 2. En la Base de Datos Neon (Backend - Futuro)

```sql
-- Insertar categoría si no existe
INSERT INTO catalogo.categorias (nombre, descripcion, icono)
VALUES ('Nueva Categoría', 'Descripción', '🌱')
ON CONFLICT (nombre) DO NOTHING;

-- Insertar producto
INSERT INTO catalogo.productos (nombre, descripcion, precio, categoria_id, imagen_url, stock, rating)
VALUES (
    'Nombre de la Planta',
    'Descripción detallada del producto',
    19990,
    (SELECT id FROM catalogo.categorias WHERE nombre = 'Arbustos'),
    'nombre_planta',
    50,
    4.5
);

-- Insertar detalles botánicos
INSERT INTO catalogo.plantas_detalle (
    producto_id,
    nombre_cientifico,
    familia,
    origen,
    tipo_riego,
    exposicion_solar,
    altura_maxima,
    tipo_suelo,
    epoca_floracion,
    color_flor,
    crecimiento,
    rusticidad,
    usos
)
VALUES (
    (SELECT id FROM catalogo.productos WHERE nombre = 'Nombre de la Planta'),
    'Nombre Científico',
    'Familia Botánica',
    'País de Origen',
    'Moderado',
    'Sol directo',
    '2-3 metros',
    'Drenado y fértil',
    'Primavera-Verano',
    'Blanco',
    'Medio',
    'Resistente a heladas ligeras',
    'Ornamental, cercos, jardines'
);
```

---

#### 3. Agregar Imagen

1. Renombrar imagen a `nombre_planta.jpg` (minúsculas, sin espacios)
2. Colocar en `app/src/main/res/drawable/`
3. Sincronizar proyecto en Android Studio

---

### Agregar una Nueva Categoría

#### 1. En la Base de Datos

```sql
INSERT INTO catalogo.categorias (nombre, descripcion, icono)
VALUES ('Cactáceas', 'Plantas suculentas resistentes a la sequía', '🌵')
ON CONFLICT (nombre) DO NOTHING;
```

---

#### 2. En ProductRepository.kt

```kotlin
private val mockCategories = listOf(
    // ... categorías existentes ...
    Category(id = 6, name = "Cactáceas", icon = "🌵")
)
```

---

### Actualizar Precios

```sql
UPDATE catalogo.productos
SET precio = 18990, updated_at = CURRENT_TIMESTAMP
WHERE nombre = 'Lavanda Dentata';
```

---

### Operaciones CRUD con Room Database

#### Inicializar Base de Datos

**Opción 1: En MainActivity (Recomendado)**

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar database
        val database = AppDatabase.getDatabase(applicationContext)
        
        // Prellenar datos iniciales (solo primera vez)
        lifecycleScope.launch {
            val productDao = database.productDao()
            val count = productDao.getAllProducts().first().size
            
            if (count == 0) {
                // Base de datos vacía, insertar productos iniciales
                val initialProducts = listOf(
                    ProductEntity(
                        name = "Viburnum Lucidum",
                        description = "Arbusto perenne ideal para cercos",
                        price = 24990.0,
                        imageUrl = "viburnum_lucidum",
                        category = "Arbustos",
                        stock = 10,
                        rating = 4.8
                    ),
                    // ... más productos
                )
                productDao.insertAll(initialProducts)
            }
        }
        
        setContent {
            PlantBuddyTheme {
                // UI
            }
        }
    }
}
```

**Opción 2: En Application Class**

```kotlin
class PlantBuddyApplication : Application() {
    lateinit var database: AppDatabase
        private set
    
    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        
        // Inicializar datos
        CoroutineScope(Dispatchers.IO).launch {
            initializeData()
        }
    }
    
    private suspend fun initializeData() {
        // Lógica de inicialización
    }
}

// En AndroidManifest.xml
<application
    android:name=".PlantBuddyApplication"
    ...>
```

---

#### Operaciones de Lectura

**Observar todos los productos (reactivo)**

```kotlin
// En ViewModel
class CatalogViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {
    
    val products: StateFlow<List<Product>> = productRepository
        .getAllProducts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

// En Composable
val products by viewModel.products.collectAsState()

LazyColumn {
    items(products) { product ->
        ProductCard(product = product)
    }
}
```

**Buscar productos**

```kotlin
fun searchProducts(query: String): Flow<List<Product>> {
    return productRepository.searchProducts(query)
}

// Uso
viewModelScope.launch {
    searchProducts("lavanda")
        .collect { results ->
            _estado.update { it.copy(searchResults = results) }
        }
}
```

**Obtener un producto por ID**

```kotlin
suspend fun getProduct(id: Int): Product? {
    return productRepository.getProductById(id).first()
}
```

---

#### Operaciones de Escritura

**Insertar producto**

```kotlin
viewModelScope.launch {
    try {
        val newProduct = Product(
            name = "Rosmarinus Officinalis",
            description = "Romero aromático",
            price = 8990.0,
            imageUrl = "romero",
            category = "Aromáticas",
            stock = 25,
            rating = 4.6
        )
        
        productRepository.insertProduct(newProduct)
        
        // Éxito - Room actualizará el Flow automáticamente
    } catch (e: Exception) {
        _estado.update { it.copy(error = "Error al guardar: ${e.message}") }
    }
}
```

**Actualizar producto**

```kotlin
viewModelScope.launch {
    try {
        val updatedProduct = product.copy(
            stock = product.stock - 1,  // Decrementar stock
            rating = 4.9                // Actualizar rating
        )
        
        productRepository.updateProduct(updatedProduct)
    } catch (e: Exception) {
        Log.e("ViewModel", "Error al actualizar", e)
    }
}
```

**Eliminar producto**

```kotlin
viewModelScope.launch {
    try {
        productRepository.deleteProduct(product)
    } catch (e: Exception) {
        Log.e("ViewModel", "Error al eliminar", e)
    }
}
```

**Eliminar todos los productos**

```kotlin
viewModelScope.launch {
    productRepository.deleteAll()
}
```

---

#### Operaciones con Usuarios

**Registrar usuario**

```kotlin
// En AuthViewModel
fun register(username: String, email: String, password: String) {
    viewModelScope.launch {
        try {
            // Validar que no exista
            if (userRepository.emailExists(email)) {
                _estado.update { it.copy(registerError = "Email ya registrado") }
                return@launch
            }
            
            if (userRepository.usernameExists(username)) {
                _estado.update { it.copy(registerError = "Username ya existe") }
                return@launch
            }
            
            // Hash password (usar BCrypt en producción)
            val hashedPassword = hashPassword(password)
            
            val newUser = User(
                username = username,
                email = email,
                password = hashedPassword
            )
            
            val userId = userRepository.insertUser(newUser)
            
            // Guardar sesión
            saveUserSession(userId)
            
            _estado.update { it.copy(isLoggedIn = true) }
        } catch (e: Exception) {
            _estado.update { it.copy(registerError = e.message) }
        }
    }
}
```

**Login usuario**

```kotlin
fun login(email: String, password: String) {
    viewModelScope.launch {
        try {
            val user = userRepository.getUserByEmail(email)
            
            if (user == null) {
                _estado.update { it.copy(loginError = "Usuario no encontrado") }
                return@launch
            }
            
            if (!verifyPassword(password, user.password)) {
                _estado.update { it.copy(loginError = "Contraseña incorrecta") }
                return@launch
            }
            
            // Guardar sesión
            saveUserSession(user.id)
            
            _estado.update { it.copy(isLoggedIn = true, currentUser = user) }
        } catch (e: Exception) {
            _estado.update { it.copy(loginError = e.message) }
        }
    }
}
```

---

#### Operaciones con Plantel

**Agregar planta al plantel**

```kotlin
fun addPlantToPlantel(userId: Int, productId: Int, plantName: String) {
    viewModelScope.launch {
        try {
            // Verificar que no exista
            val existing = plantelRepository.getPlantByUserAndProduct(userId, productId)
            if (existing != null) {
                _estado.update { it.copy(error = "Ya tienes esta planta") }
                return@launch
            }
            
            val newPlant = PlantelPlant(
                userId = userId,
                productId = productId,
                plantName = plantName,
                plantDescription = "Mi $plantName",
                wateringFrequencyDays = 7
            )
            
            plantelRepository.insertPlant(newPlant)
            
            _estado.update { it.copy(showSuccessMessage = true) }
        } catch (e: Exception) {
            _estado.update { it.copy(error = e.message) }
        }
    }
}
```

**Regar planta (actualizar fecha)**

```kotlin
fun waterPlant(plantId: Int) {
    viewModelScope.launch {
        try {
            plantelRepository.updateLastWatered(plantId, System.currentTimeMillis())
        } catch (e: Exception) {
            Log.e("PlantelViewModel", "Error al regar", e)
        }
    }
}
```

**Observar plantas del usuario**

```kotlin
val userPlants: StateFlow<List<PlantelPlant>> = plantelRepository
    .getUserPlants(currentUserId)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
```

---

#### Transacciones con Room

Para operaciones múltiples que deben ser atómicas:

```kotlin
@Transaction
suspend fun completeCheckout(
    userId: Int,
    cartItems: List<CartItem>,
    shippingAddress: String,
    paymentMethod: PaymentMethod
) {
    // Calcular total
    val total = cartItems.sumOf { it.subtotal }
    
    // Crear compra
    val purchase = Purchase(
        userId = userId,
        total = total,
        shippingAddress = shippingAddress,
        paymentMethod = paymentMethod,
        status = PurchaseStatus.PENDING
    )
    
    val purchaseId = purchaseRepository.insertPurchase(purchase)
    
    // Actualizar stock de productos
    cartItems.forEach { item ->
        val product = productRepository.getProductById(item.product.id).first()
        product?.let {
            val updated = it.copy(stock = it.stock - item.quantity)
            productRepository.updateProduct(updated)
        }
    }
    
    // Limpiar carrito
    cartRepository.clearCart()
}
```

---

#### Consultas Complejas

**Productos con stock bajo**

```kotlin
@Query("SELECT * FROM products WHERE stock < :threshold")
fun getLowStockProducts(threshold: Int = 5): Flow<List<ProductEntity>>
```

**Compras del último mes**

```kotlin
@Query("""
    SELECT * FROM purchases 
    WHERE userId = :userId 
    AND createdAt > :startDate 
    ORDER BY createdAt DESC
""")
fun getRecentPurchases(userId: Int, startDate: Long): Flow<List<PurchaseEntity>>
```

**Plantas que necesitan riego**

```kotlin
@Query("""
    SELECT * FROM plantel_plants 
    WHERE userId = :userId 
    AND (lastWateredDate IS NULL 
         OR lastWateredDate < :thresholdDate)
""")
fun getPlantsNeedingWater(
    userId: Int, 
    thresholdDate: Long
): Flow<List<PlantelPlantEntity>>

// Uso
val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
plantelPlantDao.getPlantsNeedingWater(userId, sevenDaysAgo)
```

---

### Debugging Común

#### Problema: "Cannot access database on the main thread"

**Error**:
```
java.lang.IllegalStateException: Cannot access database on the main thread
```

**Solución**: Usar `suspend fun` o `viewModelScope.launch`

```kotlin
// ❌ Incorrecto
fun loadProducts() {
    val products = productDao.getAllProducts()  // Crash!
}

// ✅ Correcto
fun loadProducts() {
    viewModelScope.launch {
        productRepository.getAllProducts()
            .collect { products ->
                _estado.update { it.copy(products = products) }
            }
    }
}
```

---

#### Problema: "Imagen no se muestra"

**Solución**:
1. Verificar que el nombre de archivo coincide **exactamente** (minúsculas, sin espacios)
2. Verificar que está en `drawable/` no en `mipmap/`
3. Hacer **Clean Project** → **Rebuild Project** en Android Studio
4. Revisar Logcat: `adb logcat | grep ProductImage`

---

#### Problema: "Room no genera DAOs"

**Error**:
```
Unresolved reference: UserDao_Impl
```

**Soluciones**:
1. **Build → Rebuild Project** en Android Studio
2. Verificar que KSP esté configurado:
   ```kotlin
   plugins {
       id("com.google.devtools.ksp") version "2.0.21-1.0.27"
   }
   ```
3. Limpiar caché:
   ```bash
   ./gradlew clean
   ./gradlew build
   ```
4. Invalidar cachés: **File → Invalidate Caches → Invalidate and Restart**

---

#### Problema: "Flow no emite valores actualizados"

**Síntoma**: UI no se actualiza después de insertar/actualizar datos.

**Solución**: Verificar que estás usando `Flow` en el DAO, no `suspend fun` con retorno directo.

```kotlin
// ❌ No reactivo
@Query("SELECT * FROM products")
suspend fun getAllProducts(): List<ProductEntity>

// ✅ Reactivo
@Query("SELECT * FROM products")
fun getAllProducts(): Flow<List<ProductEntity>>
```

---

#### Problema: "Type mismatch: Date vs Long"

**Error**:
```
Type mismatch: inferred type is Date but Long was expected
```

**Solución**: Asegurarse de que `@TypeConverters(Converters::class)` está en `@Database`

```kotlin
@Database(
    entities = [...],
    version = 1
)
@TypeConverters(Converters::class)  // ← Importante
abstract class AppDatabase : RoomDatabase()
```

---

#### Problema: "Foreign key constraint failed"

**Error**:
```
FOREIGN KEY constraint failed (code 787)
```

**Causa**: Intentar insertar con `userId` que no existe en tabla `users`.

**Solución**: Insertar usuario primero

```kotlin
// 1. Insertar usuario
val userId = userRepository.insertUser(newUser)

// 2. Usar ese ID para la planta
val plant = PlantelPlant(
    userId = userId.toInt(),
    productId = productId,
    ...
)
plantelRepository.insertPlant(plant)
```

---

#### Problema: "App sigue en 60Hz"

**Solución**:
1. Revisar Logcat: `adb logcat | grep RefreshRate`
2. Confirmar que el dispositivo soporta 120Hz
3. Configurar manualmente en MIUI: `Ajustes → Pantalla → Frecuencia de actualización`
4. Verificar que `hardwareAccelerated="true"` en AndroidManifest

---

#### Problema: "Búsqueda no funciona"

**Solución**:
1. Verificar que `onQueryChange` está llamando a `viewModel.onSearchQueryChange()`
2. Confirmar que `filterProducts()` está siendo invocado
3. Revisar que `searchQuery` en UIState se actualiza correctamente
4. Logs en ViewModel:
   ```kotlin
   Log.d("CatalogViewModel", "Search query: $query, Results: ${filteredProducts.size}")
   ```

---

### Testing

#### Unit Tests

```kotlin
@Test
fun `filterProducts con búsqueda retorna productos correctos`() {
    val repository = ProductRepository()
    val viewModel = CatalogViewModel(repository)
    
    viewModel.onSearchQueryChange("lavanda")
    
    val state = viewModel.estado.value
    assertEquals(1, state.filteredProducts.size)
    assertEquals("Lavanda Dentata", state.filteredProducts[0].name)
}
```

---

### Métricas de Rendimiento

- **Tiempo de carga inicial**: < 500ms (mock data)
- **Tiempo de búsqueda**: < 100ms (filtrado local)
- **Tiempo de filtrado por categoría**: < 50ms
- **Recomposiciones UI**: Optimizado con `remember()` y `derivedStateOf`

---

## Próximos Pasos Recomendados

### Corto Plazo (1-2 semanas)
1. ✅ Implementar backend en Node.js/Express
2. ✅ Desplegar backend en Render.com
3. ✅ Subir imágenes a Cloudinary
4. ✅ Actualizar `imagen_url` en base de datos con URLs públicas
5. ✅ Cambiar ProductRepository a llamadas API reales

### Mediano Plazo (3-4 semanas)
6. ⏳ Implementar pantalla de detalle de producto
7. ⏳ Sistema de carrito de compras
8. ⏳ Autenticación con JWT
9. ⏳ Sistema de favoritos
10. ⏳ Historial de compras

### Largo Plazo (2-3 meses)
11. ⏳ Sistema de reviews y ratings
12. ⏳ Integración de pagos (Transbank, MercadoPago)
13. ⏳ Notificaciones push
14. ⏳ Sistema de recomendaciones basado en IA
15. ⏳ Panel de administración web

---

## Contacto y Soporte

**Desarrollador**: Diego Herrera  
**Proyecto**: PlantBuddy - Catálogo de Plantas Ornamentales  
**Framework**: Jetpack Compose + Material 3  
**Base de Datos**: Neon Serverless Postgres  

---

## Apéndices

### A. Comandos útiles de Gradle

```bash
# Limpiar proyecto
./gradlew clean

# Construir APK
./gradlew assembleDebug

# Instalar en dispositivo
./gradlew installDebug

# Ver dependencias
./gradlew dependencies
```

---

### B. Estructura de Archivos Completa

```
DAM_EVA2_Diego_Herrera/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/cl/duoc/app/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   │
│   │   │   │   ├── model/                           # 🆕 NUEVA ARQUITECTURA ROOM
│   │   │   │   │   ├── data/
│   │   │   │   │   │   ├── config/
│   │   │   │   │   │   │   ├── AppDatabase.kt      # Singleton Room DB
│   │   │   │   │   │   │   └── Converters.kt       # TypeConverters (Date ↔ Long)
│   │   │   │   │   │   ├── entities/
│   │   │   │   │   │   │   ├── UserEntity.kt       # @Entity - Tabla users
│   │   │   │   │   │   │   ├── ProductEntity.kt    # @Entity - Tabla products
│   │   │   │   │   │   │   ├── PlantelPlantEntity.kt # @Entity - Tabla plantel_plants
│   │   │   │   │   │   │   └── PurchaseEntity.kt   # @Entity - Tabla purchases
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   ├── UserDao.kt          # @Dao - CRUD usuarios
│   │   │   │   │   │   │   ├── ProductDao.kt       # @Dao - CRUD productos
│   │   │   │   │   │   │   ├── PlantelPlantDao.kt  # @Dao - CRUD plantel
│   │   │   │   │   │   │   └── PurchaseDao.kt      # @Dao - CRUD compras
│   │   │   │   │   │   └── repositories/
│   │   │   │   │   │       ├── UserRepository.kt   # Entity ↔ Domain
│   │   │   │   │   │       ├── ProductRepository.kt # Entity ↔ Domain
│   │   │   │   │   │       └── PlantelRepository.kt # Entity ↔ Domain
│   │   │   │   │   └── domain/
│   │   │   │   │       ├── User.kt                 # Modelo de negocio
│   │   │   │   │       ├── Product.kt              # Modelo de negocio
│   │   │   │   │       ├── PlantelPlant.kt         # Modelo de negocio
│   │   │   │   │       ├── Purchase.kt             # Modelo de negocio + Enums
│   │   │   │   │       ├── CartItem.kt             # Modelo carrito
│   │   │   │   │       ├── LoginUIState.kt         # Estado formulario login
│   │   │   │   │       └── RegisterUIState.kt      # Estado formulario registro
│   │   │   │   │
│   │   │   │   ├── data/                           # 📦 LEGACY (anterior)
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Product.kt              # Modelo antiguo
│   │   │   │   │   │   ├── Category.kt
│   │   │   │   │   │   ├── User.kt
│   │   │   │   │   │   ├── CartItem.kt
│   │   │   │   │   │   ├── PlantelPlant.kt
│   │   │   │   │   │   └── PlantState.kt
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── ProductRepository.kt    # Mock data
│   │   │   │   │   │   ├── UserRepository.kt       # Mock data
│   │   │   │   │   │   ├── CartRepository.kt       # In-memory
│   │   │   │   │   │   └── PlantelRepository.kt    # Mock data
│   │   │   │   │   ├── api/
│   │   │   │   │   │   ├── ProductApiService.kt    # Retrofit (preparado)
│   │   │   │   │   │   └── RetrofitClient.kt
│   │   │   │   │   └── preferences/
│   │   │   │   │       └── UserPreferences.kt      # DataStore
│   │   │   │   │
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── auth/
│   │   │   │   │   │   │   ├── LoginScreen.kt
│   │   │   │   │   │   │   └── RegisterScreen.kt
│   │   │   │   │   │   ├── catalog/
│   │   │   │   │   │   │   ├── CatalogScreen.kt
│   │   │   │   │   │   │   ├── CatalogViewModel.kt
│   │   │   │   │   │   │   └── CatalogUIState.kt
│   │   │   │   │   │   ├── cart/
│   │   │   │   │   │   │   └── CartScreen.kt
│   │   │   │   │   │   ├── plantel/
│   │   │   │   │   │   │   ├── PlantelScreen.kt
│   │   │   │   │   │   │   ├── PlantelViewModel.kt
│   │   │   │   │   │   │   └── PlantelUIState.kt
│   │   │   │   │   │   ├── BienvenidaScreen.kt
│   │   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   │   └── AjustesScreen.kt
│   │   │   │   │   ├── components/
│   │   │   │   │   │   └── BottomNavigationBar.kt
│   │   │   │   │   ├── viewmodel/
│   │   │   │   │   │   ├── AuthViewModel.kt
│   │   │   │   │   │   ├── CartViewModel.kt
│   │   │   │   │   │   └── SettingsViewModel.kt
│   │   │   │   │   └── theme/
│   │   │   │   │       ├── Theme.kt
│   │   │   │   │       └── Type.kt
│   │   │   │   │
│   │   │   │   ├── navigation/
│   │   │   │   │   ├── Navigation.kt
│   │   │   │   │   └── AppRoutes.kt
│   │   │   │   │
│   │   │   │   ├── notifications/
│   │   │   │   │   ├── NotificationHelper.kt
│   │   │   │   │   ├── NotificationPermissionHelper.kt
│   │   │   │   │   └── WateringReminderReceiver.kt
│   │   │   │   │
│   │   │   │   └── viewmodel/                      # ⚠️ Legacy ViewModel
│   │   │   │       └── LoginViewModel.kt
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── drawable/
│   │   │   │   │   ├── viburnum_lucidum.jpg
│   │   │   │   │   ├── kniphofia_uvaria.jpg
│   │   │   │   │   ├── rhus_crenata.jpg
│   │   │   │   │   ├── lavanda_dentata.jpg
│   │   │   │   │   ├── laurel_flor_enano.jpg
│   │   │   │   │   ├── pitosporo_tobira_enano.jpg
│   │   │   │   │   └── bignonia_naranja.jpg
│   │   │   │   ├── values/
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   └── xml/
│   │   │   │       └── backup_rules.xml
│   │   │   │
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   ├── androidTest/
│   │   │   └── java/cl/duoc/app/
│   │   │       └── ExampleInstrumentedTest.kt
│   │   │
│   │   └── test/
│   │       └── java/cl/duoc/app/
│   │           └── ExampleUnitTest.kt
│   │
│   ├── build.gradle.kts                            # 🔧 Configuración con KSP + Room
│   └── proguard-rules.pro
│
├── database/                                       # 🗄️ BACKEND SQL
│   ├── neon_plantbuddy_schema.sql
│   ├── README.md
│   └── BACKEND_SETUP.md
│
├── gradle/
│   ├── libs.versions.toml                         # Gestión de versiones
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
│
├── build.gradle.kts                                # Configuración raíz
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── local.properties
├── README.md
├── SISTEMA_AUTENTICACION.md
└── FUNCIONAMIENTO_APP.md                          # 📖 Este archivo
```

### Leyenda de Iconos

- 🆕 **Nueva Arquitectura Room**: Implementación actual con Room Database
- 📦 **Legacy**: Código anterior que coexiste (se migrará progresivamente)
- ⚠️ **Deprecated**: Archivos que serán reemplazados
- 🔧 **Configuración**: Archivos de build y configuración
- 🗄️ **Backend**: Scripts SQL para Neon Postgres
- 📖 **Documentación**: Archivos informativos

---

### C. Dependencias del Proyecto (build.gradle.kts)

```kotlin
dependencies {
    // Jetpack Compose
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Retrofit (preparado)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
}
```

---

## Changelog

### Versión 2.0.0 (Actual - Room Database) 🆕
- ✅ **Arquitectura Room Database 2.6.1 con KSP**
  - AppDatabase con 4 tablas (users, products, plantel_plants, purchases)
  - TypeConverters para Date ↔ Long
  - 4 DAOs con Flow reactivo
  - 4 Entities con @ForeignKey
- ✅ **Clean Architecture**
  - Capa Domain: 7 modelos de negocio puros
  - Capa Data: 3 Repositories con Entity ↔ Domain conversion
  - Separación clara de responsabilidades
- ✅ **KSP (Kotlin Symbol Processing)**
  - Reemplazo de KAPT para procesamiento de anotaciones
  - 2x más rápido en compilación
  - Generación optimizada de código
- ✅ **Sistema de Carrito de Compras**
  - CartScreen con checkout dialog
  - CartViewModel con estado reactivo
  - Resumen de compra fijo con shadow
  - Previews completos
- ✅ **Scroll Collapse en Toolbar**
  - Ocultación automática al scroll hacia abajo
  - Animación suave con animateDpAsState
  - Detección de dirección con LazyGridState
- ✅ **Sistema de Autenticación (Preparado)**
  - LoginUIState y RegisterUIState
  - Validaciones de email/username/password
  - UserEntity con campos encrypted password
- ✅ Configuración 120Hz con hardwareAccelerated
- ✅ Navegación funcional con Navigation Compose 2.7.6
- ✅ Base de datos Neon Postgres documentada (backend futuro)

### Versión 1.0.0 (Anterior - Mock Data)
- ✅ Sistema de catálogo con 7 productos
- ✅ Búsqueda en tiempo real
- ✅ Filtros por categoría (6 categorías con iconos)
- ✅ Carga de imágenes locales desde drawable
- ✅ Mock data con precios CLP
- ✅ ProductRepository con datos en memoria
- ⚠️ **Deprecado**: Repositorios legacy en `data/repository/`

### Versión 2.1.0 (En Progreso)
- ⏳ Migración completa de ViewModels a Room Repositories
- ⏳ PurchaseRepository con Entity ↔ Domain conversion
- ⏳ Inicialización de datos mock en AppDatabase
- ⏳ CartRepository con Room (persistencia de carrito)
- ⏳ Sistema de notificaciones de riego con WorkManager

### Versión 3.0.0 (Planeada)
- ⏳ Integración con backend API (Neon Postgres)
- ⏳ Sincronización Room ↔ API REST
- ⏳ Carga de imágenes remotas con Coil
- ⏳ Pantalla de detalle de producto expandida
- ⏳ Sistema de favoritos persistente
- ⏳ Historial de compras con PurchaseDao
- ⏳ Sistema de reviews y ratings

---

## Glosario de Términos Técnicos

### Arquitectura y Patrones

**MVVM (Model-View-ViewModel)**
- Patrón de arquitectura que separa la lógica de negocio (ViewModel) de la UI (View)
- Facilita testing y reutilización de código
- View observa cambios en ViewModel mediante StateFlow/LiveData

**Clean Architecture**
- Arquitectura en capas con dependencias unidireccionales
- Capas: UI → Domain → Data
- Domain (modelos de negocio) no depende de frameworks

**Repository Pattern**
- Patrón que abstrae el acceso a datos
- Fuente única de verdad para los datos
- Facilita cambio entre fuentes (Room, API, caché)

**Singleton Pattern**
- Garantiza una única instancia de una clase
- Usado en AppDatabase para evitar múltiples conexiones

---

### Room Database

**@Entity**
- Anotación que marca una clase como tabla de base de datos
- Cada campo se convierte en columna
- Ejemplo: `@Entity(tableName = "users")`

**@PrimaryKey**
- Define la clave primaria de la tabla
- `autoGenerate = true`: ID autoincrementable
- Garantiza unicidad de registros

**@Dao (Data Access Object)**
- Interfaz que define operaciones de base de datos
- Room genera la implementación automáticamente
- Métodos anotados con @Query, @Insert, @Update, @Delete

**@Query**
- Define una consulta SQL personalizada
- Validada en tiempo de compilación
- Soporta parámetros con `:nombreParametro`

**@Insert / @Update / @Delete**
- Operaciones CRUD básicas
- `onConflict`: Define estrategia de conflicto (REPLACE, ABORT, IGNORE)

**@ForeignKey**
- Define relación entre tablas
- `onDelete = CASCADE`: Elimina registros hijos automáticamente
- Mantiene integridad referencial

**@TypeConverter**
- Convierte tipos complejos a tipos soportados por SQLite
- Ejemplo: Date ↔ Long, Enum ↔ String
- Se registra con `@TypeConverters` en @Database

**Flow<T>**
- Stream reactivo de datos
- Emite nuevo valor cuando cambia la base de datos
- Reemplaza LiveData en arquitecturas modernas

**suspend fun**
- Función que puede suspender ejecución (coroutine)
- No bloquea el thread principal
- Se ejecuta en background automáticamente con Room

---

### Kotlin & Coroutines

**StateFlow**
- Holder de estado observable (hot stream)
- Siempre tiene un valor actual
- Recompone UI automáticamente en Compose

**MutableStateFlow**
- Versión mutable de StateFlow
- Se actualiza con `.value = newValue` o `.update { }`
- Privada en ViewModel, expuesta como StateFlow inmutable

**viewModelScope**
- CoroutineScope atado al lifecycle del ViewModel
- Se cancela automáticamente cuando se destruye el ViewModel
- Evita memory leaks

**launch**
- Inicia una coroutine sin retornar valor
- Fire-and-forget
- Ejemplo: `viewModelScope.launch { ... }`

**async/await**
- `async`: Inicia coroutine que retorna resultado
- `await`: Espera el resultado
- Para operaciones paralelas

**collect**
- Observa un Flow y ejecuta lambda con cada valor emitido
- Suspende hasta que el Flow termina
- Ejemplo: `flow.collect { value -> ... }`

**map / filter / combine**
- Operadores de transformación de Flow
- `map`: Transforma cada valor emitado
- `filter`: Filtra valores según condición
- `combine`: Combina múltiples Flows

---

### Jetpack Compose

**@Composable**
- Anotación que marca función como componente UI
- Puede ser recompuesta múltiples veces
- Debe ser llamada desde otra @Composable o setContent

**remember**
- Almacena valor en memoria durante recomposiciones
- Se pierde al salir de la composición
- Ejemplo: `val state = remember { mutableStateOf("") }`

**rememberSaveable**
- Como remember pero sobrevive a cambios de configuración
- Guarda estado en Bundle
- Para formularios, scroll positions

**LaunchedEffect**
- Ejecuta efecto secundario cuando entra en composición
- Se cancela cuando sale de composición
- Para llamadas a API, observers

**derivedStateOf**
- Crea estado derivado de otros estados
- Solo recompone cuando cambia el resultado
- Optimización de performance

**Modifier**
- Sistema de modificación encadenable para UI
- Orden importa: `.padding().background()` ≠ `.background().padding()`
- Ejemplos: `.fillMaxWidth()`, `.padding(16.dp)`, `.clickable { }`

---

### Jetpack Components

**Navigation Compose**
- Sistema de navegación declarativo para Compose
- `NavHost`: Contenedor de rutas
- `NavController`: Controlador de navegación
- `composable("route")`: Define destino

**Material Design 3**
- Sistema de diseño moderno de Google
- Dynamic colors (Material You)
- Componentes: Card, Button, TextField, etc

**ViewModel**
- Sobrevive a cambios de configuración (rotación)
- Almacena estado UI
- Se destruye cuando Activity/Fragment se destruye permanentemente

**LiveData** (Reemplazado por StateFlow)
- Holder de datos observable lifecycle-aware
- Obsoleto en arquitecturas modernas
- StateFlow es más flexible y compatible con coroutines

---

### Procesamiento de Anotaciones

**KSP (Kotlin Symbol Processing)**
- API moderna para procesamiento de anotaciones
- 2x más rápido que KAPT
- Mejor integración con Kotlin
- Usado por Room 2.6+

**KAPT (Kotlin Annotation Processing Tool)** - Deprecado
- Procesador antiguo basado en Java
- Más lento y pesado
- Siendo reemplazado por KSP

**Code Generation**
- Room genera implementaciones de DAOs en tiempo de compilación
- Código generado en `app/build/generated/ksp/`
- Validación de queries SQL antes de ejecutar app

---

### Base de Datos

**SQLite**
- Base de datos relacional embebida en Android
- Almacenamiento local en archivo `.db`
- Room es una abstracción sobre SQLite

**Primary Key**
- Columna que identifica únicamente cada fila
- No puede ser NULL ni duplicada
- Generalmente `id INTEGER PRIMARY KEY AUTOINCREMENT`

**Foreign Key**
- Columna que referencia Primary Key de otra tabla
- Mantiene integridad referencial
- Permite relaciones 1:N, N:N

**CASCADE**
- Propaga operaciones (DELETE, UPDATE) a tablas relacionadas
- `onDelete = CASCADE`: Elimina hijos si se elimina padre
- Evita registros huérfanos

**Transaction**
- Grupo de operaciones que se ejecutan como unidad atómica
- Si una falla, todas se revierten (rollback)
- Garantiza consistencia de datos

**Index**
- Estructura que acelera búsquedas
- Penaliza inserciones/actualizaciones
- Útil para columnas frecuentemente consultadas

---

### Networking (Preparado)

**Retrofit**
- Cliente HTTP tipo-seguro para Android/Java
- Convierte API REST a interfaces Kotlin
- Integración con coroutines y Flow

**OkHttp**
- Cliente HTTP low-level
- Maneja conexiones, timeouts, caché
- Usado internamente por Retrofit

**Gson / Moshi / kotlinx.serialization**
- Librerías de serialización JSON ↔ Kotlin
- Gson: Más popular pero usa reflection
- Moshi: Más rápido, generación de código
- kotlinx.serialization: Oficial de Kotlin

**Interceptor**
- Middleware que procesa requests/responses
- Usos: logging, autenticación, modificación headers
- Ejemplo: `HttpLoggingInterceptor`

---

### Otros Conceptos

**Dependency Injection**
- Patrón que proporciona dependencias desde fuera
- Facilita testing (mock de dependencias)
- Frameworks: Hilt, Koin, manual injection

**BOM (Bill of Materials)**
- Gestiona versiones compatibles de librerías relacionadas
- Ejemplo: `compose-bom` para todos los componentes de Compose
- Evita conflictos de versiones

**Type Safety**
- El compilador verifica tipos en tiempo de compilación
- Previene errores de runtime
- Kotlin es más type-safe que Java

**Reactive Programming**
- Programación basada en streams de datos
- UI reacciona automáticamente a cambios
- Flow, StateFlow son ejemplos

**Coroutine**
- Hilo ligero (lightweight thread)
- Suspendible y reanudable
- Permite código asíncrono secuencial

**Extension Function**
- Añade funciones a clases existentes sin herencia
- Ejemplo: `fun String.isEmail(): Boolean`
- Usadas en Repository para conversión Entity ↔ Domain

---

**Fin de la Documentación Técnica**

*Última actualización: Noviembre 2025*
*Versión: 2.0.0 - Room Database Architecture*
