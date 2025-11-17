# Sistema de Autenticación y Ajustes - Plant Buddy

## 📋 Resumen de Implementación

Se ha implementado un sistema completo de autenticación de usuarios y configuración de ajustes siguiendo la arquitectura MVVM.

## 🏗️ Estructura Creada

### 1. Modelos de Datos (`data/model/`)
- **User.kt**: Modelo de usuario con campos:
  - `id`: Int
  - `email`: String
  - `password`: String
  - `profileImageUrl`: String? (opcional)
  - `createdAt`: Date
  - `isAdmin`: Boolean

- **Purchase**: Modelo para registro de compras (preparado para futuras funcionalidades)
  - `id`, `userId`, `productId`, `productName`, `quantity`, `totalPrice`, `purchaseDate`, `status`

### 2. Almacenamiento Local (`data/preferences/`)
- **UserPreferences.kt**: Gestión de preferencias con DataStore
  - `currentUserId`: Flow<Int?>
  - `currentUserEmail`: Flow<String?>
  - `isLoggedIn`: Flow<Boolean>
  - `isDarkModeEnabled`: Flow<Boolean>
  - Funciones: `saveUserSession()`, `clearUserSession()`, `setDarkMode()`

### 3. Repositorio (`data/repository/`)
- **UserRepository.kt**: Gestión de usuarios en memoria
  - Cuenta de Admin pre-creada:
    - Email: `admin@plantbuddy.com`
    - Password: `admin123`
  - Funciones de autenticación: `login()`, `register()`
  - Gestión de perfil: `updateProfileImage()`, `updatePassword()`
  - Gestión de compras: `addPurchase()`, `getUserPurchases()`

### 4. ViewModels (`ui/viewmodel/`)
- **AuthViewModel.kt**
  - Estado: `AuthState` (isLoading, isLoggedIn, currentUser, error)
  - Funciones: `login()`, `register()`, `logout()`, `checkLoginStatus()`
  - Verifica automáticamente sesión guardada en `init`

- **SettingsViewModel.kt**
  - Estado: `SettingsState` (currentUser, isDarkMode, isLoading, messages)
  - Funciones: `toggleDarkMode()`, `updateProfileImage()`, `updatePassword()`

### 5. Pantallas (`ui/screens/`)

#### LoginScreen.kt (`ui/screens/auth/`)
- Campos: Email y Contraseña
- Validación en tiempo real
- Toggle de visibilidad de contraseña
- Card informativa con credenciales de Admin
- Navegación a RegisterScreen
- Auto-login si hay sesión guardada

#### RegisterScreen.kt (`ui/screens/auth/`)
- Campos: Email, Contraseña, Confirmar Contraseña
- Validación de coincidencia de contraseñas
- Mensajes de error contextuales
- Auto-login después del registro exitoso

#### AjustesScreen.kt (Completamente rediseñado)

**SECCIÓN CUENTA:**
- ✅ Imagen de perfil (circular, 100dp)
- ✅ Email del usuario
- ✅ Badge de "Administrador" (si aplica)
- ✅ Botón: "Cambiar imagen de perfil" → Dialog con TextField para URL
- ✅ Botón: "Cambiar contraseña" → Dialog con 3 campos (actual, nueva, confirmar)
- ✅ Botón: "Cerrar sesión" → Dialog de confirmación

**SECCIÓN GENERAL:**
- ✅ Switch: "Modo oscuro" (conectado a DataStore y Theme)
- ✅ Botón: "Comentarios y reclamos" (preparado para implementación futura)
- ✅ Botón: "Ayuda" (preparado para implementación futura)

### 6. Navegación (`navigation/`)
- **AppRoutes.kt**: Agregada ruta `Screen.Register`
- **Navigation.kt**: Actualizada con:
  - Integración de `AuthViewModel` y `SettingsViewModel`
  - Rutas de autenticación (Login, Register)
  - `LaunchedEffect` para navegación automática al Home si hay sesión activa
  - Callback `onLogout` que limpia el stack de navegación

### 7. MainActivity
- Integración de ViewModels en composición
- **PlantBuddyThemeWrapper**: Composable que observa `isDarkModeEnabled` y aplica el tema dinámicamente
- Ocultación de BottomBar en pantallas de Login y Register

## 🎨 Tema Oscuro

El tema oscuro se aplica automáticamente cuando el switch en Ajustes está activado:
- Se guarda en DataStore
- Se observa mediante Flow en `PlantBuddyThemeWrapper`
- Cambia entre `LightColorScheme` y `DarkColorScheme` de `Theme.kt`

## 📦 Dependencias Agregadas

```kotlin
implementation("androidx.datastore:datastore-preferences:1.0.0")
```

## 🔐 Cuenta de Administrador Pre-creada

```
Email: admin@plantbuddy.com
Contraseña: admin123
```

## 🚀 Flujo de Usuario

1. **Primera apertura**: Muestra LoginScreen
2. **Login exitoso**: Guarda sesión en DataStore → Navega a Home
3. **Cierre de app**: Sesión persiste
4. **Reapertura**: Auto-detecta sesión → Navega automáticamente a Home
5. **Ajustes**: 
   - Cambiar imagen de perfil
   - Cambiar contraseña
   - Activar/desactivar modo oscuro
   - Cerrar sesión (limpia DataStore y regresa a Login)

## 📝 Datos Persistentes

- ✅ Email de usuario
- ✅ Contraseña (almacenada en memoria, en producción usar encriptación)
- ✅ Sesión activa (userId, email)
- ✅ Preferencia de modo oscuro
- ✅ Imagen de perfil URL
- 🔄 Datos del Plantel personal (preparado para integración futura)
- 🔄 Registro de compras (estructura lista, pendiente de conexión con catálogo)

## ✨ Características Implementadas

- Sistema de autenticación completo (Login/Register/Logout)
- Persistencia de sesión con DataStore
- Tema oscuro dinámico
- Gestión de perfil de usuario
- Validación de formularios
- Manejo de errores con Snackbar
- Navegación protegida por autenticación
- Cuenta de administrador pre-existente
- UI moderna con Material 3

## 🔮 Preparado para Futuras Funcionalidades

- Sistema de compras (modelos y repository listos)
- Plantel personal por usuario
- Comentarios y reclamos
- Sección de ayuda
- Carga de imágenes desde galería (actualmente por URL)

---

**Fecha de implementación**: 16 de noviembre, 2025
**Arquitectura**: MVVM con Jetpack Compose
**Estado**: ✅ Completamente funcional
