# Guía de Integración con Neon Postgres

## ⚠️ IMPORTANTE: No puedes conectarte directamente desde Android a PostgreSQL

Por razones de seguridad, **NO es posible** conectar directamente desde una aplicación Android a una base de datos PostgreSQL. Necesitas crear un backend API intermediario.

## Estructura Actual (Mock Data)

La app actualmente usa datos mock en `ProductRepository.kt` con 7 productos predefinidos. Todo funciona correctamente con estos datos de prueba.

## Pasos para Conectar con Neon Postgres

### Opción 1: Backend con Node.js + Express (Recomendado)

1. **Crear proyecto Node.js:**
```bash
mkdir plant-buddy-api
cd plant-buddy-api
npm init -y
npm install express pg cors dotenv
```

2. **Crear archivo `server.js`:**
```javascript
const express = require('express');
const { Pool } = require('pg');
const cors = require('cors');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());

// Conexión a Neon Postgres
const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: { rejectUnauthorized: false }
});

// Endpoint para obtener todos los productos
app.get('/api/products', async (req, res) => {
  try {
    const result = await pool.query('SELECT * FROM products');
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error al obtener productos' });
  }
});

// Endpoint para buscar productos
app.get('/api/products/search', async (req, res) => {
  const { q } = req.query;
  try {
    const result = await pool.query(
      'SELECT * FROM products WHERE name ILIKE $1 OR description ILIKE $1',
      [`%${q}%`]
    );
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error en búsqueda' });
  }
});

// Endpoint para filtrar por categoría
app.get('/api/products/category', async (req, res) => {
  const { category } = req.query;
  try {
    const result = await pool.query(
      'SELECT * FROM products WHERE category = $1',
      [category]
    );
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error al filtrar' });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`API corriendo en puerto ${PORT}`);
});
```

3. **Crear archivo `.env`:**
```
DATABASE_URL=postgresql://neondb_owner:npg_R7m8bHdfNyLW@ep-rapid-rice-a3hr3zr8-pooler.sa-east-1.aws.neon.tech/neondb?sslmode=require
PORT=3000
```

4. **Crear tabla en Neon Postgres:**
```sql
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    category VARCHAR(100),
    imageUrl TEXT,
    stock INTEGER DEFAULT 0,
    rating DECIMAL(2, 1) DEFAULT 0
);

-- Insertar los 7 productos
INSERT INTO products (name, description, price, category, imageUrl, stock, rating) VALUES
('Suculenta Echeveria', 'Planta suculenta ideal para interiores con poca luz', 15990.00, 'Suculentas', 'https://images.unsplash.com/photo-1459156212016-c812468e2115?w=400', 25, 4.5),
('Cactus San Pedro', 'Cactus de rápido crecimiento, muy resistente', 12990.00, 'Cactus', 'https://images.unsplash.com/photo-1509423350716-97f9360b4e09?w=400', 15, 4.8),
('Monstera Deliciosa', 'Planta tropical de hojas grandes y decorativas', 24990.00, 'Tropicales', 'https://images.unsplash.com/photo-1614594975525-e45190c55d0b?w=400', 10, 4.9),
('Pothos Dorado', 'Planta colgante de fácil cuidado, purifica el aire', 8990.00, 'Colgantes', 'https://images.unsplash.com/photo-1593482892540-73c9366c9d4b?w=400', 30, 4.7),
('Aloe Vera', 'Planta medicinal con propiedades curativas', 9990.00, 'Suculentas', 'https://images.unsplash.com/photo-1596003906949-67221c37965c?w=400', 20, 4.6),
('Helecho Boston', 'Helecho frondoso ideal para espacios húmedos', 18990.00, 'Helechos', 'https://images.unsplash.com/photo-1585320806297-9794b3e4eeae?w=400', 12, 4.4),
('Cactus Barrel', 'Cactus esférico de crecimiento lento', 13990.00, 'Cactus', 'https://images.unsplash.com/photo-1496889906133-f33f6e0d8c58?w=400', 18, 4.3);
```

5. **Ejecutar el servidor:**
```bash
node server.js
```

6. **Desplegar en Render/Railway/Heroku:**
   - Crea una cuenta en Render.com (gratis)
   - Conecta tu repositorio
   - Agrega la variable de entorno `DATABASE_URL`
   - Deploy automático

### Opción 2: Backend con Spring Boot (Java)

Si prefieres Java, puedo crear un proyecto Spring Boot completo.

## Actualizar la App Android

Una vez que tengas tu backend desplegado:

1. **Edita `RetrofitClient.kt`:**
```kotlin
private const val BASE_URL = "https://tu-api.render.com/api/"
```

2. **Actualiza `ProductRepository.kt`:**
```kotlin
suspend fun getProducts(): Result<List<Product>> {
    return try {
        val products = RetrofitClient.productApi.getProducts()
        Result.success(products)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

## Esquema JSON Esperado

El backend debe devolver productos en este formato:

```json
[
  {
    "id": 1,
    "name": "Suculenta Echeveria",
    "description": "Planta suculenta ideal para interiores",
    "price": 15990.0,
    "category": "Suculentas",
    "imageUrl": "https://...",
    "stock": 25,
    "rating": 4.5
  }
]
```

## Testing

Usa Postman o curl para probar tu API:
```bash
curl https://tu-api.com/api/products
curl https://tu-api.com/api/products/search?q=cactus
curl https://tu-api.com/api/products/category?category=Suculentas
```

## Seguridad

⚠️ **NUNCA expongas credenciales de base de datos en el código Android**
- Usa variables de entorno en el backend
- Implementa autenticación JWT si es necesario
- Agrega rate limiting
- Valida todas las entradas

## Estado Actual

✅ App Android funcionando con mock data
✅ UI completa con búsqueda y filtros
✅ 7 productos de prueba
⏳ Pendiente: Crear backend API
⏳ Pendiente: Conectar con Neon Postgres

## ¿Necesitas ayuda?

Si necesitas que te ayude a crear el backend completo, avísame y puedo generar todo el código necesario.
