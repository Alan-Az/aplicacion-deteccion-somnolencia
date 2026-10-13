package com.example.aplicaciondesomnolencia.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CameraEnhance
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aplicaciondesomnolencia.R
import com.example.aplicaciondesomnolencia.biometrics.DriverState
import com.example.aplicaciondesomnolencia.camera.CameraCapabilityInspector
import com.example.aplicaciondesomnolencia.camera.CameraPreviewView
import com.example.aplicaciondesomnolencia.mediapipe.FaceLandmarkerHelper
import com.example.aplicaciondesomnolencia.ui.theme.AplicacionDeSomnolenciaTheme
import com.example.aplicaciondesomnolencia.util.ModelDownloader
import com.example.aplicaciondesomnolencia.viewmodel.MainViewModel
import java.util.Locale

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    val driverState by viewModel.driverState.collectAsStateWithLifecycle()
    val earScore by viewModel.earScore.collectAsStateWithLifecycle()
    val rawEar by viewModel.rawEar.collectAsStateWithLifecycle()
    val fps by viewModel.fps.collectAsStateWithLifecycle()
    val consecutiveFatigueFrames by viewModel.consecutiveFatigueFrames.collectAsStateWithLifecycle()
    val limiteEar by viewModel.limiteEar.collectAsStateWithLifecycle()
    val limiteFrames by viewModel.limiteFrames.collectAsStateWithLifecycle()
    val riskProgress by viewModel.riskProgress.collectAsStateWithLifecycle()
    val isAudioEnabled by viewModel.isAudioAlertEnabled.collectAsStateWithLifecycle()
    val isHapticEnabled by viewModel.isHapticAlertEnabled.collectAsStateWithLifecycle()
    val isFrontCamera by viewModel.isFrontCamera.collectAsStateWithLifecycle()
    val isTorchEnabled by viewModel.isTorchEnabled.collectAsStateWithLifecycle()
    val cameraSpecs by viewModel.cameraSpecs.collectAsStateWithLifecycle()
    val customAudioUri by viewModel.customAudioUri.collectAsStateWithLifecycle()

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setCustomAudioUri(it) }
    }

    var inferenceTimeMs by remember { mutableLongStateOf(0L) }
    var showSettings by remember { mutableStateOf(false) }
    var showCameraSpecsDialog by remember { mutableStateOf(false) }

    val faceLandmarkerHelper = remember {
        FaceLandmarkerHelper(
            context = context,
            faceLandmarkerHelperListener = object : FaceLandmarkerHelper.LandmarkerListener {
                override fun onError(error: String, errorCode: Int) {
                    Log.e("MainScreen", "FaceLandmarker error: $error")
                }

                override fun onResults(resultBundle: FaceLandmarkerHelper.ResultBundle) {
                    inferenceTimeMs = resultBundle.inferenceTime
                    val results = resultBundle.results
                    val faceLandmarks = if (results.isNotEmpty() && results[0].faceLandmarks().isNotEmpty()) {
                        results[0].faceLandmarks()[0]
                    } else {
                        null
                    }
                    viewModel.processFaceLandmarks(faceLandmarks)
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
        try {
            ModelDownloader.ensureModelExists(context)
            if (!faceLandmarkerHelper.isPrepared()) {
                faceLandmarkerHelper.setupFaceLandmarker()
            }
        } catch (e: Exception) {
            Log.e("MainScreen", "Error preparing model: ${e.message}")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            faceLandmarkerHelper.clearFaceLandmarker()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (hasCameraPermission) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isLandscape = maxWidth > maxHeight

                // Full Screen Camera Feed
                CameraPreviewView(
                    faceLandmarkerHelper = faceLandmarkerHelper,
                    modifier = Modifier.fillMaxSize(),
                    isFrontCamera = isFrontCamera,
                    isTorchEnabled = isTorchEnabled,
                    onCameraSpecsInspected = { specs ->
                        viewModel.updateCameraSpecs(specs)
                    }
                )

                // Full Screen Fatigue Warning Overlay (pulsing red border & screen wash)
                if (driverState == DriverState.FATIGUE_DETECTED) {
                    FatigueAlertOverlay()
                }

                // Adaptive HUD Overlay
                if (isLandscape) {
                    // Landscape / Wide Layout
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left Column: Brand Header, Status Badge & Telemetry
                        Column(
                            modifier = Modifier
                                .width(340.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            HudBrandHeader()

                            StatusBannerCard(
                                driverState = driverState,
                                riskProgress = riskProgress,
                                consecutiveFatigueFrames = consecutiveFatigueFrames,
                                limiteFrames = limiteFrames,
                                onOpenSpecsDialog = { showCameraSpecsDialog = true }
                            )

                            DashboardCard(
                                driverState = driverState,
                                earScore = earScore,
                                rawEar = rawEar,
                                fps = fps,
                                inferenceTimeMs = inferenceTimeMs,
                                limiteEar = limiteEar,
                                isFrontCamera = isFrontCamera,
                                isTorchEnabled = isTorchEnabled,
                                hasFlashUnit = cameraSpecs?.hasFlashUnit ?: false,
                                onToggleCamera = { viewModel.toggleCameraLens() },
                                onToggleTorch = { viewModel.toggleTorch() },
                                onOpenSpecsDialog = { showCameraSpecsDialog = true },
                                showSettings = showSettings,
                                onToggleSettings = { showSettings = !showSettings }
                            )
                        }

                        // Right Column: Settings Panel if expanded
                        if (showSettings) {
                            Column(
                                modifier = Modifier
                                    .width(360.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                SettingsPanelCard(
                                    limiteEar = limiteEar,
                                    limiteFrames = limiteFrames,
                                    isAudioEnabled = isAudioEnabled,
                                    isHapticEnabled = isHapticEnabled,
                                    customAudioUri = customAudioUri,
                                    onLimiteEarChanged = { viewModel.setLimiteEar(it) },
                                    onLimiteFramesChanged = { viewModel.setLimiteFrames(it.toInt()) },
                                    onAudioToggle = { viewModel.setAudioAlertEnabled(it) },
                                    onHapticToggle = { viewModel.setHapticAlertEnabled(it) },
                                    onSelectAudio = { audioPickerLauncher.launch("audio/*") },
                                    onClearAudio = { viewModel.setCustomAudioUri(null) },
                                    onResetDefaults = { viewModel.resetDefaults() },
                                    onClose = { showSettings = false }
                                )
                            }
                        }
                    }
                } else {
                    // Portrait Layout
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            HudBrandHeader()

                            StatusBannerCard(
                                driverState = driverState,
                                riskProgress = riskProgress,
                                consecutiveFatigueFrames = consecutiveFatigueFrames,
                                limiteFrames = limiteFrames,
                                onOpenSpecsDialog = { showCameraSpecsDialog = true }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DashboardCard(
                                driverState = driverState,
                                earScore = earScore,
                                rawEar = rawEar,
                                fps = fps,
                                inferenceTimeMs = inferenceTimeMs,
                                limiteEar = limiteEar,
                                isFrontCamera = isFrontCamera,
                                isTorchEnabled = isTorchEnabled,
                                hasFlashUnit = cameraSpecs?.hasFlashUnit ?: false,
                                onToggleCamera = { viewModel.toggleCameraLens() },
                                onToggleTorch = { viewModel.toggleTorch() },
                                onOpenSpecsDialog = { showCameraSpecsDialog = true },
                                showSettings = showSettings,
                                onToggleSettings = { showSettings = !showSettings }
                            )

                            AnimatedVisibility(
                                visible = showSettings,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                SettingsPanelCard(
                                    limiteEar = limiteEar,
                                    limiteFrames = limiteFrames,
                                    isAudioEnabled = isAudioEnabled,
                                    isHapticEnabled = isHapticEnabled,
                                    customAudioUri = customAudioUri,
                                    onLimiteEarChanged = { viewModel.setLimiteEar(it) },
                                    onLimiteFramesChanged = { viewModel.setLimiteFrames(it.toInt()) },
                                    onAudioToggle = { viewModel.setAudioAlertEnabled(it) },
                                    onHapticToggle = { viewModel.setHapticAlertEnabled(it) },
                                    onSelectAudio = { audioPickerLauncher.launch("audio/*") },
                                    onClearAudio = { viewModel.setCustomAudioUri(null) },
                                    onResetDefaults = { viewModel.resetDefaults() },
                                    onClose = { showSettings = false }
                                )
                            }
                        }
                    }
                }

                // Camera Specs & Night Diagnostic Dialog
                if (showCameraSpecsDialog) {
                    CameraSpecsDialog(
                        cameraSpecs = cameraSpecs ?: CameraCapabilityInspector.createFallbackSpecs(isFrontCamera = isFrontCamera),
                        isTorchEnabled = isTorchEnabled,
                        onToggleTorch = { viewModel.toggleTorch() },
                        onDismiss = { showCameraSpecsDialog = false }
                    )
                }
            }
        } else {
            // Permission request screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Permiso de Cámara Requerido",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Se requiere acceso a la cámara frontal para monitorear los ojos y detectar signos de somnolencia en tiempo real.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Otorgar Permiso de Cámara")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full-screen pulsing red overlay and border for immediate fatigue alert notification.
 */
@Composable
private fun FatigueAlertOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "fatigueAlertPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val borderPulseWidth by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderPulseWidth"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red.copy(alpha = pulseAlpha))
            .border(
                width = borderPulseWidth.dp,
                color = Color.Red,
                shape = RectangleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xEEB71C1C)),
            border = BorderStroke(2.dp, Color.White),
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color.Yellow,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "⚠️ ALERTA DE SOMNOLENCIA DETECTADA!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "¡MANTENGA LA ATENCIÓN EN EL CAMINO!\nSe detectó parpadeo prolongado u ojos cerrados.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.95f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Top HUD Brand Header displaying the official app logo and title.
 */
@Composable
private fun HudBrandHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xDC10141D)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF2979FF).copy(alpha = 0.25f),
                border = BorderStroke(1.dp, Color(0xFF82B1FF).copy(alpha = 0.5f)),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "Logo SomnolenIA Mobile",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }
            }

            Column {
                Text(
                    text = "SomnolenIA Mobile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Monitoreo Inteligente en Tiempo Real",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray.copy(alpha = 0.85f)
                )
            }
        }
    }
}

/**
 * Top Status Banner HUD displaying current Driver State and Risk Bar.
 */
@Composable
private fun StatusBannerCard(
    driverState: DriverState,
    riskProgress: Float,
    consecutiveFatigueFrames: Int,
    limiteFrames: Int,
    onOpenSpecsDialog: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when (driverState) {
            DriverState.FATIGUE_DETECTED -> Color(0xFFD32F2F)
            DriverState.ATTENTIVE -> Color(0xFF1B5E20)
            DriverState.NO_FACE_DETECTED -> Color(0xFFE65100)
        },
        animationSpec = tween(durationMillis = 300),
        label = "statusBgColor"
    )

    // Dynamic Risk Bar Color based on Risk level
    val riskBarColor = when {
        riskProgress >= 0.85f -> Color(0xFFF44336) // Red
        riskProgress >= 0.50f -> Color(0xFFFFC107) // Yellow
        else -> Color(0xFF4CAF50) // Green
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (driverState) {
                            DriverState.FATIGUE_DETECTED -> Icons.Default.Warning
                            DriverState.ATTENTIVE -> Icons.Default.CheckCircle
                            DriverState.NO_FACE_DETECTED -> Icons.Default.Face
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .padding(end = 8.dp)
                    )

                    Text(
                        text = when (driverState) {
                            DriverState.FATIGUE_DETECTED -> "¡SOMNOLENCIA DETECTADA!"
                            DriverState.ATTENTIVE -> "CONDUCTOR ATENTO"
                            DriverState.NO_FACE_DETECTED -> "BUSCANDO ROSTRO..."
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }

                IconButton(
                    onClick = onOpenSpecsDialog,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Capacidades de Cámara",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Risk Meter Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Riesgo: ${(riskProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                LinearProgressIndicator(
                    progress = { riskProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = riskBarColor,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$consecutiveFatigueFrames / $limiteFrames f",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * High contrast Telemetry Dashboard HUD showing EAR score, Raw EAR, FPS, Inference time, Camera Toggle and Settings.
 */
@Composable
private fun DashboardCard(
    driverState: DriverState,
    earScore: Float,
    rawEar: Float,
    fps: Int,
    inferenceTimeMs: Long,
    limiteEar: Float,
    isFrontCamera: Boolean,
    isTorchEnabled: Boolean,
    hasFlashUnit: Boolean,
    onToggleCamera: () -> Unit,
    onToggleTorch: () -> Unit,
    onOpenSpecsDialog: () -> Unit,
    showSettings: Boolean,
    onToggleSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xDC10141D) // High contrast dark translucent HUD background
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Telemetría Biométrica",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "EAR Suavizado (EMA)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val earColor = if ((earScore < limiteEar) && (driverState != DriverState.NO_FACE_DETECTED)) {
                        Color(0xFFFF5252)
                    } else {
                        Color(0xFF69F0AE)
                    }

                    Text(
                        text = String.format(Locale.US, "%.3f", earScore),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = earColor
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Camera Switch Button
                    IconButton(
                        onClick = onToggleCamera,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = if (isFrontCamera) "Cámara Frontal" else "Cámara Trasera",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Camera Specs Info Button
                    IconButton(
                        onClick = onOpenSpecsDialog,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraEnhance,
                            contentDescription = "Capacidades de Cámara",
                            tint = Color(0xFF82B1FF)
                        )
                    }

                    if (hasFlashUnit) {
                        Spacer(modifier = Modifier.width(6.dp))

                        // Torch Quick Toggle Button
                        IconButton(
                            onClick = onToggleTorch,
                            modifier = Modifier
                                .background(
                                    if (isTorchEnabled) Color(0xFF689F38) else Color.White.copy(alpha = 0.1f),
                                    CircleShape
                                )
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Modo Noche / Linterna",
                                tint = if (isTorchEnabled) Color.White else Color.LightGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onToggleSettings,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (showSettings) Icons.Default.ExpandLess else Icons.Default.Settings,
                            contentDescription = "Ajustes",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics row: Raw EAR, FPS, Inference, Threshold
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricChip(label = "RAW EAR", value = String.format(Locale.US, "%.3f", rawEar))
                MetricChip(label = "FPS", value = fps.toString())
                MetricChip(label = "INFERENCIA", value = "$inferenceTimeMs ms")
                MetricChip(label = "UMBRAL EAR", value = String.format(Locale.US, "%.2f", limiteEar))
            }
        }
    }
}

/**
 * Sensitivity & Settings Control Panel.
 */
@Composable
private fun SettingsPanelCard(
    limiteEar: Float,
    limiteFrames: Int,
    isAudioEnabled: Boolean,
    isHapticEnabled: Boolean,
    customAudioUri: Uri?,
    onLimiteEarChanged: (Float) -> Unit,
    onLimiteFramesChanged: (Float) -> Unit,
    onAudioToggle: (Boolean) -> Unit,
    onHapticToggle: (Boolean) -> Unit,
    onSelectAudio: () -> Unit,
    onClearAudio: () -> Unit,
    onResetDefaults: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xF2181C26)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color(0xFF82B1FF),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Ajustes de Sensibilidad",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.ExpandLess,
                        contentDescription = "Cerrar",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Slider EAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Límite EAR (Sensibilidad):",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
                Text(
                    text = String.format(Locale.US, "%.2f", limiteEar),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF82B1FF)
                )
            }
            Slider(
                value = limiteEar,
                onValueChange = onLimiteEarChanged,
                valueRange = 0.08f..0.30f,
                steps = 21,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF82B1FF),
                    activeTrackColor = Color(0xFF82B1FF),
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Slider Frames
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Límite Frames de Somnolencia:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
                Text(
                    text = "$limiteFrames frames",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF82B1FF)
                )
            }
            Slider(
                value = limiteFrames.toFloat(),
                onValueChange = onLimiteFramesChanged,
                valueRange = 5f..45f,
                steps = 39,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF82B1FF),
                    activeTrackColor = Color(0xFF82B1FF),
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Audio Alert Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isAudioEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = if (isAudioEnabled) Color(0xFF82B1FF) else Color.Gray,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Alarma Sonora",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
                Switch(
                    checked = isAudioEnabled,
                    onCheckedChange = onAudioToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF2979FF)
                    )
                )
            }

            // Custom Audio Sound Picker Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AudioFile,
                        contentDescription = null,
                        tint = Color(0xFF82B1FF),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column {
                        Text(
                            text = "Tono de Alarma",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Text(
                            text = if (customAudioUri != null) "Audio Personalizado" else "Sirena de Alarma (Raw)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (customAudioUri != null) Color(0xFF69F0AE) else Color.LightGray
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (customAudioUri != null) {
                        OutlinedButton(
                            onClick = onClearAudio,
                            modifier = Modifier.padding(end = 6.dp),
                            border = BorderStroke(1.dp, Color.LightGray),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Predet.", fontSize = 11.sp)
                        }
                    }
                    Button(
                        onClick = onSelectAudio,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2979FF))
                    ) {
                        Text("Seleccionar", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Haptic Alert Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = null,
                        tint = if (isHapticEnabled) Color(0xFF82B1FF) else Color.Gray,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Alerta de Vibración",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
                Switch(
                    checked = isHapticEnabled,
                    onCheckedChange = onHapticToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF2979FF)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reset Defaults Button
            OutlinedButton(
                onClick = onResetDefaults,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Color(0xFF82B1FF)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF82B1FF))
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Restablecer Valores Predeterminados")
            }
        }
    }
}

/**
 * High contrast metric display chip.
 */
@Composable
private fun MetricChip(label: String, value: String) {
    Box(
        modifier = Modifier
            .background(
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(10.dp)
            )
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    AplicacionDeSomnolenciaTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                StatusBannerCard(
                    driverState = DriverState.ATTENTIVE,
                    riskProgress = 0.25f,
                    consecutiveFatigueFrames = 4,
                    limiteFrames = 15,
                    onOpenSpecsDialog = {}
                )

                DashboardCard(
                    driverState = DriverState.ATTENTIVE,
                    earScore = 0.245f,
                    rawEar = 0.251f,
                    fps = 30,
                    inferenceTimeMs = 18L,
                    limiteEar = 0.15f,
                    isFrontCamera = true,
                    isTorchEnabled = false,
                    hasFlashUnit = true,
                    onToggleCamera = {},
                    onToggleTorch = {},
                    onOpenSpecsDialog = {},
                    showSettings = false,
                    onToggleSettings = {}
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPanelPreview() {
    AplicacionDeSomnolenciaTheme {
        SettingsPanelCard(
            limiteEar = 0.15f,
            limiteFrames = 15,
            isAudioEnabled = true,
            isHapticEnabled = true,
            customAudioUri = null,
            onLimiteEarChanged = {},
            onLimiteFramesChanged = {},
            onAudioToggle = {},
            onHapticToggle = {},
            onSelectAudio = {},
            onClearAudio = {},
            onResetDefaults = {},
            onClose = {}
        )
    }
}
