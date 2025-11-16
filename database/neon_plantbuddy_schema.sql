-- =====================================================
-- Script SQL para Base de Datos Neon Postgres
-- Catálogo de Plantas PlantBuddy
-- =====================================================

-- Crear esquema si no existe
CREATE SCHEMA IF NOT EXISTS catalogo;

-- =====================================================
-- TABLA: categorias
-- =====================================================
CREATE TABLE IF NOT EXISTS catalogo.categorias (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insertar categorías
INSERT INTO catalogo.categorias (nombre, descripcion) VALUES
    ('Planta', 'Plantas ornamentales y decorativas'),
    ('Arbustos', 'Arbustos de diversos tamaños'),
    ('Perennes', 'Plantas perennes de floración'),
    ('Aromáticas', 'Plantas aromáticas y medicinales'),
    ('Ornamentales', 'Plantas ornamentales para jardín'),
    ('Trepadoras', 'Plantas trepadoras y enredaderas')
ON CONFLICT (nombre) DO NOTHING;

-- =====================================================
-- TABLA: productos
-- =====================================================
CREATE TABLE IF NOT EXISTS catalogo.productos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10, 2) NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    categoria_id INTEGER REFERENCES catalogo.categorias(id),
    disponible BOOLEAN DEFAULT TRUE,
    imagen_url TEXT,
    rating DECIMAL(2, 1) DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_precio_positivo CHECK (precio >= 0),
    CONSTRAINT check_stock_no_negativo CHECK (stock >= 0),
    CONSTRAINT check_rating_rango CHECK (rating >= 0 AND rating <= 5)
);

-- =====================================================
-- TABLA: plantas_detalle
-- =====================================================
CREATE TABLE IF NOT EXISTS catalogo.plantas_detalle (
    id SERIAL PRIMARY KEY,
    producto_id INTEGER UNIQUE REFERENCES catalogo.productos(id) ON DELETE CASCADE,
    nombre_cientifico VARCHAR(255),
    tipo VARCHAR(100),
    luz_requerida VARCHAR(50),
    riego_frecuencia VARCHAR(50),
    temperatura_min DECIMAL(5, 2),
    temperatura_max DECIMAL(5, 2),
    toxicidad BOOLEAN DEFAULT FALSE,
    altura_promedio_cm INTEGER,
    cuidados TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- INSERTAR PRODUCTOS Y DETALLES
-- =====================================================

-- 1) Viburnum Lucidum
INSERT INTO catalogo.productos (nombre, descripcion, precio, stock, categoria_id, disponible, rating)
VALUES (
    'Viburnum Lucidum',
    'Arbusto perenne de hojas brillantes, ideal para interiores o exteriores, sombra parcial o sol, muy decorativo.',
    24990,
    10,
    (SELECT id FROM catalogo.categorias WHERE nombre = 'Planta'),
    TRUE,
    4.8
)
ON CONFLICT DO NOTHING
RETURNING id;

-- Obtener el último ID insertado para plantas_detalle
WITH last_product AS (
    SELECT id FROM catalogo.productos WHERE nombre = 'Viburnum Lucidum' LIMIT 1
)
INSERT INTO catalogo.plantas_detalle (
    producto_id, nombre_cientifico, tipo, luz_requerida, riego_frecuencia,
    temperatura_min, temperatura_max, toxicidad, altura_promedio_cm, cuidados
) 
SELECT 
    id,
    'Viburnum tinus ''Lucidum''',
    'arbusto perenne',
    'media-alta',
    'semanal',
    -8.0,
    30.0,
    FALSE,
    250,
    'Ubicación al sol o sombra parcial, suelo bien drenado ligeramente ácido, poda después de floración'
FROM last_product
ON CONFLICT (producto_id) DO NOTHING;

-- 2) Kniphofia Uvaria
INSERT INTO catalogo.productos (nombre, descripcion, precio, stock, categoria_id, disponible, rating)
VALUES (
    'Kniphofia Uvaria',
    'Planta perenne de floración llamativa tipo "antorcha", ideal para bordes soleados y suelo bien drenado.',
    19990,
    8,
    (SELECT id FROM catalogo.categorias WHERE nombre = 'Planta'),
    TRUE,
    4.6
)
ON CONFLICT DO NOTHING;

WITH last_product AS (
    SELECT id FROM catalogo.productos WHERE nombre = 'Kniphofia Uvaria' LIMIT 1
)
INSERT INTO catalogo.plantas_detalle (
    producto_id, nombre_cientifico, tipo, luz_requerida, riego_frecuencia,
    temperatura_min, temperatura_max, toxicidad, altura_promedio_cm, cuidados
)
SELECT 
    id,
    'Kniphofia uvaria',
    'perenne de floración',
    'alta',
    'moderado',
    -23.0,
    35.0,
    FALSE,
    100,
    'Pleno sol, suelo bien drenado, eliminar flores marchitas para favorecer nueva floración'
FROM last_product
ON CONFLICT (producto_id) DO NOTHING;

-- 3) Rhus Crenata
INSERT INTO catalogo.productos (nombre, descripcion, precio, stock, categoria_id, disponible, rating)
VALUES (
    'Rhus Crenata',
    'Arbusto de hoja perenne, resistente al viento y al sol, ideal para cercos o jardines costeros.',
    17990,
    12,
    (SELECT id FROM catalogo.categorias WHERE nombre = 'Planta'),
    TRUE,
    4.5
)
ON CONFLICT DO NOTHING;

WITH last_product AS (
    SELECT id FROM catalogo.productos WHERE nombre = 'Rhus Crenata' LIMIT 1
)
INSERT INTO catalogo.plantas_detalle (
    producto_id, nombre_cientifico, tipo, luz_requerida, riego_frecuencia,
    temperatura_min, temperatura_max, toxicidad, altura_promedio_cm, cuidados
)
SELECT 
    id,
    'Searsia crenata (= Rhus crenata)',
    'arbusto perenne',
    'alta',
    'moderado',
    -5.0,
    30.0,
    FALSE,
    300,
    'Suelo arenoso o bien drenado, pleno sol, tolera viento y salinidad, ideal para setos costeros'
FROM last_product
ON CONFLICT (producto_id) DO NOTHING;

-- 4) Lavanda Dentata
INSERT INTO catalogo.productos (nombre, descripcion, precio, stock, categoria_id, disponible, rating)
VALUES (
    'Lavanda Dentata',
    'Lavanda francesa de hojas dentadas, arbusto aromático para sol pleno y suelos secos.',
    15990,
    20,
    (SELECT id FROM catalogo.categorias WHERE nombre = 'Planta'),
    TRUE,
    4.9
)
ON CONFLICT DO NOTHING;

WITH last_product AS (
    SELECT id FROM catalogo.productos WHERE nombre = 'Lavanda Dentata' LIMIT 1
)
INSERT INTO catalogo.plantas_detalle (
    producto_id, nombre_cientifico, tipo, luz_requerida, riego_frecuencia,
    temperatura_min, temperatura_max, toxicidad, altura_promedio_cm, cuidados
)
SELECT 
    id,
    'Lavandula dentata',
    'arbusto aromático',
    'alta',
    'baja',
    -5.0,
    35.0,
    FALSE,
    70,
    'Pleno sol, suelo bien drenado, preferiblemente alcalino, evitar suelos ricos o húmedos'
FROM last_product
ON CONFLICT (producto_id) DO NOTHING;

-- 5) Laurel de Flor Enano
INSERT INTO catalogo.productos (nombre, descripcion, precio, stock, categoria_id, disponible, rating)
VALUES (
    'Laurel de Flor Enano',
    'Arbusto ornamental enano de floración, ideal para jardines pequeños o macetas.',
    13990,
    15,
    (SELECT id FROM catalogo.categorias WHERE nombre = 'Planta'),
    TRUE,
    4.4
)
ON CONFLICT DO NOTHING;

WITH last_product AS (
    SELECT id FROM catalogo.productos WHERE nombre = 'Laurel de Flor Enano' LIMIT 1
)
INSERT INTO catalogo.plantas_detalle (
    producto_id, nombre_cientifico, tipo, luz_requerida, riego_frecuencia,
    temperatura_min, temperatura_max, toxicidad, altura_promedio_cm, cuidados
)
SELECT 
    id,
    'Laurus nobilis (var. enano)',
    'arbusto ornamental enano',
    'media',
    'semanal',
    -5.0,
    30.0,
    FALSE,
    60,
    'Maceta o jardín, proteger de heladas fuertes, podar para mantener tamaño compacto'
FROM last_product
ON CONFLICT (producto_id) DO NOTHING;

-- 6) Pitosporo Tobira Enano
INSERT INTO catalogo.productos (nombre, descripcion, precio, stock, categoria_id, disponible, rating)
VALUES (
    'Pitosporo Tobira Enano',
    'Versión enana del Pittosporum tobira, arbusto ornamental perenne, resistente, ideal para cercos, macetas o jardines.',
    16990,
    18,
    (SELECT id FROM catalogo.categorias WHERE nombre = 'Planta'),
    TRUE,
    4.7
)
ON CONFLICT DO NOTHING;

WITH last_product AS (
    SELECT id FROM catalogo.productos WHERE nombre = 'Pitosporo Tobira Enano' LIMIT 1
)
INSERT INTO catalogo.plantas_detalle (
    producto_id, nombre_cientifico, tipo, luz_requerida, riego_frecuencia,
    temperatura_min, temperatura_max, toxicidad, altura_promedio_cm, cuidados
)
SELECT 
    id,
    'Pittosporum tobira (var. enano)',
    'arbusto ornamental enano',
    'media-alta',
    'moderado',
    -5.0,
    30.0,
    FALSE,
    120,
    'Suelo bien drenado, sol o semisombra, excelente para cercos y contenedores, poda ligera después de floración'
FROM last_product
ON CONFLICT (producto_id) DO NOTHING;

-- 7) Bignonia Naranja
INSERT INTO catalogo.productos (nombre, descripcion, precio, stock, categoria_id, disponible, rating)
VALUES (
    'Bignonia Naranja',
    'Planta trepadora de flores naranjas, crecimiento rápido, ideal para pérgolas o muros al sol.',
    21990,
    9,
    (SELECT id FROM catalogo.categorias WHERE nombre = 'Planta'),
    TRUE,
    4.8
)
ON CONFLICT DO NOTHING;

WITH last_product AS (
    SELECT id FROM catalogo.productos WHERE nombre = 'Bignonia Naranja' LIMIT 1
)
INSERT INTO catalogo.plantas_detalle (
    producto_id, nombre_cientifico, tipo, luz_requerida, riego_frecuencia,
    temperatura_min, temperatura_max, toxicidad, altura_promedio_cm, cuidados
)
SELECT 
    id,
    'Tecomaria capensis (o Bignonia naranja)',
    'planta trepadora',
    'alta',
    'moderado',
    -2.0,
    35.0,
    FALSE,
    500,
    'Pleno sol, suelo bien drenado, soporte para trepar, podar tras floración para controlar crecimiento'
FROM last_product
ON CONFLICT (producto_id) DO NOTHING;

-- =====================================================
-- ÍNDICES PARA MEJORAR PERFORMANCE
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_productos_categoria ON catalogo.productos(categoria_id);
CREATE INDEX IF NOT EXISTS idx_productos_disponible ON catalogo.productos(disponible);
CREATE INDEX IF NOT EXISTS idx_productos_nombre ON catalogo.productos(nombre);
CREATE INDEX IF NOT EXISTS idx_plantas_detalle_producto ON catalogo.plantas_detalle(producto_id);

-- =====================================================
-- VISTA: Vista completa de productos con detalles
-- =====================================================
CREATE OR REPLACE VIEW catalogo.v_productos_completos AS
SELECT 
    p.id,
    p.nombre,
    p.descripcion,
    p.precio,
    p.stock,
    p.disponible,
    p.imagen_url,
    p.rating,
    c.nombre as categoria,
    pd.nombre_cientifico,
    pd.tipo,
    pd.luz_requerida,
    pd.riego_frecuencia,
    pd.temperatura_min,
    pd.temperatura_max,
    pd.toxicidad,
    pd.altura_promedio_cm,
    pd.cuidados
FROM catalogo.productos p
LEFT JOIN catalogo.categorias c ON p.categoria_id = c.id
LEFT JOIN catalogo.plantas_detalle pd ON p.id = pd.producto_id
WHERE p.disponible = TRUE;

-- =====================================================
-- FUNCIONES ÚTILES
-- =====================================================

-- Función para buscar productos
CREATE OR REPLACE FUNCTION catalogo.buscar_productos(
    p_termino TEXT,
    p_categoria TEXT DEFAULT NULL
)
RETURNS TABLE (
    id INTEGER,
    nombre VARCHAR(255),
    descripcion TEXT,
    precio DECIMAL(10, 2),
    stock INTEGER,
    categoria VARCHAR(100),
    rating DECIMAL(2, 1)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        p.id,
        p.nombre,
        p.descripcion,
        p.precio,
        p.stock,
        c.nombre as categoria,
        p.rating
    FROM catalogo.productos p
    LEFT JOIN catalogo.categorias c ON p.categoria_id = c.id
    WHERE p.disponible = TRUE
    AND (
        p.nombre ILIKE '%' || p_termino || '%' 
        OR p.descripcion ILIKE '%' || p_termino || '%'
    )
    AND (p_categoria IS NULL OR c.nombre ILIKE p_categoria)
    ORDER BY p.rating DESC, p.nombre;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- CONSULTAS DE VERIFICACIÓN
-- =====================================================

-- Ver todas las categorías
-- SELECT * FROM catalogo.categorias;

-- Ver todos los productos con su categoría
-- SELECT 
--     p.id, 
--     p.nombre, 
--     p.precio, 
--     p.stock, 
--     c.nombre as categoria,
--     p.rating
-- FROM catalogo.productos p
-- LEFT JOIN catalogo.categorias c ON p.categoria_id = c.id
-- ORDER BY p.id;

-- Ver productos con detalles completos
-- SELECT * FROM catalogo.v_productos_completos;

-- Buscar productos (ejemplo)
-- SELECT * FROM catalogo.buscar_productos('lavanda');
-- SELECT * FROM catalogo.buscar_productos('arbusto', 'Planta');

-- =====================================================
-- SCRIPT COMPLETADO
-- =====================================================
-- Este script crea la estructura completa de la base de datos
-- para el catálogo de plantas de PlantBuddy
-- =====================================================
