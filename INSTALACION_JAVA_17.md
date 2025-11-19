# 🔧 Guía Completa: Instalación y Configuración de Java 17 en Windows

Esta guía te ayudará a instalar Java 17 y configurarlo correctamente como versión predeterminada.

---

## 📥 Opción 1: Instalación con Eclipse Temurin (Recomendado - Más Fácil)

Eclipse Temurin es OpenJDK oficial y se integra mejor con Windows.

### Paso 1: Descargar

1. Visita: **https://adoptium.net/temurin/releases/**
2. Selecciona:
   - **Version**: 17 (LTS)
   - **Operating System**: Windows
   - **Architecture**: x64
   - **Package Type**: JDK
   - **Image Type**: JDK (.msi)
3. Click en el botón de descarga (archivo .msi)

### Paso 2: Instalar

1. **Ejecuta** el archivo `.msi` descargado
2. En el instalador:
   - ✅ Marca: **"Set JAVA_HOME variable"**
   - ✅ Marca: **"Add to PATH"**
   - ✅ Marca: **"Associate .jar files"**
3. Click en **"Install"**
4. Espera a que termine la instalación
5. Click en **"Finish"**

### Paso 3: Verificar

Abre una **nueva ventana de PowerShell** (importante: debe ser nueva) y ejecuta:

```powershell
java -version
```

Deberías ver algo como:
```
openjdk version "17.0.x" 2024-xx-xx
OpenJDK Runtime Environment Temurin-17.0.x+x (build 17.0.x+x)
OpenJDK 64-Bit Server VM Temurin-17.0.x+x (build 17.0.x+x, mixed mode, sharing)
```

Y verifica JAVA_HOME:
```powershell
echo $env:JAVA_HOME
```

Debería mostrar algo como: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x.x-hotspot\`

**Si funciona, ¡ya está! Salta a "Configuración en Android Studio"**

---

## 📥 Opción 2: Instalación Manual con Oracle JDK

Si prefieres Oracle JDK o la Opción 1 no funcionó.

### Paso 1: Descargar

1. Visita: **https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html**
2. Busca: **"Windows x64 Installer"** (archivo .exe)
3. Acepta la licencia y descarga

### Paso 2: Instalar

1. Ejecuta el instalador `.exe`
2. Click en "Next"
3. **Anota la ruta de instalación** (ejemplo: `C:\Program Files\Java\jdk-17`)
4. Completa la instalación

### Paso 3: Configurar Variables de Entorno MANUALMENTE

Como el instalador de Oracle no lo hace automáticamente, debes configurarlo:

#### A. Configurar JAVA_HOME

1. Presiona `Win + R`
2. Escribe: `sysdm.cpl` y presiona Enter
3. Ve a la pestaña **"Opciones avanzadas"**
4. Click en **"Variables de entorno"**
5. En **"Variables del sistema"** (sección inferior):
   - Click en **"Nueva..."**
   - **Nombre de la variable**: `JAVA_HOME`
   - **Valor de la variable**: `C:\Program Files\Java\jdk-17` (ajusta según tu ruta)
   - Click en **"Aceptar"**

#### B. Agregar Java al PATH

1. En la misma ventana de **"Variables del sistema"**
2. Busca la variable **"Path"**
3. Selecciónala y click en **"Editar..."**
4. Click en **"Nuevo"**
5. Agrega: `%JAVA_HOME%\bin`
6. Click en **"Aceptar"** en todas las ventanas

### Paso 4: Verificar

**IMPORTANTE:** Cierra todas las ventanas de PowerShell/CMD y abre una nueva.

```powershell
java -version
```

Deberías ver:
```
java version "17.0.x" 2024-xx-xx LTS
Java(TM) SE Runtime Environment (build 17.0.x+x-LTS-xxx)
Java HotSpot(TM) 64-Bit Server VM (build 17.0.x+x-LTS-xxx, mixed mode, sharing)
```

---

## 🔍 Solución de Problemas

### Problema 1: "java no se reconoce como comando" después de instalar

**Causa:** PowerShell/CMD tiene las variables antiguas en caché.

**Solución:**
1. **Cierra TODAS las ventanas** de PowerShell, CMD y Android Studio
2. Abre una **nueva** ventana de PowerShell
3. Prueba de nuevo: `java -version`

Si aún no funciona:
```powershell
# Recargar variables de entorno en PowerShell actual
$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
$env:JAVA_HOME = [System.Environment]::GetEnvironmentVariable("JAVA_HOME","Machine")

# Verificar
java -version
```

### Problema 2: Tengo múltiples versiones de Java

Si ya tenías otra versión de Java instalada:

#### Ver todas las versiones instaladas:
```powershell
Get-ChildItem "C:\Program Files\Java"
Get-ChildItem "C:\Program Files\Eclipse Adoptium"
Get-ChildItem "C:\Program Files (x86)\Java"
```

#### Verificar qué Java está en PATH:
```powershell
where.exe java
```

Esto mostrará todas las rutas de Java en tu sistema. La primera es la que se usará.

#### Solución - Priorizar Java 17:

1. Abre Variables de Entorno (como se explicó antes)
2. Edita la variable **"Path"** del sistema
3. Busca las entradas de Java
4. **Mueve** la entrada de Java 17 (`C:\Program Files\Java\jdk-17\bin` o similar) **al principio** de la lista
5. Usa las flechas "Subir" o arrastra la entrada
6. Elimina rutas de versiones viejas de Java si no las necesitas
7. Click en "Aceptar"

### Problema 3: JAVA_HOME apunta a versión incorrecta

```powershell
# Ver valor actual de JAVA_HOME
echo $env:JAVA_HOME

# Si está mal, corregirlo para la sesión actual:
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"

# Para corregirlo permanentemente:
# Ir a Variables de Entorno (Win + R → sysdm.cpl)
# Y editar JAVA_HOME en Variables del sistema
```

### Problema 4: Android Studio no detecta Java 17

Incluso si Java funciona en PowerShell, Android Studio puede no detectarlo:

**Solución:**
1. Abre Android Studio
2. Ve a: `File` → `Project Structure` (o `Ctrl + Alt + Shift + S`)
3. En el panel izquierdo, selecciona **"SDK Location"**
4. Busca **"JDK location"**
5. Click en el botón **"..."** junto al campo
6. Navega manualmente a tu instalación de Java 17:
   - Temurin: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x.x-hotspot\`
   - Oracle: `C:\Program Files\Java\jdk-17\`
7. Click en **"OK"**
8. Click en **"Apply"** y luego **"OK"**

---

## ✅ Verificación Completa

Ejecuta estos comandos para asegurarte de que todo está correcto:

```powershell
# 1. Versión de Java
java -version

# 2. Compilador de Java
javac -version

# 3. Variable JAVA_HOME
echo $env:JAVA_HOME

# 4. Verificar que apunta a Java 17
Get-ChildItem $env:JAVA_HOME

# 5. Verificar ubicación del ejecutable
where.exe java
```

**Resultado esperado:**
- `java -version` muestra versión 17.x.x
- `javac -version` muestra versión 17.x.x
- `$env:JAVA_HOME` muestra la ruta a JDK 17
- `where.exe java` muestra el ejecutable de Java 17 primero

---

## 🎯 Configuración en Android Studio

Una vez que Java 17 funcione en PowerShell:

### 1. Configurar JDK en Android Studio

1. Abre Android Studio
2. Si tienes un proyecto abierto:
   - `File` → `Close Project` (cierra el proyecto)
3. En la pantalla de bienvenida:
   - Click en ⚙️ **"Customize"** → **"All settings"**
   - O: `Ctrl + Alt + S`
4. Navega a:
   - `Build, Execution, Deployment` → `Build Tools` → `Gradle`
5. En **"Gradle JDK"**:
   - Selecciona **"JAVA_HOME"** si está disponible
   - O selecciona la instalación de JDK 17 de la lista
   - Si no aparece, usa **"Add JDK"** y navega a la carpeta de instalación

### 2. Configurar JDK para el Proyecto

1. Abre tu proyecto (Plant Buddy)
2. `File` → `Project Structure` (o `Ctrl + Alt + Shift + S`)
3. En **"SDK Location"**:
   - **JDK location**: Asegúrate que apunte a Java 17
4. En **"Project"**:
   - **SDK**: Selecciona tu Android SDK
   - **Language level**: Mantén el valor por defecto
5. Click en **"Apply"** → **"OK"**

### 3. Sincronizar Gradle

1. Si ves un banner que dice "Gradle files have changed":
   - Click en **"Sync Now"**
2. O manualmente:
   - `File` → `Sync Project with Gradle Files`
3. Espera a que termine (puede tardar varios minutos la primera vez)

---

## 🚀 Siguiente Paso

Una vez que Java 17 esté configurado correctamente:

1. ✅ Verifica: `java -version` muestra 17.x.x
2. ✅ Android Studio está usando Java 17 en la configuración
3. ✅ Continúa con el resto de la `GUIA_INSTALACION.md`

---

## 📞 Comando Rápido de Diagnóstico

Copia y pega este bloque completo en PowerShell para hacer un diagnóstico:

```powershell
Write-Host "=== DIAGNÓSTICO JAVA ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Versión de Java:" -ForegroundColor Yellow
java -version 2>&1
Write-Host ""
Write-Host "2. Variable JAVA_HOME:" -ForegroundColor Yellow
echo $env:JAVA_HOME
Write-Host ""
Write-Host "3. Ubicación de java.exe:" -ForegroundColor Yellow
where.exe java 2>&1
Write-Host ""
Write-Host "4. Versión de javac:" -ForegroundColor Yellow
javac -version 2>&1
Write-Host ""
Write-Host "5. Instalaciones de Java detectadas:" -ForegroundColor Yellow
Get-ChildItem "C:\Program Files\Java" -ErrorAction SilentlyContinue | Select-Object Name
Get-ChildItem "C:\Program Files\Eclipse Adoptium" -ErrorAction SilentlyContinue | Select-Object Name
Write-Host ""
Write-Host "=== FIN DIAGNÓSTICO ===" -ForegroundColor Cyan
```

**Comparte el resultado de este comando si sigues teniendo problemas.**

---

## 📚 Recursos Adicionales

- **Eclipse Temurin**: https://adoptium.net/
- **Oracle JDK**: https://www.oracle.com/java/technologies/downloads/#java17
- **Documentación Android sobre JDK**: https://developer.android.com/studio/intro/studio-config#jdk

---

**¿Funcionó?** Una vez que `java -version` muestre 17.x.x, estarás listo para continuar con la instalación de Android Studio y el proyecto.
