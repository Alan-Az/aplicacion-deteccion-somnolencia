# Auditoría de Seguridad y Privacidad de Datos

Este documento certifica el análisis estático y de configuración realizado en el repositorio antes de su publicación en GitHub.

---

## 1. Resumen Ejecutivo
- **Estado:** ✅ **Aprobado para publicación pública / privada.**
- **Exposición de Secretos / Credenciales:** **0 encontradas.**
- **Datos Personales / Rutas Locales:** **0 expuestas** en el historial de Git.
- **Modo de Operación de la IA:** **100% On-Device / Edge AI (Offline).**

---

## 2. Inspección de Archivos Sensibles y Equivalentes a `.env`

| Tipo de Recurso | Archivo Estándar | Estado en este Proyecto | ¿Expuesto en Git? |
| :--- | :--- | :--- | :--- |
| **Variables de entorno locales** | `.env` / `local.properties` | `local.properties` contiene la ruta del SDK del desarrollador (`C:\Users\...`) | ❌ **No.** Excluido mediante `.gitignore`. |
| **Cachés de Android Studio** | `.idea/workspace.xml`, `.idea/caches` | Contienen nombres de usuario e historial de ventanas locales | ❌ **No.** Excluido mediante `.gitignore`. |
| **Claves de Firma (Keystores)** | `*.jks`, `*.keystore` | Claves para subir a Google Play Store | ❌ **No existen ni están en el repo.** |
| **Claves de API Externas** | Claves de OpenAI, Firebase, AWS, Google Cloud | No se requieren | ❌ **No se utilizan.** |
| **Tokens / Secretos** | Tokens OAuth, Bearer tokens | No se requieren | ❌ **No se utilizan.** |

---

## 3. Arquitectura y Privacidad del Usuario Final

1. **Sin Recolección ni Envío de Video / Biometría:**
   - La cámara procesa los fotogramas en memoria RAM a través de CameraX y MediaPipe (`app/src/main/assets/face_landmarker.task`).
   - Ningún fotograma, coordenada biométrica ni audio se guarda en disco persistente ni se transmite a servidores por internet.
2. **Sin Dependencia de Backend de Terceros:**
   - La aplicación funciona en modo avión / sin conexión a internet.
3. **Portabilidad para Otros Desarrolladores:**
   - Cualquier persona que clone el repositorio podrá abrirlo en su propia instalación de Android Studio; Gradle y el IDE generarán automáticamente su propio `local.properties` compatible con su sistema operativo (Windows, macOS o Linux) sin ninguna colisión.
