# 🌱 Base de Datos PlantBuddy - Catálogo de Plantas

## ✅ Cambios Realizados

### 1. Actualización de Productos en Android App

Los productos en `ProductRepository.kt` han sido actualizados con los nuevos 7 productos:

1. **Viburnum Lucidum** - $24,990 (Arbustos)
2. **Kniphofia Uvaria** - $19,990 (Perennes)
3. **Rhus Crenata** - $17,990 (Arbustos)
4. **Lavanda Dentata** - $15,990 (Aromáticas)
5. **Laurel de Flor Enano** - $13,990 (Ornamentales)
6. **Pitosporo Tobira Enano** - $16,990 (Ornamentales)
7. **Bignonia Naranja** - $21,990 (Trepadoras)

### 2. Nuevas Categorías

Se han actualizado las categorías:
- ✅ Arbustos
- ✅ Perennes
- ✅ Aromáticas
- ✅ Ornamentales
- ✅ Trepadoras

### 3. Script SQL Completo

Se ha creado `database/neon_plantbuddy_schema.sql` que incluye:

#### Estructura de Tablas:
- **catalogo.categorias** - Categorías de plantas
- **catalogo.productos** - Productos del catálogo
- **catalogo.plantas_detalle** - Detalles técnicos de cada planta

#### Características del Script:
- ✅ Creación de esquema `catalogo`
- ✅ Tablas con relaciones y constraints
- ✅ Los 7 productos con todos sus detalles
- ✅ Índices para optimizar consultas
- ✅ Vista `v_productos_completos` para consultas rápidas
- ✅ Función `buscar_productos()` para búsquedas avanzadas
- ✅ Manejo de conflictos con `ON CONFLICT DO NOTHING`

## 🚀 Cómo Usar el Script SQL en Neon

### Opción 1: Usar la Consola SQL de Neon (Recomendado)

1. Ve a https://console.neon.tech/
2. Selecciona tu proyecto
3. Ve a la pestaña **SQL Editor**
4. Copia todo el contenido de `database/neon_plantbuddy_schema.sql`
5. Pégalo en el editor
6. Haz clic en **Run** o presiona `Ctrl+Enter`

### Opción 2: Usar psql (Cliente PostgreSQL)

```bash
psql "postgresql://neondb_owner:npg_R7m8bHdfNyLW@ep-rapid-rice-a3hr3zr8-pooler.sa-east-1.aws.neon.tech/neondb?sslmode=require" -f database/neon_plantbuddy_schema.sql
```

### Opción 3: Usar DBeaver / pgAdmin

1. Conecta a tu base de datos Neon con el connection string
2. Abre el archivo `neon_plantbuddy_schema.sql`
3. Ejecuta el script completo

## 📊 Verificar que Todo Funcionó

Después de ejecutar el script, verifica con estas consultas:

```sql
-- Ver todas las categorías
SELECT * FROM catalogo.categorias;

-- Ver todos los productos
SELECT 
    p.id, 
    p.nombre, 
    p.precio, 
    p.stock, 
    c.nombre as categoria,
    p.rating
FROM catalogo.productos p
LEFT JOIN catalogo.categorias c ON p.categoria_id = c.id
ORDER BY p.id;

-- Ver productos con detalles completos
SELECT * FROM catalogo.v_productos_completos;

-- Buscar productos
SELECT * FROM catalogo.buscar_productos('lavanda');
```

Deberías ver 7 productos con todos sus detalles.

## 🔗 Conectar Android App con Neon

**IMPORTANTE:** No puedes conectar directamente desde Android a PostgreSQL. Necesitas crear un backend API intermediario.

### Paso 1: Crear Backend API (Node.js)

Crea un archivo `server.js`:

```javascript
const express = require('express');
const { Pool } = require('pg');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

const pool = new Pool({
  connectionString: 'postgresql://neondb_owner:npg_R7m8bHdfNyLW@ep-rapid-rice-a3hr3zr8-pooler.sa-east-1.aws.neon.tech/neondb?sslmode=require',
  ssl: { rejectUnauthorized: false }
});

// Obtener todos los productos
app.get('/api/products', async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT 
        p.id,
        p.nombre as name,
        p.descripcion as description,
        p.precio as price,
        p.stock,
        c.nombre as category,
        p.imagen_url as "imageUrl",
        p.rating
      FROM catalogo.productos p
      LEFT JOIN catalogo.categorias c ON p.categoria_id = c.id
      WHERE p.disponible = TRUE
      ORDER BY p.id
    `);
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error al obtener productos' });
  }
});

// Buscar productos
app.get('/api/products/search', async (req, res) => {
  const { q } = req.query;
  try {
    const result = await pool.query(`
      SELECT 
        p.id,
        p.nombre as name,
        p.descripcion as description,
        p.precio as price,
        p.stock,
        c.nombre as category,
        p.imagen_url as "imageUrl",
        p.rating
      FROM catalogo.productos p
      LEFT JOIN catalogo.categorias c ON p.categoria_id = c.id
      WHERE p.disponible = TRUE
      AND (p.nombre ILIKE $1 OR p.descripcion ILIKE $1)
      ORDER BY p.rating DESC
    `, [`%${q}%`]);
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error en búsqueda' });
  }
});

// Filtrar por categoría
app.get('/api/products/category', async (req, res) => {
  const { category } = req.query;
  try {
    const result = await pool.query(`
      SELECT 
        p.id,
        p.nombre as name,
        p.descripcion as description,
        p.precio as price,
        p.stock,
        c.nombre as category,
        p.imagen_url as "imageUrl",
        p.rating
      FROM catalogo.productos p
      LEFT JOIN catalogo.categorias c ON p.categoria_id = c.id
      WHERE p.disponible = TRUE
      AND c.nombre ILIKE $1
      ORDER BY p.id
    `, [category]);
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error al filtrar' });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`🌱 PlantBuddy API corriendo en puerto ${PORT}`);
});
```

### Paso 2: Instalar Dependencias

```bash
npm init -y
npm install express pg cors
```

### Paso 3: Ejecutar Localmente

```bash
node server.js
```

### Paso 4: Desplegar en Render.com (Gratis)

1. Crea cuenta en https://render.com
2. Crea nuevo **Web Service**
3. Conecta tu repositorio Git
4. Configura:
   - **Build Command:** `npm install`
   - **Start Command:** `node server.js`
5. Deploy!

### Paso 5: Actualizar Android App

En `RetrofitClient.kt`, reemplaza:

```kotlin
private const val BASE_URL = "https://tu-backend.onrender.com/api/"
```

## 📱 Estado Actual de la App

### ✅ Funcionando con Mock Data
- Catálogo con 7 productos nuevos
- Búsqueda funcional
- Filtros por categoría
- UI completa y responsive

### 🔄 Pendiente
- Crear backend API
- Desplegar backend
- Conectar app con backend real

## 📋 Detalle de Productos en BD

| ID | Nombre | Precio | Stock | Categoría | Rating |
|----|--------|--------|-------|-----------|--------|
| 1 | Viburnum Lucidum | $24,990 | 10 | Arbustos | 4.8 |
| 2 | Kniphofia Uvaria | $19,990 | 8 | Perennes | 4.6 |
| 3 | Rhus Crenata | $17,990 | 12 | Arbustos | 4.5 |
| 4 | Lavanda Dentata | $15,990 | 20 | Aromáticas | 4.9 |
| 5 | Laurel de Flor Enano | $13,990 | 15 | Ornamentales | 4.4 |
| 6 | Pitosporo Tobira Enano | $16,990 | 18 | Ornamentales | 4.7 |
| 7 | Bignonia Naranja | $21,990 | 9 | Trepadoras | 4.8 |

## 🔍 Detalles Técnicos de Plantas

Cada producto incluye información detallada en `catalogo.plantas_detalle`:

- Nombre científico
- Tipo de planta
- Requerimientos de luz
- Frecuencia de riego
- Rango de temperatura (min/max)
- Toxicidad
- Altura promedio
- Cuidados específicos

## 📝 Notas Importantes

⚠️ **Seguridad:**
- El connection string está expuesto en este README solo para desarrollo
- En producción, usa variables de entorno
- Nunca expongas credenciales en el código de la app Android

✅ **Performance:**
- El script incluye índices optimizados
- Vista `v_productos_completos` para consultas rápidas
- Función `buscar_productos()` con búsqueda eficiente

🔄 **Mantenimiento:**
- El script es idempotente (se puede ejecutar múltiples veces)
- Usa `ON CONFLICT DO NOTHING` para evitar duplicados
- Las tablas tienen timestamps automáticos

## 🆘 Soporte

Si necesitas ayuda:
1. Revisa los logs del script SQL en Neon
2. Verifica que los 7 productos se insertaron correctamente
3. Prueba las consultas de verificación
4. Contacta si necesitas ayuda con el backend API
