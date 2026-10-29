# Medición de cloro en línea

Aplicación académica para el proyecto de medición de cloro en línea.

## Flujo actual

1. Crea una cuenta con datos ficticios desde la pantalla de inicio de sesión.
2. Revisa el resumen del registro y pulsa **Ir al inicio**.
3. Usa la barra inferior o el menú lateral para navegar entre **Puntos**,
   **Alertas** y **Perfil**.
4. Inicia sesión con el correo y la contraseña usados al crear la cuenta.

La autenticación es simulada: la cuenta solo permanece disponible mientras la
aplicación está abierta y se pierde al cerrar o reiniciar la app. Las pantallas
de alertas usan mediciones ficticias del repositorio académico; el historial y
las integraciones reales se agregarán en próximas guías.

Los perfiles de supervisor y jefatura pueden administrar los puntos asignados
desde **Puntos**. Un punto nuevo guarda su medición inicial en el historial y
genera una alerta si el valor está fuera del rango normal. Para proteger la
trazabilidad, los puntos con mediciones históricas no se pueden eliminar. Esta
función usa Room sobre el archivo SQLite local existente.

## Ejecutar

Abre el proyecto en Android Studio o genera el APK de depuración desde PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug
```

El APK se genera en `app\build\outputs\apk\debug\app-debug.apk`.
