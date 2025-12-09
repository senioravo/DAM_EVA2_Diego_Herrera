# APK Firmado - PlantBuddy App

## Información General

**Aplicación**: PlantBuddy - Sistema de Gestión de Plantas  
**Versión**: 1.0 (versionCode: 1)  
**Package**: cl.duoc.app  
**Tamaño APK**: 21.1 MB (21,067,998 bytes)  
**Fecha de generación**: 9 de diciembre de 2025

## Ubicación del APK

```
app/build/outputs/apk/release/app-release.apk
```

## Configuración de Firma

### Keystore
- **Archivo**: `keystore/plantbuddy-release.jks`
- **Tipo**: JKS (Java KeyStore)
- **Algoritmo**: RSA 2048 bits
- **Validez**: 10,000 días (~27 años)

### Credenciales
- **Store Password**: `plantbuddy2025`
- **Key Alias**: `plantbuddy-key`
- **Key Password**: `plantbuddy2025`

### Certificado Digital

**Distinguished Name (DN)**:
```
CN=Diego Herrera
OU=PlantBuddy
O=DUOC UC
L=Santiago
ST=RM
C=CL
```

**Huellas Digitales**:
- **SHA-256**: `5edf7d21905d47c4babcccd41e26744d837c20cbad43e4c6051c6af110630eec`
- **SHA-1**: `f5c199faa0b59be28c6613efb4b4024b4f6f58c1`
- **MD5**: `6a9cd64b335da0418490e9307ee8dca7`

## Configuración Técnica

### build.gradle.kts

```kotlin
android {
    namespace = "cl.duoc.app"
    compileSdk = 36
    
    defaultConfig {
        applicationId = "cl.duoc.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    
    signingConfigs {
        create("release") {
            storeFile = file("../keystore/plantbuddy-release.jks")
            storePassword = "plantbuddy2025"
            keyAlias = "plantbuddy-key"
            keyPassword = "plantbuddy2025"
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}
```

## Requisitos del Sistema

### Mínimos
- **Android**: 7.0 Nougat (API 24)
- **RAM**: 2 GB
- **Almacenamiento**: 100 MB libres

### Recomendados
- **Android**: 14.0 o superior (API 34+)
- **RAM**: 4 GB
- **Almacenamiento**: 200 MB libres
- **Conexión**: Internet estable para sincronización con backend

## Características de la Build

### Incluido en el APK
- ✅ Todas las dependencias necesarias
- ✅ Recursos optimizados
- ✅ Soporte multi-dex
- ✅ Bibliotecas nativas (ARM, x86)
- ✅ Firma digital válida

### Configuraciones
- ✅ ProGuard deshabilitado (para debugging)
- ✅ Lint checks deshabilitados (optimización de build)
- ✅ Frame rate: 120Hz configurado
- ✅ Soporte para arquitecturas: armeabi-v7a, arm64-v8a, x86, x86_64

## Instalación

### Método 1: Android Studio
```bash
adb install app/build/outputs/apk/release/app-release.apk
```

### Método 2: Manual
1. Transferir el APK al dispositivo Android
2. Habilitar "Instalar aplicaciones de origen desconocido"
3. Abrir el APK y seguir las instrucciones

### Método 3: Gradle
```bash
./gradlew installRelease
```

## Verificación de Firma

### Con apksigner
```bash
apksigner verify --print-certs app-release.apk
```

### Con jarsigner
```bash
jarsigner -verify -verbose -certs app-release.apk
```

### Con keytool (ver certificado del keystore)
```bash
keytool -list -v -keystore plantbuddy-release.jks -storepass plantbuddy2025
```

## Proceso de Build

```bash
# Limpiar build anterior
./gradlew clean

# Generar APK firmado
./gradlew assembleRelease

# Resultado en:
# app/build/outputs/apk/release/app-release.apk
```

## Archivos Generados

```
app/build/outputs/apk/release/
├── app-release.apk          # APK firmado (21.1 MB)
├── output-metadata.json     # Metadatos de la build
└── baselineProfiles/        # Perfiles de optimización
```

## Integración con Backend

### Configuración
- **URL Base**: `http://10.0.2.2:8080/api/` (emulador)
- **URL Producción**: Configurar en `RetrofitClient.kt`
- **Timeout**: 30 segundos
- **Autenticación**: JWT Token

### Endpoints Utilizados
- `POST /auth/login` - Autenticación
- `POST /auth/register` - Registro
- `GET /productos` - Lista de productos
- `POST /compras/crear` - Crear compra
- `GET /plantel/usuario/{id}` - Plantas del usuario

## Seguridad

### ⚠️ Importante
- El keystore contiene credenciales privadas
- NO compartir el archivo `.jks` públicamente
- NO subir a repositorios públicos
- En producción, usar variables de entorno

### Recomendaciones
1. Guardar backup del keystore en lugar seguro
2. Documentar las contraseñas de forma privada
3. Para publicación en Play Store, considerar Google Play App Signing
4. Cambiar contraseñas en ambiente de producción

## Testing

### Verificar Instalación
```bash
# Listar aplicaciones instaladas
adb shell pm list packages | grep cl.duoc.app

# Ver información de la app
adb shell dumpsys package cl.duoc.app
```

### Logs de Ejecución
```bash
# Ver logs en tiempo real
adb logcat -s "CartRepository:D" "PlantBuddy:D"
```

## Troubleshooting

### Error: "App not installed"
- Desinstalar versión anterior: `adb uninstall cl.duoc.app`
- Verificar espacio disponible en dispositivo

### Error: "Parse error"
- Verificar que el dispositivo cumple con minSdk 24
- Descargar APK nuevamente (puede estar corrupto)

### Backend no conecta
- Verificar que el backend está corriendo en puerto 8080
- Para dispositivo físico, usar IP de la computadora en red local
- Configurar URL en `RetrofitClient.kt`

## Información del Desarrollador

**Autor**: Diego Herrera  
**Organización**: DUOC UC  
**Proyecto**: PlantBuddy - Sistema de Gestión de Plantas  
**Contexto**: Proyecto académico DAM - Desarrollo de Aplicaciones Móviles

## Notas de la Versión 1.0

### Funcionalidades Implementadas
- ✅ Sistema de autenticación completo
- ✅ Catálogo de productos con detalles
- ✅ Carrito de compras funcional
- ✅ Flujo de checkout integrado con backend
- ✅ Gestión de plantel personal
- ✅ Historial de compras
- ✅ Perfil de usuario

### Integración Backend
- ✅ API REST con Spring Boot
- ✅ Base de datos PostgreSQL (Neon)
- ✅ Persistencia de compras y detalles
- ✅ Actualización automática de inventario
- ✅ Logs de debugging habilitados

---

**Generado**: 9 de diciembre de 2025  
**Herramienta**: Gradle 8.13 + Android Build Tools  
**JDK**: OpenJDK 17
