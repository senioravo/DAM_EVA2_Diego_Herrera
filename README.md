# Plant Buddy 🌱

Una aplicación Android sencilla desarrollada en Kotlin con Jetpack Compose para el cuidado de plantas.

## Características

- **Pantalla de Inicio**: Mensaje de bienvenida a Plant Buddy
- **Navegación Inferior**: Tres secciones principales:
  - 🏠 **Inicio**: Pantalla de bienvenida
  - ❤️ **Cuidado**: Sección para el cuidado de plantas (próximamente)
  - 📋 **Catálogo**: Catálogo de plantas (próximamente)
- **Tema Personalizado**: Colores inspirados en la naturaleza (verdes y marrones)
- **Material Design 3**: UI moderna siguiendo las últimas guías de diseño

## Tecnologías Utilizadas

- **Kotlin**: Lenguaje de programación principal
- **Jetpack Compose**: Framework de UI moderno
- **Material Design 3**: Sistema de diseño de Google
- **Navigation Compose**: Navegación entre pantallas
- **Android Gradle Plugin 8.12.3**: Herramientas de compilación actualizadas

## Estructura del Proyecto

```
app/src/main/java/cl/duoc/app/
├── MainActivity.kt                    # Actividad principal
├── navigation/
│   └── Navigation.kt                  # Configuración de navegación
├── ui/
│   ├── components/
│   │   └── BottomNavigationBar.kt    # Barra de navegación inferior
│   ├── screens/
│   │   ├── HomeScreen.kt             # Pantalla de inicio
│   │   └── PlaceholderScreen.kt      # Pantallas temporales
│   └── theme/
│       ├── Theme.kt                  # Tema de la aplicación
│       └── Type.kt                   # Tipografía
```

## Configuración Optimizada

### Versiones de Dependencias
- **Compose BOM**: 2024.09.03 (gestión centralizada de versiones)
- **Navigation Compose**: 2.8.3
- **Lifecycle**: 2.8.7
- **Java Target**: 17 (para mejor rendimiento)

### Características de Rendimiento
- **ProGuard**: Configurado para optimizar el APK final
- **R8**: Habilitado para ofuscación y minificación
- **Compose BOM**: Evita conflictos de versiones entre librerías de Compose

## Configuración del Proyecto

### Requisitos
- Android Studio Hedgehog o superior
- SDK de Android 24 (mínimo) - 36 (target)
- Java 17

### Instalación
1. Clona o descarga el proyecto
2. Abre el proyecto en Android Studio
3. Sincroniza las dependencias de Gradle
4. Ejecuta la aplicación

### Compilación
```bash
# Compilación de desarrollo
./gradlew assembleDebug

# Compilación de producción
./gradlew assembleRelease

# Limpieza y compilación completa
./gradlew clean build
```

## Próximas Características

- **Gestión de Plantas**: Añadir, editar y eliminar plantas
- **Recordatorios de Riego**: Notificaciones personalizadas
- **Catálogo de Plantas**: Base de datos con información de plantas
- **Registro Fotográfico**: Seguimiento visual del crecimiento
- **Consejos de Cuidado**: Tips personalizados según la planta

## Contribución

Este proyecto es parte de una evaluación académica. Las mejoras y sugerencias son bienvenidas.

## Licencia

Proyecto educativo - DAM EVA2 Diego Herrera