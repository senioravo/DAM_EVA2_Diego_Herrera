# 🚀 Guía de Instalación - Plant Buddy

Esta guía te ayudará a configurar todo lo necesario para ejecutar el proyecto Plant Buddy en Android Studio desde cero.

---

## 📋 Requisitos Previos

### 1. **Java Development Kit (JDK) 17**

El proyecto requiere JDK 17 para funcionar correctamente.

#### Windows:
1. **Descargar JDK 17**:
   - Visita: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
   - O usa OpenJDK: https://adoptium.net/temurin/releases/?version=17
   
2. **Instalar**:
   - Ejecuta el instalador descargado
   - Nota la ruta de instalación (ej: `C:\Program Files\Java\jdk-17`)

3. **Configurar Variables de Entorno**:
   - Presiona `Win + X` → "Sistema"
   - Click en "Configuración avanzada del sistema"
   - "Variables de entorno"
   - En "Variables del sistema", busca `Path` y haz click en "Editar"
   - Agrega: `C:\Program Files\Java\jdk-17\bin`
   - Click en "Nueva" y crea:
     - **Variable**: `JAVA_HOME`
     - **Valor**: `C:\Program Files\Java\jdk-17`

4. **Verificar instalación**:
   ```powershell
   java -version
   ```
   Deberías ver algo como: `java version "17.0.x"`

---

## 🛠️ Instalación de Android Studio

### 2. **Descargar e Instalar Android Studio**

1. **Descargar**:
   - Visita: https://developer.android.com/studio
   - Descarga la última versión (Hedgehog 2023.1.1 o superior)

2. **Instalar**:
   - Ejecuta el instalador
   - Acepta la instalación de componentes por defecto
   - Incluye:
     - Android SDK
     - Android SDK Platform
     - Android Virtual Device (AVD)

3. **Primera Configuración**:
   - Al abrir Android Studio por primera vez, seguirá un asistente de configuración
   - Selecciona "Standard" installation
   - Acepta las licencias
   - Espera a que descargue todos los componentes necesarios

---

## 📱 Configuración del SDK de Android

### 3. **Configurar Android SDK**

1. **Abrir SDK Manager**:
   - En Android Studio: `Tools` → `SDK Manager`
   - O el icono ⚙️ en la barra superior

2. **Instalar SDK Platforms** (pestaña "SDK Platforms"):
   - ✅ Android 14.0 (API 34) - **Recomendado**
   - ✅ Android 13.0 (API 33)
   - ✅ Android 12.0 (API 31)
   - El proyecto usa `compileSdk = 36` pero puedes usar API 34-35 sin problemas

3. **Instalar SDK Tools** (pestaña "SDK Tools"):
   Marca la casilla "Show Package Details" y asegúrate de tener:
   - ✅ Android SDK Build-Tools 34.0.0 (o superior)
   - ✅ Android SDK Platform-Tools
   - ✅ Android SDK Tools
   - ✅ Android Emulator
   - ✅ Intel x86 Emulator Accelerator (HAXM) - solo en Windows Intel
   - ✅ Android SDK Command-line Tools

4. **Aplicar cambios** y esperar la descarga (puede tomar varios minutos)

---

## 🔧 Abrir el Proyecto en Android Studio

### 4. **Importar el Proyecto**

1. **Abrir Android Studio**

2. **Opciones para abrir el proyecto**:
   
   **Opción A - Desde la pantalla de bienvenida:**
   - Click en "Open"
   - Navega a: `C:\Users\Alex\Documents\DAM\DAM_EVA2_Diego_Herrera`
   - Selecciona la carpeta del proyecto (donde está el archivo `build.gradle.kts` principal)
   - Click en "OK"

   **Opción B - Si Android Studio ya está abierto:**
   - `File` → `Open`
   - Navega a la carpeta del proyecto
   - Click en "OK"

3. **Esperar sincronización de Gradle**:
   - Android Studio detectará automáticamente que es un proyecto Gradle
   - Verás un mensaje: "Gradle sync in progress..."
   - **Primera sincronización puede tardar 5-10 minutos** mientras descarga dependencias
   - Observa la barra de progreso en la parte inferior

---

## ⚠️ Solución de Problemas Comunes

### Problema 1: "Project JDK is not defined"

**Solución:**
1. `File` → `Project Structure` (o `Ctrl + Alt + Shift + S`)
2. En "SDK Location" → "JDK location"
3. Click en "..." y navega a tu instalación de JDK 17
4. Ejemplo: `C:\Program Files\Java\jdk-17`
5. Click en "Apply" → "OK"

### Problema 2: "Gradle sync failed: Could not find gradle-wrapper.jar"

**Solución:**
1. Abre PowerShell en la carpeta del proyecto
2. Ejecuta:
   ```powershell
   .\gradlew wrapper
   ```
3. Esto regenerará los archivos del wrapper de Gradle

### Problema 3: "SDK location not found"

**Solución:**
1. `File` → `Project Structure`
2. En "SDK Location" → "Android SDK location"
3. Establece la ruta (usualmente): `C:\Users\TuUsuario\AppData\Local\Android\Sdk`
4. O usa el botón "Edit" para que Android Studio lo configure automáticamente

### Problema 4: "Unsupported Kotlin plugin version"

**Solución:**
1. `File` → `Settings` → `Plugins`
2. Busca "Kotlin" y actualiza el plugin
3. Reinicia Android Studio
4. Vuelve a sincronizar Gradle

### Problema 5: Errores de compilación por versión de compileSdk 36

Si obtienes errores porque no tienes API 36:

**Solución rápida** - Edita el archivo `app/build.gradle.kts`:
```kotlin
android {
    compileSdk = 34  // Cambiar de 36 a 34
    
    defaultConfig {
        targetSdk = 34  // Cambiar de 36 a 34
        // ...
    }
}
```

---

## 🏃‍♂️ Ejecutar el Proyecto

### 5. **Configurar un Dispositivo Virtual (Emulador)**

1. **Abrir AVD Manager**:
   - Icono 📱 en la barra superior
   - O: `Tools` → `Device Manager`

2. **Crear nuevo dispositivo**:
   - Click en "Create Virtual Device"
   - Selecciona un dispositivo (recomendado: **Pixel 5** o **Pixel 6**)
   - Click en "Next"

3. **Seleccionar imagen del sistema**:
   - Pestaña "Recommended"
   - Selecciona **Android 14 (API 34)** o superior
   - Si no está descargada, click en "Download" junto a la versión
   - Click en "Next"

4. **Configuración final**:
   - Nombre: "Pixel_5_API_34" (o el que prefieras)
   - Orientación: Portrait
   - Click en "Finish"

### 6. **Ejecutar la Aplicación**

1. **Seleccionar el dispositivo**:
   - En la barra superior, selecciona tu emulador creado

2. **Ejecutar**:
   - Click en el botón ▶️ (Run) o presiona `Shift + F10`
   - El emulador se iniciará (primera vez puede tardar 2-3 minutos)
   - La app se instalará y abrirá automáticamente

---

## 📦 Estructura del Proyecto

Una vez configurado, verás esta estructura en Android Studio:

```
DAM_EVA2_Diego_Herrera/
├── app/                          # Módulo principal de la aplicación
│   ├── src/
│   │   └── main/
│   │       ├── java/cl/duoc/app/ # Código Kotlin
│   │       └── res/              # Recursos (layouts, strings, etc.)
│   └── build.gradle.kts          # Configuración del módulo
├── gradle/                       # Configuración de Gradle
├── build.gradle.kts              # Build script principal
└── settings.gradle.kts           # Configuración del proyecto
```

---

## ✅ Verificación Final

Antes de comenzar a desarrollar, verifica que:

- [ ] JDK 17 está instalado y configurado
- [ ] Android Studio abre sin errores
- [ ] SDK de Android está instalado (API 34 como mínimo)
- [ ] El proyecto sincroniza correctamente con Gradle
- [ ] Puedes crear y ejecutar un emulador
- [ ] La aplicación se ejecuta sin errores

---

## 🆘 Comandos Útiles de PowerShell

Si necesitas limpiar el proyecto o regenerar archivos:

```powershell
# Limpiar build
.\gradlew clean

# Sincronizar dependencias
.\gradlew --refresh-dependencies

# Verificar configuración
.\gradlew tasks

# Build debug APK
.\gradlew assembleDebug
```

---

## 📚 Recursos Adicionales

- **Documentación oficial de Android**: https://developer.android.com/docs
- **Jetpack Compose**: https://developer.android.com/jetpack/compose
- **Kotlin para Android**: https://kotlinlang.org/docs/android-overview.html

---

## 🎯 Próximos Pasos

Una vez que tengas todo funcionando:

1. Explora el código en `app/src/main/java/cl/duoc/app/`
2. Lee el archivo `README.md` para entender la estructura
3. Revisa `FUNCIONAMIENTO_APP.md` para detalles técnicos
4. Comienza a desarrollar nuevas funcionalidades

---

**¿Problemas?** Si encuentras algún error que no esté en esta guía, revisa:
- Los logs en la pestaña "Build" de Android Studio
- La consola de Gradle (parte inferior de Android Studio)
- O pregunta con el mensaje de error específico
