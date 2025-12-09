# Configuración de Firma del APK - PlantBuddy

## Información del Keystore

**Archivo**: `plantbuddy-release.jks`

### Detalles Técnicos

- **Algoritmo**: RSA
- **Tamaño de clave**: 2048 bits
- **Validez**: 10,000 días (~27 años)
- **Formato**: JKS (Java KeyStore)

### Credenciales

- **Store Password**: `plantbuddy2025`
- **Key Alias**: `plantbuddy-key`
- **Key Password**: `plantbuddy2025`

### Información del Certificado

- **CN (Common Name)**: Diego Herrera
- **OU (Organizational Unit)**: PlantBuddy
- **O (Organization)**: DUOC UC
- **L (Locality)**: Santiago
- **ST (State)**: RM
- **C (Country)**: CL

## Uso

El keystore está configurado en `app/build.gradle.kts` para firmar automáticamente las builds de release:

```bash
# Generar APK firmado
./gradlew assembleRelease

# Generar Android App Bundle firmado
./gradlew bundleRelease
```

Los archivos firmados se generarán en:
- APK: `app/build/outputs/apk/release/app-release.apk`
- AAB: `app/build/outputs/bundle/release/app-release.aab`

## Seguridad

⚠️ **IMPORTANTE**: Este keystore contiene credenciales sensibles. 
- NO compartir el archivo `.jks`
- NO subir a repositorios públicos
- Mantener las contraseñas seguras
- Usar variables de entorno en CI/CD

## Verificación

Para verificar la firma del APK:

```bash
jarsigner -verify -verbose -certs app-release.apk
```

Para ver información del keystore:

```bash
keytool -list -v -keystore plantbuddy-release.jks -storepass plantbuddy2025
```
