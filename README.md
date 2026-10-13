# Aplicación de Detección de Somnolencia (Drowsiness Detection App)

Aplicación móvil para Android desarrollada en Kotlin y Jetpack Compose que detecta signos de somnolencia y fatiga mediante visión por computadora en tiempo real (MediaPipe Face Mesh y cálculo de Eye Aspect Ratio - EAR).

---

## 🚀 Características

- **Detección facial en tiempo real:** Uso de MediaPipe Face Landmarker para monitorear puntos clave del rostro y párpados.
- **Cálculo de EAR (Eye Aspect Ratio):** Monitoreo continuo de la apertura ocular y frecuencia de parpadeo.
- **Alertas multimodales:** Alertas auditivas y retroalimentación háptica (vibración) ante eventos de somnolencia detectados.
- **Inspección de cámara:** Diagnóstico y visualización de capacidades del sensor de cámara del dispositivo.
- **Interfaz moderna:** Diseñada con Jetpack Compose y Material 3.

---

## 🛠️ Tecnologías y Requisitos

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Visión por computadora / IA:** Google MediaPipe Tasks Vision
- **Cámara:** AndroidX CameraX
- **Arquitectura:** MVVM (Model-View-ViewModel) con Kotlin Coroutines y StateFlow
- **Requisitos mínimos:**
  - Android Studio Ladybug / Koala o superior
  - JDK 17 o superior
  - Android SDK (minSdk 24, targetSdk 35)
  - Dispositivo físico o emulador con cámara habilitada

---

## 📦 Configuración del Proyecto

1. **Clonar el repositorio:**
   ```bash
   git clone <URL_DEL_REPOSITORIO>
   cd aplicaciondesomnolencia
   ```

2. **Abrir en Android Studio:**
   - Selecciona **File > Open...** y elige la carpeta raíz del proyecto.
   - Espera a que Gradle sincronice las dependencias.

3. **Compilar y ejecutar:**
   - Conecta tu dispositivo Android con depuración USB habilitada (o inicia un emulador con soporte de cámara).
   - Presiona **Run 'app'** (`Shift + F10`).
