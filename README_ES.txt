MEDIADROP — PROYECTO ANDROID INICIAL
====================================

Qué incluye
- Interfaz HTML oscura inspirada en el patrón de uso de un convertidor de enlaces.
- App Android nativa mínima (Kotlin + WebView) que carga el HTML local.
- Flujo de compilación en GitHub Actions para producir un APK de prueba en la nube.

IMPORTANTE
Esta versión NO descarga ni convierte automáticamente videos de YouTube, Facebook o Instagram. Detecta enlaces y abre la plataforma oficial. El selector MP3/MP4 es una elección de interfaz, no un convertidor todavía. Para añadir descargas se requiere integrar una fuente o API autorizada y respetar los términos y derechos del contenido.

CÓMO CREAR EL APK SOLO CON TU TELÉFONO (compilación en la nube)
1. Descarga y descomprime MediaDrop_Android.zip usando el gestor de archivos del teléfono.
2. En el navegador, entra en https://github.com/ y crea una cuenta/inicia sesión.
3. Crea un repositorio nuevo llamado MediaDrop. Si ofrece añadir README o .gitignore automáticamente, déjalo vacío para evitar conflictos.
4. Sube el CONTENIDO de la carpeta MediaDrop_Android al repositorio, manteniendo la estructura de carpetas (incluida .github/workflows/build-apk.yml). En GitHub móvil puede ser más fácil activar “Sitio para ordenador” en el menú ⋮ del navegador.
5. Abre la pestaña Actions del repositorio. Si GitHub solicita habilitar Actions, acéptalo.
6. Selecciona “Build MediaDrop APK” y pulsa “Run workflow”. Si no aparece, confirma que el archivo está en .github/workflows/build-apk.yml en la rama main.
7. Espera a que la ejecución termine con marca verde. Abre esa ejecución, busca Artifacts y descarga “MediaDrop-debug-apk”.
8. Descomprime el ZIP descargado; dentro estará app-debug.apk. Tócalo para instalar. Android podría pedir permiso para instalar aplicaciones desde ese navegador/gestor de archivos; actívalo solo para esa app y luego puedes desactivarlo.

NOTAS TÉCNICAS
- El workflow usa Gradle 8.9, JDK 17 y Android Gradle Plugin 8.7.3.
- APK debug sirve para probar e instalar manualmente, no es una publicación firmada de lanzamiento.
- Si la compilación falla, abre el registro de Actions y copia el mensaje de error.
