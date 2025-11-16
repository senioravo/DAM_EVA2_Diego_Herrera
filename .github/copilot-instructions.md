# Instrucciones para GitHub Copilot - Plant Buddy

## Documentación de Funcionalidades

Cada vez que se implemente una nueva funcionalidad en el proyecto Plant Buddy, debes actualizar automáticamente el archivo `FUNCIONAMIENTO_APP.md` siguiendo estas reglas:

### Criterios para Documentar

- **Solo funcionalidades**: Documenta únicamente cambios que agreguen o modifiquen funcionalidades de la aplicación (features, comportamientos de UI, integraciones, etc.)
- **No documentar**: Refactorizaciones menores, correcciones de typos, cambios de formato o ajustes de estilo que no afecten la funcionalidad

### Estructura de Documentación

1. **Ubicación**: Agregar la documentación en la sección correspondiente del archivo `FUNCIONAMIENTO_APP.md`
2. **Formato**: Usar títulos jerárquicos apropiados (###, ####) según el nivel de la funcionalidad
3. **Contenido requerido**:
   - Título descriptivo de la funcionalidad
   - Descripción técnica clara y concisa
   - Componentes/archivos involucrados
   - Tecnologías o APIs utilizadas (si aplica)
   - Ejemplo de uso o comportamiento esperado (si es relevante)

### Estilo de Escritura

- Lenguaje técnico profesional
- Orientado a desarrolladores
- Explicaciones claras sobre el "qué" y el "cómo"
- Incluir nombres de clases, funciones y archivos relevantes entre backticks (\`)

### Ejemplo de Formato

```markdown
#### Scroll Collapse en Toolbar (CatalogScreen)

Implementación de comportamiento de ocultación automática del toolbar (búsqueda, filtros y categorías) al hacer scroll hacia abajo en el catálogo de productos. El toolbar reaparece al hacer scroll hacia arriba.

**Componentes**: `CatalogScreen.kt`, `CatalogViewModel.kt`

**Implementación técnica**:
- Uso de `LazyListState` para detectar dirección del scroll
- `derivedStateOf` para calcular el offset de ocultación
- Animación con `animateDpAsState` para transición suave
- Offset negativo aplicado al Column del toolbar para ocultarlo

**Comportamiento**:
- Scroll hacia abajo: Toolbar se oculta progresivamente fuera de la pantalla
- Scroll hacia arriba: Toolbar reaparece con animación suave
```

## Mantenimiento del Archivo

- Mantén la estructura existente del documento
- Agrupa funcionalidades relacionadas bajo secciones comunes
- Actualiza el índice si se agregan nuevas secciones principales
- Preserva el historial de funcionalidades anteriores
