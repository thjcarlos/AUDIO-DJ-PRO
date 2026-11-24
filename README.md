# AUDIO DJ PRO — Proyecto Android Studio

Aplicación Android horizontal con dos reproductores:
- Deck A: audio base en repetición continua.
- Deck B: pista seleccionada, reproducida una sola vez.
- Al terminar Deck B, Deck A vuelve automáticamente.
- Biblioteca de pistas importadas desde el almacenamiento.
- Volúmenes independientes.
- Servicio de reproducción en primer plano para continuar en segundo plano.

## Abrir
1. Descomprime `AUDIO_DJ_PRO.zip`.
2. Abre la carpeta `AUDIO_DJ_PRO` desde Android Studio (Open).
3. Permite la sincronización de Gradle y descarga las dependencias.
4. Conecta un dispositivo Android o crea un emulador.
5. Ejecuta `app`.

## Generar APK
En Android Studio: Build > Build Bundle(s) / APK(s) > Build APK(s).
El APK de depuración quedará en `app/build/outputs/apk/debug/app-debug.apk`.

## Notas
- Requiere Android 8.0 (API 26) o superior.
- La primera compilación necesita internet para descargar Gradle y dependencias.
- El sistema de Android puede requerir permiso para notificaciones.
- La app solicita acceso a los audios mediante el selector de documentos; no requiere permiso general de almacenamiento.
- Este paquete contiene el código fuente completo del proyecto. El APK debe compilarse en Android Studio.
- Es una primera versión funcional base; todavía no incluye jog wheels animados, visualización de formas de onda, ecualizador ni crossfader de mezcla.
