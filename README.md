# Medición de cloro en línea

Aplicación académica para el proyecto de medición de cloro en línea.

## Flujo actual

1. Crea una cuenta con datos ficticios desde la pantalla de inicio de sesión.
2. Revisa el resumen del registro y pulsa **Ir al inicio**.
3. Desde el menú puedes consultar los puntos de medición y cerrar sesión.
4. Inicia sesión con el correo y la contraseña usados al crear la cuenta.

La autenticación es simulada: la cuenta solo permanece disponible mientras la
aplicación está abierta y se pierde al cerrar o reiniciar la app. Las pantallas
de alertas e historial se agregarán en próximas guías.

## Ejecutar

Abre el proyecto en Android Studio o genera el APK de depuración desde PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug
```

El APK se genera en `app\build\outputs\apk\debug\app-debug.apk`.
