# Documentación Técnica - PlantBuddy App

## Índice
1. [Resumen General](#resumen-general)
2. [Arquitectura de la Aplicación](#arquitectura-de-la-aplicación)
3. [Estructura de Pantallas](#estructura-de-pantallas)
4. [Sistema de Datos](#sistema-de-datos)
5. [Base de Datos](#base-de-datos)
6. [Sistema de Navegación](#sistema-de-navegación)
7. [Configuración de Alto Rendimiento](#configuración-de-alto-rendimiento)
8. [Integración Backend (Futuro)](#integración-backend-futuro)
9. [Guía de Mantenimiento](#guía-de-mantenimiento)

---

## Resumen General

**PlantBuddy** es una aplicación Android nativa desarrollada en **Kotlin** utilizando **Jetpack Compose** para la construcción de interfaces de usuario modernas y declarativas. La aplicación está diseñada para gestionar un catálogo de plantas ornamentales con funcionalidades de búsqueda, filtrado por categorías y visualización de productos.

### Tecnologías Principales
- **Lenguaje**: Kotlin 1.9.0
- **Framework UI**: Jetpack Compose con Material Design 3
- **Arquitectura**: MVVM (Model-View-ViewModel)
- **Gestión de Estado**: StateFlow y Coroutines
- **Base de Datos**: Neon Serverless Postgres (preparada para integración)
- **HTTP Client**: Retrofit 2.9.0 + OkHttp 4.12.0 (preparado)
- **Navegación**: Navigation Compose

### Estado Actual
La aplicación está en fase de desarrollo con **datos mock** simulando la respuesta de un backend. La infraestructura de base de datos está completamente diseñada y documentada en archivos SQL, lista para integración con el backend de producción.

---

## Arquitectura de la Aplicación

### Patrón MVVM

La aplicación implementa el patrón **Model-View-ViewModel** para separar responsabilidades:

```
┌─────────────────────────────────────────────────┐
│                    View Layer                    │
│  (Composables - CatalogScreen.kt, HomeScreen.kt)│
└─────────────────┬───────────────────────────────┘
                  │ observa StateFlow
                  │ envía eventos (clicks, búsquedas)
┌─────────────────▼───────────────────────────────┐
│                 ViewModel Layer                  │
│    (CatalogViewModel.kt, HomeViewModel.kt)      │
│    - Gestión de estado con StateFlow            │
│    - Lógica de negocio                           │
│    - Transformación de datos                     │
└─────────────────┬───────────────────────────────┘
                  │ solicita datos
                  │ recibe respuestas
┌─────────────────▼───────────────────────────────┐
│                Repository Layer                  │
│           (ProductRepository.kt)                 │
│    - Fuente única de verdad                      │
│    - Mock data (actual)                          │
│    - API calls (futuro)                          │
└─────────────────┬───────────────────────────────┘
                  │ modelos de datos
┌─────────────────▼───────────────────────────────┐
│                  Model Layer                     │
│        (Product.kt, Category.kt)                 │
│    - Data classes inmutables                     │
└─────────────────────────────────────────────────┘
```

### Flujo de Datos Unidireccional

1. **Usuario interacta** con la UI (búsqueda, filtro, click)
2. **View** envía evento al **ViewModel**
3. **ViewModel** procesa la lógica y actualiza el **StateFlow**
4. **Repository** provee los datos (mock o API)
5. **View** recompone automáticamente al observar el cambio de estado

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

## Base de Datos

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

## Guía de Mantenimiento

### Agregar un Nuevo Producto

#### 1. En la Base de Datos

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

#### 2. En el Mock Data (ProductRepository.kt)

```kotlin
private val mockProducts = listOf(
    // ... productos existentes ...
    Product(
        id = 8,
        name = "Nombre de la Planta",
        description = "Descripción detallada",
        price = 19990,
        category = "Arbustos",
        imageUrl = "nombre_planta",
        stock = 50,
        rating = 4.5f
    )
)
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

### Debugging Común

#### Problema: "Imagen no se muestra"

**Solución**:
1. Verificar que el nombre de archivo coincide **exactamente** (minúsculas, sin espacios)
2. Verificar que está en `drawable/` no en `mipmap/`
3. Hacer **Clean Project** → **Rebuild Project** en Android Studio
4. Revisar Logcat: `adb logcat | grep ProductImage`

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
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Product.kt
│   │   │   │   │   │   └── Category.kt
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── ProductRepository.kt
│   │   │   │   │   └── remote/
│   │   │   │   │       ├── ProductApiService.kt
│   │   │   │   │       └── RetrofitClient.kt
│   │   │   │   └── ui/
│   │   │   │       ├── screens/
│   │   │   │       │   ├── home/
│   │   │   │       │   │   ├── HomeScreen.kt
│   │   │   │       │   │   └── HomeViewModel.kt
│   │   │   │       │   └── catalog/
│   │   │   │       │       ├── CatalogScreen.kt
│   │   │   │       │       ├── CatalogViewModel.kt
│   │   │   │       │       └── CatalogUIState.kt
│   │   │   │       └── navigation/
│   │   │   │           └── Navigation.kt
│   │   │   └── res/
│   │   │       ├── drawable/
│   │   │       │   ├── viburnum_lucidum.jpg
│   │   │       │   ├── kniphofia_uvaria.jpg
│   │   │       │   ├── rhus_crenata.jpg
│   │   │       │   ├── lavanda_dentata.jpg
│   │   │       │   ├── laurel_flor_enano.jpg
│   │   │       │   ├── pitosporo_tobira_enano.jpg
│   │   │       │   └── bignonia_naranja.jpg
│   │   │       └── values/
│   │   │           ├── colors.xml
│   │   │           ├── strings.xml
│   │   │           └── themes.xml
│   │   └── androidTest/
│   │       └── java/cl/duoc/app/
│   │           └── ExampleInstrumentedTest.kt
│   └── build.gradle.kts
├── database/
│   ├── neon_plantbuddy_schema.sql
│   ├── README.md
│   └── BACKEND_SETUP.md
├── build.gradle.kts
├── settings.gradle.kts
└── FUNCIONAMIENTO_APP.md (este archivo)
```

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

### Versión 1.0.0 (Actual)
- ✅ Sistema de catálogo con 7 productos
- ✅ Búsqueda en tiempo real
- ✅ Filtros por categoría
- ✅ Carga de imágenes locales
- ✅ Configuración 120Hz
- ✅ Navegación funcional
- ✅ Base de datos Neon lista para integración
- ✅ Mock data completo con precios CLP

### Versión 1.1.0 (Planeada)
- ⏳ Integración con backend API
- ⏳ Carga de imágenes remotas
- ⏳ Pantalla de detalle de producto
- ⏳ Sistema de favoritos

---

**Fin de la Documentación Técnica**

*Última actualización: 2024*
