# Guía de Estudio - Evaluación PlantBuddy App

## Tabla de Contenidos
1. [UI y Navegación](#4-ui-y-navegación)
2. [Persistencia Local (SQLite)](#5-persistencia-local-sqlite)
3. [Arquitectura MVVM](#6-arquitectura-mvvm)

---

## 4) UI y Navegación

### a) Componentes Asociados a los Formularios

#### 1. **TextField y OutlinedTextField**
```kotlin
// Ejemplo en LoginScreen.kt (líneas 88-100)
OutlinedTextField(
    value = email,
    onValueChange = { viewModel.updateEmail(it) },
    label = { Text("Email") },
    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
    modifier = Modifier.fillMaxWidth()
)
```

**Componentes del formulario**:
- **value**: Estado actual del campo
- **onValueChange**: Lambda que actualiza el estado
- **label**: Etiqueta flotante del campo
- **leadingIcon/trailingIcon**: Íconos decorativos
- **keyboardOptions**: Configuración del teclado (email, número, texto)
- **visualTransformation**: Para ocultar contraseñas
- **isError**: Estado de validación visual

#### 2. **Button y IconButton**
```kotlin
// CartScreen.kt (líneas 150-160)
Button(
    onClick = { showCheckoutDialog = true },
    enabled = cartItems.isNotEmpty(),
    modifier = Modifier.fillMaxWidth(),
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary
    )
) {
    Text("Proceder al Pago")
}
```

**Propiedades clave**:
- **onClick**: Acción al presionar
- **enabled**: Control de habilitación basado en estado
- **colors**: Personalización de colores
- **modifier**: Ajustes de tamaño y padding

#### 3. **Checkbox y Switch**
```kotlin
// Ejemplo conceptual
Checkbox(
    checked = isChecked,
    onCheckedChange = { viewModel.updateCheckState(it) }
)

Switch(
    checked = notificationsEnabled,
    onCheckedChange = { viewModel.toggleNotifications(it) }
)
```

#### 4. **DropdownMenu y ExposedDropdownMenuBox**
```kotlin
// CartScreen.kt - Selector de método de pago (líneas 450-480)
ExposedDropdownMenuBox(
    expanded = expandedPayment,
    onExpandedChange = { expandedPayment = !expandedPayment }
) {
    OutlinedTextField(
        value = paymentMethod.displayName,
        onValueChange = {},
        readOnly = true,
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPayment) }
    )
    ExposedDropdownMenu(
        expanded = expandedPayment,
        onDismissRequest = { expandedPayment = false }
    ) {
        PaymentMethod.values().forEach { method ->
            DropdownMenuItem(
                text = { Text(method.displayName) },
                onClick = {
                    paymentMethod = method
                    expandedPayment = false
                }
            )
        }
    }
}
```

#### 5. **Dialog (AlertDialog)**
```kotlin
// CartScreen.kt - Dialog de confirmación (líneas 500-580)
AlertDialog(
    onDismissRequest = { showCheckoutDialog = false },
    title = { Text("Confirmar Compra") },
    text = {
        Column {
            Text("Total: $${String.format("%.2f", cartTotal)}")
            OutlinedTextField(
                value = shippingAddress,
                onValueChange = { shippingAddress = it },
                label = { Text("Dirección de envío") }
            )
        }
    },
    confirmButton = {
        Button(onClick = { viewModel.processOrder(...) }) {
            Text("Confirmar")
        }
    },
    dismissButton = {
        TextButton(onClick = { showCheckoutDialog = false }) {
            Text("Cancelar")
        }
    }
)
```

### b) Componentes de Navegación

#### 1. **NavController y NavHost**
```kotlin
// MainActivity.kt (líneas 60-80)
val navController = rememberNavController()

NavHost(
    navController = navController,
    startDestination = if (isLoggedIn) "main" else "login"
) {
    // Definición de rutas
    composable("login") { LoginScreen(navController, authViewModel) }
    composable("register") { RegisterScreen(navController, authViewModel) }
    composable("main") { MainScreen(navController) }
}
```

**Navegación programática**:
```kotlin
// Navegar a una pantalla
navController.navigate("productDetail/${productId}")

// Regresar
navController.popBackStack()

// Reemplazar stack completo
navController.navigate("main") {
    popUpTo("login") { inclusive = true }
}
```

#### 2. **BottomNavigation**
```kotlin
// MainScreen.kt (líneas 100-150)
Scaffold(
    bottomBar = {
        NavigationBar {
            items.forEach { item ->
                NavigationBarItem(
                    selected = currentRoute == item.route,
                    onClick = { 
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(item.label) }
                )
            }
        }
    }
)
```

**Rutas del Bottom Navigation**:
- `home` - Pantalla principal con productos
- `plantel` - Mis plantas
- `cart` - Carrito de compras
- `profile` - Perfil de usuario

#### 3. **TopAppBar con Navegación**
```kotlin
// RegisterScreen.kt (líneas 45-60)
TopAppBar(
    title = { Text("Registro") },
    navigationIcon = {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
        }
    }
)
```

#### 4. **Navegación con Argumentos**
```kotlin
// Definir ruta con parámetros
composable(
    route = "productDetail/{productId}",
    arguments = listOf(navArgument("productId") { type = NavType.IntType })
) { backStackEntry ->
    val productId = backStackEntry.arguments?.getInt("productId")
    ProductDetailScreen(navController, productId)
}

// Navegar con parámetros
navController.navigate("productDetail/${product.id}")
```

### c) Animaciones

#### 1. **AnimatedVisibility**
```kotlin
// CartScreen.kt - Animación del botón de checkout
AnimatedVisibility(
    visible = cartItems.isNotEmpty(),
    enter = fadeIn() + slideInVertically(),
    exit = fadeOut() + slideOutVertically()
) {
    Button(
        onClick = { /* Checkout */ },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Proceder al Pago")
    }
}
```

#### 2. **Transiciones de Navegación**
```kotlin
// Transiciones personalizadas entre pantallas
composable(
    route = "productDetail/{productId}",
    enterTransition = {
        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left)
    },
    exitTransition = {
        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right)
    }
) { /* Content */ }
```

#### 3. **Animaciones de Estado**
```kotlin
// Animación de tamaño de botón
val buttonScale by animateFloatAsState(
    targetValue = if (isPressed) 0.95f else 1f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
)

Button(
    modifier = Modifier.scale(buttonScale),
    onClick = { /* ... */ }
) { /* Content */ }
```

#### 4. **Animación de Carga (CircularProgressIndicator)**
```kotlin
// LoginScreen.kt (líneas 120-125)
if (isLoading) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
```

### d) Comunicación Componente - ViewModel

#### 1. **Flujo de Datos Unidireccional**
```kotlin
// LoginScreen.kt - Ejemplo completo
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    // 1. OBSERVAR ESTADOS del ViewModel
    val authState by viewModel.authState.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    
    // 2. COMPONENTE UI lee el estado
    OutlinedTextField(
        value = email,  // Estado viene del ViewModel
        onValueChange = { newEmail -> 
            // 3. EVENTO: Usuario escribe en el campo
            viewModel.updateEmail(newEmail)  // Notificar al ViewModel
        }
    )
    
    // 4. ACCIÓN: Usuario presiona botón
    Button(
        onClick = { 
            viewModel.login()  // ViewModel ejecuta lógica de negocio
        },
        enabled = !authState.isLoading  // UI reacciona al estado
    ) {
        Text("Iniciar Sesión")
    }
    
    // 5. REACCIÓN A CAMBIOS DE ESTADO
    LaunchedEffect(authState.user) {
        if (authState.user != null) {
            // Usuario autenticado, navegar a main
            navController.navigate("main") {
                popUpTo("login") { inclusive = true }
            }
        }
    }
    
    // 6. MANEJO DE ERRORES
    authState.error?.let { error ->
        Snackbar { Text(error) }
    }
}
```

#### 2. **ViewModel - Gestión de Estado**
```kotlin
// AuthViewModel.kt
class AuthViewModel : ViewModel() {
    // Estado privado mutable
    private val _authState = MutableStateFlow(AuthState())
    // Estado público inmutable para la UI
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()
    
    // Funciones para actualizar estado desde la UI
    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }
    
    // Lógica de negocio
    fun login() {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)
            
            try {
                val response = authRepository.login(email.value, password.value)
                if (response.isSuccessful) {
                    _authState.value = _authState.value.copy(
                        user = response.body()?.usuario,
                        isLoading = false
                    )
                } else {
                    _authState.value = _authState.value.copy(
                        error = "Credenciales inválidas",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
}
```

#### 3. **Patrón de Comunicación Completo**

```
┌─────────────────────────────────────────────────────────┐
│                    COMPOSABLE (UI)                       │
│                                                          │
│  1. collectAsState() - Observa StateFlow               │
│     val state by viewModel.state.collectAsState()      │
│                                                          │
│  2. Renderiza UI basada en estado                       │
│     if (state.isLoading) { CircularProgressIndicator() }│
│                                                          │
│  3. Eventos de usuario → Llama funciones del ViewModel  │
│     onClick = { viewModel.handleClick() }               │
│                                                          │
│  4. LaunchedEffect - Reacciona a cambios de estado     │
│     LaunchedEffect(state.success) { /* Navigate */ }    │
└─────────────────────────────────────────────────────────┘
                            ↕ StateFlow
┌─────────────────────────────────────────────────────────┐
│                      VIEWMODEL                           │
│                                                          │
│  1. MutableStateFlow (privado) - Estado interno         │
│     private val _state = MutableStateFlow(...)          │
│                                                          │
│  2. StateFlow (público) - Expone estado a la UI         │
│     val state: StateFlow<...> = _state.asStateFlow()    │
│                                                          │
│  3. Funciones públicas - Reciben eventos de la UI       │
│     fun handleClick() { viewModelScope.launch { ... } } │
│                                                          │
│  4. Actualiza estado interno                            │
│     _state.value = _state.value.copy(isLoading = true)  │
└─────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────┐
│                     REPOSITORY                           │
│  Lógica de datos (API, Database, etc.)                 │
└─────────────────────────────────────────────────────────┘
```

#### 4. **Ejemplo Completo: CartViewModel ↔ CartScreen**

```kotlin
// CartViewModel.kt
class CartViewModel : ViewModel() {
    private val _cartState = MutableStateFlow(CartState())
    val cartState: StateFlow<CartState> = _cartState.asStateFlow()
    
    fun addToCart(product: Product) {
        val currentItems = _cartState.value.items.toMutableList()
        currentItems.add(CartItem(product, 1))
        _cartState.value = _cartState.value.copy(items = currentItems)
    }
    
    suspend fun processOrder(userId: Int, address: String, payment: PaymentMethod) {
        _cartState.value = _cartState.value.copy(isLoading = true)
        
        try {
            val order = cartRepository.createOrder(userId, address, payment)
            _cartState.value = _cartState.value.copy(
                orderCompleted = order,
                isLoading = false
            )
        } catch (e: Exception) {
            _cartState.value = _cartState.value.copy(
                error = e.message,
                isLoading = false
            )
        }
    }
}

// CartScreen.kt
@Composable
fun CartScreen(viewModel: CartViewModel = viewModel()) {
    val cartState by viewModel.cartState.collectAsState()
    
    // UI reacciona al estado
    if (cartState.isLoading) {
        CircularProgressIndicator()
    }
    
    LazyColumn {
        items(cartState.items) { item ->
            CartItemCard(
                item = item,
                onRemove = { viewModel.removeFromCart(item.product.id) },
                onQuantityChange = { newQty -> 
                    viewModel.updateQuantity(item.product.id, newQty)
                }
            )
        }
    }
    
    // Reacción a orden completada
    LaunchedEffect(cartState.orderCompleted) {
        cartState.orderCompleted?.let {
            // Mostrar mensaje de éxito y navegar
        }
    }
}
```

---

## 5) Persistencia Local (SQLite)

### a) Configuración de Base de Datos

#### 1. **Dependencias en build.gradle.kts**
```kotlin
dependencies {
    // Room components
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}

// Plugin KSP para generación de código
plugins {
    id("com.google.devtools.ksp") version "2.0.21-1.0.27"
}
```

#### 2. **Clase de Base de Datos (AppDatabase.kt)**
```kotlin
@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        CartItemEntity::class,
        OrderEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "plantbuddy_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

### b) Configuración de Librerías

#### 1. **Room - ORM para SQLite**
```kotlin
// Versión: 2.6.1
// Propósito: Abstracción sobre SQLite con validación en tiempo de compilación

implementation("androidx.room:room-runtime:2.6.1")  // Runtime
implementation("androidx.room:room-ktx:2.6.1")      // Extensiones Kotlin (Coroutines, Flow)
ksp("androidx.room:room-compiler:2.6.1")            // Generador de código
```

#### 2. **KSP (Kotlin Symbol Processing)**
```kotlin
// Reemplaza KAPT, más rápido para generación de código
id("com.google.devtools.ksp") version "2.0.21-1.0.27"
```

#### 3. **Coroutines para operaciones asíncronas**
```kotlin
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

### c) Configuración de DAO, Repository y Entidades

#### 1. **Entidades (Tablas)**

```kotlin
// UserEntity.kt
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "email")
    val email: String,
    
    @ColumnInfo(name = "username")
    val username: String,
    
    @ColumnInfo(name = "password_hash")
    val passwordHash: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "profile_image_url")
    val profileImageUrl: String? = null
)

// ProductEntity.kt
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: Int,
    
    @ColumnInfo(name = "nombre")
    val nombre: String,
    
    @ColumnInfo(name = "descripcion")
    val descripcion: String?,
    
    @ColumnInfo(name = "precio")
    val precio: Double,
    
    @ColumnInfo(name = "stock")
    val stock: Int,
    
    @ColumnInfo(name = "imagen_url")
    val imagenUrl: String?,
    
    @ColumnInfo(name = "category_id")
    val categoryId: Int?,
    
    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis()
)

// CartItemEntity.kt
@Entity(
    tableName = "cart_items",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("product_id")]
)
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "product_id")
    val productId: Int,
    
    @ColumnInfo(name = "quantity")
    val quantity: Int,
    
    @ColumnInfo(name = "added_at")
    val addedAt: Long = System.currentTimeMillis()
)
```

#### 2. **DAO (Data Access Objects)**

```kotlin
// UserDao.kt
@Dao
interface UserDao {
    // CREATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity): Long
    
    // READ
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): UserEntity?
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?
    
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>
    
    // UPDATE
    @Update
    suspend fun update(user: UserEntity)
    
    @Query("UPDATE users SET username = :newUsername WHERE id = :userId")
    suspend fun updateUsername(userId: Int, newUsername: String)
    
    // DELETE
    @Delete
    suspend fun delete(user: UserEntity)
    
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteById(userId: Int)
}

// ProductDao.kt
@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)
    
    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: Int): ProductEntity?
    
    @Query("SELECT * FROM products ORDER BY nombre ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>
    
    @Query("SELECT * FROM products WHERE nombre LIKE '%' || :searchQuery || '%'")
    fun searchProducts(searchQuery: String): Flow<List<ProductEntity>>
    
    @Query("DELETE FROM products WHERE cached_at < :timestamp")
    suspend fun deleteOldCache(timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int
}

// CartDao.kt
@Dao
interface CartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity): Long
    
    @Query("""
        SELECT ci.*, p.* 
        FROM cart_items ci 
        INNER JOIN products p ON ci.product_id = p.id
        ORDER BY ci.added_at DESC
    """)
    fun getCartItemsWithProducts(): Flow<Map<CartItemEntity, ProductEntity>>
    
    @Query("SELECT * FROM cart_items")
    fun getAllCartItems(): Flow<List<CartItemEntity>>
    
    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :itemId")
    suspend fun updateQuantity(itemId: Int, quantity: Int)
    
    @Delete
    suspend fun deleteCartItem(item: CartItemEntity)
    
    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}
```

#### 3. **Repository - Capa de Abstracción**

```kotlin
// UserRepository.kt
class UserRepository(private val userDao: UserDao) {
    
    // Operaciones CRUD
    suspend fun createUser(email: String, username: String, password: String): Long {
        val passwordHash = hashPassword(password)
        val user = UserEntity(
            email = email,
            username = username,
            passwordHash = passwordHash
        )
        return userDao.insert(user)
    }
    
    suspend fun getUserById(userId: Int): UserEntity? {
        return userDao.getUserById(userId)
    }
    
    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email)
    }
    
    fun getAllUsers(): Flow<List<UserEntity>> {
        return userDao.getAllUsers()
    }
    
    suspend fun updateUser(user: UserEntity) {
        userDao.update(user)
    }
    
    suspend fun deleteUser(userId: Int) {
        userDao.deleteById(userId)
    }
    
    // Lógica de negocio
    suspend fun authenticateUser(email: String, password: String): UserEntity? {
        val user = userDao.getUserByEmail(email) ?: return null
        return if (verifyPassword(password, user.passwordHash)) user else null
    }
    
    private fun hashPassword(password: String): String {
        // Implementación de hashing (BCrypt, SHA256, etc.)
        return password // Simplificado
    }
    
    private fun verifyPassword(password: String, hash: String): Boolean {
        return hashPassword(password) == hash
    }
}

// ProductRepository.kt
class ProductRepository(
    private val productDao: ProductDao,
    private val apiService: ProductoApiService
) {
    
    // Patrón Single Source of Truth
    fun getProducts(): Flow<List<ProductEntity>> {
        return productDao.getAllProducts()
    }
    
    suspend fun refreshProducts() {
        try {
            // Obtener de API
            val response = apiService.getAllProducts()
            if (response.isSuccessful && response.body() != null) {
                // Mapear DTOs a Entities
                val entities = response.body()!!.map { dto ->
                    ProductEntity(
                        id = dto.id,
                        nombre = dto.nombre,
                        descripcion = dto.descripcion,
                        precio = dto.precio,
                        stock = dto.stock,
                        imagenUrl = dto.imagenUrl,
                        categoryId = dto.categoria?.id
                    )
                }
                // Guardar en base de datos local
                productDao.insertAll(entities)
            }
        } catch (e: Exception) {
            // Manejar error, la UI seguirá mostrando datos cacheados
            Log.e("ProductRepository", "Error refreshing products", e)
        }
    }
    
    suspend fun getProductById(productId: Int): ProductEntity? {
        return productDao.getProductById(productId)
    }
    
    fun searchProducts(query: String): Flow<List<ProductEntity>> {
        return productDao.searchProducts(query)
    }
    
    suspend fun cleanOldCache(daysOld: Int = 7) {
        val timestamp = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000)
        productDao.deleteOldCache(timestamp)
    }
}

// CartRepository.kt (con persistencia local)
class CartRepository(
    private val cartDao: CartDao,
    private val compraApiService: CompraApiService
) {
    
    fun getCartItems(): Flow<List<CartItemEntity>> {
        return cartDao.getAllCartItems()
    }
    
    suspend fun addToCart(productId: Int, quantity: Int = 1) {
        val item = CartItemEntity(
            productId = productId,
            quantity = quantity
        )
        cartDao.insertCartItem(item)
    }
    
    suspend fun updateQuantity(itemId: Int, quantity: Int) {
        if (quantity <= 0) {
            // Si cantidad es 0, eliminar del carrito
            val item = CartItemEntity(id = itemId, productId = 0, quantity = 0)
            cartDao.deleteCartItem(item)
        } else {
            cartDao.updateQuantity(itemId, quantity)
        }
    }
    
    suspend fun removeFromCart(itemId: Int) {
        val item = CartItemEntity(id = itemId, productId = 0, quantity = 0)
        cartDao.deleteCartItem(item)
    }
    
    suspend fun clearCart() {
        cartDao.clearCart()
    }
    
    suspend fun checkout(userId: Int, address: String, paymentMethod: String): Boolean {
        try {
            // 1. Obtener items del carrito
            val cartItems = cartDao.getAllCartItems().first()
            
            // 2. Crear request para API
            val request = CreateCompraRequest(
                userId = userId,
                shippingAddress = address,
                paymentMethod = paymentMethod,
                items = cartItems.map { 
                    CartItemRequest(it.productId, it.quantity) 
                }
            )
            
            // 3. Enviar al backend
            val response = compraApiService.createCompra(request)
            
            // 4. Si exitoso, limpiar carrito local
            if (response.isSuccessful) {
                clearCart()
                return true
            }
            return false
        } catch (e: Exception) {
            Log.e("CartRepository", "Checkout failed", e)
            return false
        }
    }
}
```

### d) Comunicación ViewModel con Persistencia

#### 1. **ViewModel usando Repository**

```kotlin
// ProductViewModel.kt
class ProductViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {
    
    // Estado observable desde la UI
    private val _productsState = MutableStateFlow<ProductsState>(ProductsState.Loading)
    val productsState: StateFlow<ProductsState> = _productsState.asStateFlow()
    
    init {
        loadProducts()
    }
    
    private fun loadProducts() {
        viewModelScope.launch {
            // Observar Flow de la base de datos
            productRepository.getProducts()
                .catch { e ->
                    _productsState.value = ProductsState.Error(e.message ?: "Error desconocido")
                }
                .collect { products ->
                    _productsState.value = ProductsState.Success(products)
                }
        }
        
        // Refrescar desde API en segundo plano
        viewModelScope.launch {
            productRepository.refreshProducts()
        }
    }
    
    fun searchProducts(query: String) {
        viewModelScope.launch {
            productRepository.searchProducts(query)
                .collect { products ->
                    _productsState.value = ProductsState.Success(products)
                }
        }
    }
}

sealed class ProductsState {
    object Loading : ProductsState()
    data class Success(val products: List<ProductEntity>) : ProductsState()
    data class Error(val message: String) : ProductsState()
}

// CartViewModel.kt con persistencia
class CartViewModel(
    private val cartRepository: CartRepository
) : ViewModel() {
    
    private val _cartState = MutableStateFlow(CartState())
    val cartState: StateFlow<CartState> = _cartState.asStateFlow()
    
    init {
        loadCartItems()
    }
    
    private fun loadCartItems() {
        viewModelScope.launch {
            cartRepository.getCartItems()
                .collect { items ->
                    _cartState.value = _cartState.value.copy(items = items)
                }
        }
    }
    
    fun addToCart(productId: Int, quantity: Int = 1) {
        viewModelScope.launch {
            try {
                cartRepository.addToCart(productId, quantity)
                _cartState.value = _cartState.value.copy(
                    message = "Producto agregado al carrito"
                )
            } catch (e: Exception) {
                _cartState.value = _cartState.value.copy(
                    error = "Error al agregar producto"
                )
            }
        }
    }
    
    fun updateQuantity(itemId: Int, quantity: Int) {
        viewModelScope.launch {
            cartRepository.updateQuantity(itemId, quantity)
        }
    }
    
    fun removeItem(itemId: Int) {
        viewModelScope.launch {
            cartRepository.removeFromCart(itemId)
        }
    }
    
    fun checkout(userId: Int, address: String, payment: String) {
        viewModelScope.launch {
            _cartState.value = _cartState.value.copy(isLoading = true)
            
            val success = cartRepository.checkout(userId, address, payment)
            
            _cartState.value = _cartState.value.copy(
                isLoading = false,
                checkoutSuccess = success,
                message = if (success) "Compra realizada" else "Error en compra"
            )
        }
    }
}
```

#### 2. **Flujo Completo de Datos**

```
┌──────────────────────────────────────────────────────────────┐
│                        COMPOSABLE (UI)                        │
│                                                               │
│  val productsState by viewModel.productsState.collectAsState()│
│                                                               │
│  LazyColumn {                                                 │
│    items(productsState.products) { product ->                │
│      ProductCard(                                            │
│        product = product,                                    │
│        onClick = { viewModel.addToCart(product.id) }         │
│      )                                                       │
│    }                                                         │
│  }                                                           │
└──────────────────────────────────────────────────────────────┘
                            ↕ StateFlow<ProductsState>
┌──────────────────────────────────────────────────────────────┐
│                         VIEWMODEL                             │
│                                                               │
│  private val _productsState = MutableStateFlow(Loading)      │
│  val productsState: StateFlow = _productsState.asStateFlow() │
│                                                               │
│  init {                                                       │
│    viewModelScope.launch {                                   │
│      productRepository.getProducts()                         │
│        .collect { products ->                                │
│          _productsState.value = Success(products)            │
│        }                                                     │
│    }                                                         │
│  }                                                           │
│                                                               │
│  fun addToCart(productId: Int) {                            │
│    viewModelScope.launch {                                   │
│      cartRepository.addToCart(productId)                     │
│    }                                                         │
│  }                                                           │
└──────────────────────────────────────────────────────────────┘
                            ↕ suspend functions / Flow
┌──────────────────────────────────────────────────────────────┐
│                        REPOSITORY                             │
│                                                               │
│  fun getProducts(): Flow<List<ProductEntity>> {              │
│    return productDao.getAllProducts()  // Flow desde Room    │
│  }                                                           │
│                                                               │
│  suspend fun refreshProducts() {                             │
│    val response = apiService.getAllProducts()                │
│    productDao.insertAll(response.body()!!.map { ... })       │
│  }                                                           │
│                                                               │
│  suspend fun addToCart(productId: Int) {                    │
│    cartDao.insertCartItem(CartItemEntity(...))               │
│  }                                                           │
└──────────────────────────────────────────────────────────────┘
                            ↕ SQL Queries
┌──────────────────────────────────────────────────────────────┐
│                           DAO                                 │
│                                                               │
│  @Query("SELECT * FROM products")                            │
│  fun getAllProducts(): Flow<List<ProductEntity>>             │
│                                                               │
│  @Insert(onConflict = OnConflictStrategy.REPLACE)            │
│  suspend fun insertAll(products: List<ProductEntity>)        │
│                                                               │
│  @Insert                                                      │
│  suspend fun insertCartItem(item: CartItemEntity)            │
└──────────────────────────────────────────────────────────────┘
                            ↕ Room
┌──────────────────────────────────────────────────────────────┐
│                    SQLite DATABASE                            │
│                                                               │
│  ┌──────────────────┐  ┌──────────────────┐                 │
│  │   products       │  │   cart_items     │                 │
│  ├──────────────────┤  ├──────────────────┤                 │
│  │ id (PK)          │  │ id (PK)          │                 │
│  │ nombre           │  │ product_id (FK)  │                 │
│  │ precio           │  │ quantity         │                 │
│  │ stock            │  │ added_at         │                 │
│  └──────────────────┘  └──────────────────┘                 │
└──────────────────────────────────────────────────────────────┘
```

### Pruebas CRUD Completas

#### Ejemplo 1: Inserción y Consulta de Productos
```kotlin
// En ViewModel o Repository
viewModelScope.launch {
    // INSERT
    val products = listOf(
        ProductEntity(1, "Monstera", "Planta tropical", 15000.0, 10, null, 1),
        ProductEntity(2, "Pothos", "Planta de interior", 8000.0, 15, null, 1)
    )
    productDao.insertAll(products)
    
    // SELECT ALL
    productDao.getAllProducts().collect { allProducts ->
        println("Total productos: ${allProducts.size}")
    }
    
    // SELECT BY ID
    val product = productDao.getProductById(1)
    println("Producto encontrado: ${product?.nombre}")
    
    // SEARCH
    productDao.searchProducts("mons").collect { results ->
        println("Resultados búsqueda: ${results.size}")
    }
}
```

#### Ejemplo 2: Operaciones con Carrito
```kotlin
viewModelScope.launch {
    // INSERT en carrito
    val item = CartItemEntity(productId = 1, quantity = 2)
    val itemId = cartDao.insertCartItem(item)
    
    // UPDATE cantidad
    cartDao.updateQuantity(itemId.toInt(), 3)
    
    // SELECT items del carrito
    cartDao.getAllCartItems().collect { items ->
        items.forEach { item ->
            println("Producto ${item.productId}, cantidad: ${item.quantity}")
        }
    }
    
    // DELETE item específico
    cartDao.deleteCartItem(item)
    
    // DELETE ALL (limpiar carrito)
    cartDao.clearCart()
}
```

#### Ejemplo 3: Autenticación de Usuario
```kotlin
// Registro (INSERT)
suspend fun registerUser(email: String, username: String, password: String) {
    val userId = userRepository.createUser(email, username, password)
    println("Usuario creado con ID: $userId")
}

// Login (SELECT con validación)
suspend fun loginUser(email: String, password: String) {
    val user = userRepository.authenticateUser(email, password)
    if (user != null) {
        println("Login exitoso: ${user.username}")
    } else {
        println("Credenciales inválidas")
    }
}

// Actualizar perfil (UPDATE)
suspend fun updateProfile(userId: Int, newUsername: String) {
    userDao.updateUsername(userId, newUsername)
    println("Username actualizado")
}
```

---

## 6) Arquitectura MVVM

### Explicación del Patrón

**MVVM (Model-View-ViewModel)** es un patrón arquitectónico que separa la lógica de presentación de la lógica de negocio y los datos.

```
┌─────────────────────────────────────────────────────────────┐
│                         MVVM PATTERN                         │
└─────────────────────────────────────────────────────────────┘

┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│    VIEW      │ observe │  VIEWMODEL   │  uses   │    MODEL     │
│ (Composable) │ ──────> │              │ ──────> │ (Repository) │
│              │         │              │         │              │
│  - UI only   │  emit   │  - State     │ return  │  - Data      │
│  - No logic  │ <────── │  - Logic     │ <────── │  - Business  │
│              │ actions │  - Transform │  data   │              │
└──────────────┘         └──────────────┘         └──────────────┘
```

### Componentes del Patrón

#### 1. **MODEL (Modelo)**

Representa los datos y la lógica de negocio.

**Componentes en PlantBuddy**:
- **Entities**: Estructuras de datos (UserEntity, ProductEntity)
- **DTOs**: Objetos de transferencia de datos para API
- **Repository**: Abstracción de fuentes de datos
- **Data Sources**: Room Database, Retrofit API

```kotlin
// Entity (datos locales)
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val precio: Double,
    val stock: Int
)

// DTO (datos de API)
data class ProductoDTO(
    val id: Int,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val categoria: CategoriaDTO?
)

// Repository (fuente única de verdad)
class ProductRepository(
    private val productDao: ProductDao,
    private val apiService: ProductoApiService
) {
    fun getProducts(): Flow<List<ProductEntity>> {
        return productDao.getAllProducts()
    }
    
    suspend fun refreshFromApi() {
        val response = apiService.getAllProducts()
        productDao.insertAll(response.body()!!.toEntities())
    }
}
```

#### 2. **VIEW (Vista)**

Componentes UI que observan el estado y reaccionan a cambios.

**Características**:
- Solo renderiza UI basada en estado
- No contiene lógica de negocio
- Notifica eventos al ViewModel
- Observa StateFlow/LiveData del ViewModel

```kotlin
@Composable
fun ProductListScreen(
    viewModel: ProductViewModel = viewModel()
) {
    // OBSERVA estado del ViewModel
    val productsState by viewModel.productsState.collectAsState()
    
    // RENDERIZA basado en estado
    when (productsState) {
        is ProductsState.Loading -> {
            CircularProgressIndicator()
        }
        is ProductsState.Success -> {
            LazyColumn {
                items(productsState.products) { product ->
                    ProductCard(
                        product = product,
                        // NOTIFICA evento al ViewModel
                        onClick = { viewModel.addToCart(product.id) }
                    )
                }
            }
        }
        is ProductsState.Error -> {
            Text("Error: ${productsState.message}")
        }
    }
    
    // REACCIONA a cambios de estado
    LaunchedEffect(viewModel.cartAdded) {
        if (viewModel.cartAdded) {
            // Mostrar Snackbar
        }
    }
}
```

#### 3. **VIEWMODEL (Modelo de Vista)**

Intermediario entre Vista y Modelo. Gestiona el estado de la UI y la lógica de presentación.

**Responsabilidades**:
- Mantener el estado de la UI
- Exponer estado observable (StateFlow/LiveData)
- Procesar eventos de la UI
- Llamar al Repository para operaciones de datos
- Transformar datos del Model para la View
- Sobrevive a cambios de configuración

```kotlin
class ProductViewModel(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository
) : ViewModel() {
    
    // ESTADO PRIVADO (mutable)
    private val _productsState = MutableStateFlow<ProductsState>(ProductsState.Loading)
    private val _cartAdded = MutableStateFlow(false)
    
    // ESTADO PÚBLICO (immutable)
    val productsState: StateFlow<ProductsState> = _productsState.asStateFlow()
    val cartAdded: StateFlow<Boolean> = _cartAdded.asStateFlow()
    
    init {
        loadProducts()
    }
    
    // LÓGICA DE PRESENTACIÓN
    private fun loadProducts() {
        viewModelScope.launch {
            // Observar datos del Repository
            productRepository.getProducts()
                .catch { e ->
                    _productsState.value = ProductsState.Error(e.message ?: "Error")
                }
                .collect { products ->
                    // Transformar datos para la vista
                    _productsState.value = ProductsState.Success(products)
                }
            
            // Refrescar desde API en paralelo
            productRepository.refreshFromApi()
        }
    }
    
    // PROCESAR EVENTOS DE LA UI
    fun addToCart(productId: Int) {
        viewModelScope.launch {
            try {
                cartRepository.addToCart(productId, quantity = 1)
                _cartAdded.value = true
            } catch (e: Exception) {
                _productsState.value = ProductsState.Error("Error al agregar")
            }
        }
    }
    
    fun searchProducts(query: String) {
        viewModelScope.launch {
            productRepository.searchProducts(query)
                .collect { results ->
                    _productsState.value = ProductsState.Success(results)
                }
        }
    }
}
```

### Implementación en PlantBuddy

#### Estructura del Proyecto

```
app/src/main/java/cl/duoc/app/
│
├── data/                           # MODEL LAYER
│   ├── model/                      # Entities y Data Classes
│   │   ├── User.kt
│   │   ├── Product.kt
│   │   ├── CartItem.kt
│   │   └── Order.kt
│   │
│   ├── local/                      # Room Database
│   │   ├── AppDatabase.kt
│   │   ├── dao/
│   │   │   ├── UserDao.kt
│   │   │   ├── ProductDao.kt
│   │   │   └── CartDao.kt
│   │   └── entities/
│   │       ├── UserEntity.kt
│   │       └── ProductEntity.kt
│   │
│   ├── api/                        # Retrofit API
│   │   ├── RetrofitClient.kt
│   │   ├── AuthApiService.kt
│   │   ├── ProductoApiService.kt
│   │   └── dto/
│   │       ├── LoginRequest.kt
│   │       ├── ProductoDTO.kt
│   │       └── CompraDTO.kt
│   │
│   └── repository/                 # Repositories
│       ├── AuthRepository.kt
│       ├── ProductRepository.kt
│       └── CartRepository.kt
│
├── ui/                             # VIEW LAYER
│   ├── screens/                    # Composables
│   │   ├── auth/
│   │   │   ├── LoginScreen.kt
│   │   │   └── RegisterScreen.kt
│   │   ├── home/
│   │   │   └── HomeScreen.kt
│   │   ├── cart/
│   │   │   └── CartScreen.kt
│   │   └── profile/
│   │       └── ProfileScreen.kt
│   │
│   ├── components/                 # Reusable Components
│   │   ├── ProductCard.kt
│   │   ├── CartItemCard.kt
│   │   └── CustomButton.kt
│   │
│   └── theme/                      # UI Theme
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
└── viewmodel/                      # VIEWMODEL LAYER
    ├── AuthViewModel.kt
    ├── ProductViewModel.kt
    ├── CartViewModel.kt
    └── ProfileViewModel.kt
```

### Flujo de Datos Completo

#### Ejemplo: Proceso de Compra

```kotlin
// 1. VIEW: Usuario hace clic en "Proceder al Pago"
@Composable
fun CartScreen(viewModel: CartViewModel = viewModel()) {
    val cartState by viewModel.cartState.collectAsState()
    
    Button(
        onClick = { 
            // Usuario inicia checkout
            viewModel.processOrder(
                userId = 1,
                address = "Los Almendros 123",
                payment = PaymentMethod.CREDIT_CARD
            )
        }
    ) {
        Text("Proceder al Pago")
    }
    
    // Observar resultado
    LaunchedEffect(cartState.orderCompleted) {
        cartState.orderCompleted?.let { order ->
            // Navegar a pantalla de confirmación
        }
    }
}

// 2. VIEWMODEL: Procesa la acción y actualiza estado
class CartViewModel(
    private val cartRepository: CartRepository
) : ViewModel() {
    
    private val _cartState = MutableStateFlow(CartState())
    val cartState: StateFlow<CartState> = _cartState.asStateFlow()
    
    fun processOrder(userId: Int, address: String, payment: PaymentMethod) {
        viewModelScope.launch {
            // Actualizar UI a estado de carga
            _cartState.value = _cartState.value.copy(isLoading = true)
            
            try {
                // Llamar al repository
                val order = cartRepository.createOrder(userId, address, payment)
                
                // Actualizar UI con resultado exitoso
                _cartState.value = _cartState.value.copy(
                    isLoading = false,
                    orderCompleted = order,
                    error = null
                )
            } catch (e: Exception) {
                // Actualizar UI con error
                _cartState.value = _cartState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}

// 3. REPOSITORY: Coordina fuentes de datos
class CartRepository(
    private val cartDao: CartDao,
    private val compraApiService: CompraApiService
) {
    suspend fun createOrder(
        userId: Int, 
        address: String, 
        payment: PaymentMethod
    ): Order {
        // 3.1 Obtener items del carrito (SQLite)
        val cartItems = cartDao.getAllCartItems().first()
        
        // 3.2 Crear request para API
        val request = CreateCompraRequest(
            userId = userId,
            shippingAddress = address,
            paymentMethod = payment.name,
            items = cartItems.map { 
                CartItemRequest(it.productId, it.quantity) 
            }
        )
        
        // 3.3 Enviar al backend (API REST)
        val response = compraApiService.createCompra(request)
        
        if (response.isSuccessful && response.body() != null) {
            val compraResponse = response.body()!!
            
            // 3.4 Limpiar carrito local
            cartDao.clearCart()
            
            // 3.5 Crear y retornar Order
            return Order(
                id = compraResponse.id,
                userId = userId,
                items = cartItems.map { /* mapear */ },
                total = compraResponse.total,
                status = OrderStatus.PENDING
            )
        } else {
            throw Exception("Error al crear la compra")
        }
    }
}

// 4. DATA SOURCES: Ejecutan operaciones
// 4.1 Room DAO
@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getAllCartItems(): Flow<List<CartItemEntity>>
    
    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

// 4.2 Retrofit Service
interface CompraApiService {
    @POST("compras/crear")
    suspend fun createCompra(
        @Body request: CreateCompraRequest
    ): Response<CreateCompraResponse>
}
```

### Ventajas de MVVM en PlantBuddy

1. **Separación de Responsabilidades**
   - View: Solo UI, no lógica
   - ViewModel: Lógica de presentación
   - Model: Lógica de negocio y datos

2. **Testabilidad**
   - ViewModels pueden probarse sin UI
   - Repositories pueden mockearse
   - Lógica independiente de Android Framework

3. **Reactividad**
   - StateFlow/Flow propagan cambios automáticamente
   - UI siempre sincronizada con el estado

4. **Supervivencia a Cambios de Configuración**
   - ViewModels sobreviven a rotaciones
   - Estado no se pierde

5. **Mantenibilidad**
   - Código organizado y modular
   - Fácil de entender y modificar

6. **Escalabilidad**
   - Fácil agregar nuevas features
   - Reutilización de componentes

### Diagrama Completo de Arquitectura

```
┌──────────────────────────────────────────────────────────────┐
│                        UI LAYER (View)                        │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐            │
│  │LoginScreen │  │HomeScreen  │  │CartScreen  │            │
│  │            │  │            │  │            │            │
│  │ @Composable│  │ @Composable│  │ @Composable│            │
│  └────────────┘  └────────────┘  └────────────┘            │
│         │               │               │                     │
│         └───────────────┴───────────────┘                     │
│                         │ observes StateFlow                  │
└─────────────────────────┼─────────────────────────────────────┘
                          │
┌─────────────────────────┼─────────────────────────────────────┐
│               PRESENTATION LAYER (ViewModel)                   │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐            │
│  │AuthViewModel│ │ProductVM   │  │CartViewModel│            │
│  │            │  │            │  │            │            │
│  │ StateFlow  │  │ StateFlow  │  │ StateFlow  │            │
│  │ Functions  │  │ Functions  │  │ Functions  │            │
│  └────────────┘  └────────────┘  └────────────┘            │
│         │               │               │                     │
│         └───────────────┴───────────────┘                     │
│                         │ calls suspend functions             │
└─────────────────────────┼─────────────────────────────────────┘
                          │
┌─────────────────────────┼─────────────────────────────────────┐
│                   DOMAIN LAYER (Repository)                    │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐            │
│  │AuthRepo    │  │ProductRepo │  │CartRepo    │            │
│  │            │  │            │  │            │            │
│  │ Single     │  │ Caching    │  │ Checkout   │            │
│  │ Source of  │  │ Strategy   │  │ Logic      │            │
│  │ Truth      │  │            │  │            │            │
│  └────────────┘  └────────────┘  └────────────┘            │
│         │               │               │                     │
│         └───────────────┴───────────────┘                     │
│                         │                                      │
└─────────────────────────┼─────────────────────────────────────┘
                          │
        ┌─────────────────┴─────────────────┐
        │                                     │
┌───────┼──────────────┐            ┌────────┼───────────────┐
│ DATA LAYER (Local)   │            │ DATA LAYER (Remote)    │
│  ┌────────────┐      │            │  ┌────────────┐        │
│  │Room        │      │            │  │Retrofit    │        │
│  │Database    │      │            │  │API         │        │
│  │            │      │            │  │            │        │
│  │ DAO        │      │            │  │ Services   │        │
│  │ Entities   │      │            │  │ DTOs       │        │
│  └────────────┘      │            │  └────────────┘        │
│         │            │            │         │               │
│    SQLite DB         │            │    REST API             │
└──────────────────────┘            └─────────────────────────┘
```

---

## Resumen de Preguntas Clave

### UI y Navegación
1. **Formularios**: TextField, Button, Checkbox, Dialog
2. **Navegación**: NavController, NavHost, BottomNavigation, TopAppBar
3. **Animaciones**: AnimatedVisibility, transitions, CircularProgressIndicator
4. **Comunicación**: StateFlow + collectAsState() + eventos onClick

### Persistencia
1. **Configuración DB**: AppDatabase, @Entity, @Dao
2. **Librerías**: Room 2.6.1, KSP, Coroutines
3. **DAO/Repository**: CRUD operations, Flow observers
4. **ViewModel-Persistencia**: viewModelScope + repository.method()

### MVVM
1. **Separación**: View (UI) + ViewModel (Estado/Lógica) + Model (Datos)
2. **Flujo**: View observa → ViewModel procesa → Repository consulta → DAO ejecuta
3. **Ventajas**: Testeable, Reactivo, Supervive configuraciones, Mantenible

---

**Fecha de creación**: 9 de diciembre de 2025  
**Proyecto**: PlantBuddy - Sistema de Gestión de Plantas  
**Tecnologías**: Kotlin, Jetpack Compose, Room, Retrofit, MVVM
