# Medición de cloro en línea

Aplicación académica para el proyecto de medición de cloro en línea.

## Flujo actual

1. Si aún no tienes una cuenta, pulsa **Crear cuenta** desde la pantalla de
   inicio de sesión y completa el registro.
2. Revisa el resumen del registro y pulsa **Ir al inicio**.
3. Usa la barra inferior o el menú lateral para navegar entre **Puntos**,
   **Alertas** y **Perfil**.
4. Para volver a entrar, inicia sesión con el correo y la contraseña de esa
   cuenta. Las cuentas se guardan en la base SQLite del dispositivo y persisten
   al cerrar o reiniciar la aplicación.

La autenticación es local y académica; no existe un servicio de cuentas en
línea. Las pantallas de alertas usan mediciones ficticias del repositorio
académico; el historial y las integraciones reales se agregarán en próximas
guías.

Los perfiles de supervisor y jefatura pueden administrar los puntos asignados
desde **Puntos**. Un punto nuevo guarda su medición inicial en el historial y
genera una alerta si el valor está fuera del rango normal. Para proteger la
trazabilidad, los puntos con mediciones históricas no se pueden eliminar. Esta
función usa Room sobre el archivo SQLite local existente.

## Ejecutar

Abre en Android Studio la carpeta raíz que contiene `settings.gradle.kts` y
sincroniza el proyecto con Gradle. Android Studio debe tener instalado el SDK
indicado por `compileSdk`; la ruta local se configura en `local.properties`,
archivo ignorado por Git. También puedes generar el APK de depuración desde
PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug
```

El APK se genera en `app\build\outputs\apk\debug\app-debug.apk`.
