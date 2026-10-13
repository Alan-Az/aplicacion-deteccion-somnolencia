package com.example.aplicaciondesomnolencia.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.aplicaciondesomnolencia.R
import com.example.aplicaciondesomnolencia.camera.CameraSpecs
import com.example.aplicaciondesomnolencia.camera.CameraStatusVerdict

@Composable
fun CameraSpecsDialog(
    cameraSpecs: CameraSpecs,
    isTorchEnabled: Boolean,
    onToggleTorch: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF141824),
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF2B3245),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.app_logo),
                                    contentDescription = "SomnolenIA Logo",
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "SomnolenIA Mobile",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Capacidades de Cámara & Modo Nocturno",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.LightGray
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Verdict Badge Banner
                VerdictBadgeBanner(verdict = cameraSpecs.statusVerdict)

                Spacer(modifier = Modifier.height(16.dp))

                // Hardware Specifications Grid Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2433)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Especificaciones Detectadas",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF82B1FF)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SpecItem(label = "Lente Activo", value = cameraSpecs.lensFacingName)
                            SpecItem(label = "Resolución", value = cameraSpecs.resolutionLabel)
                            SpecItem(label = "FPS Máx", value = "${cameraSpecs.maxFps} FPS")
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SpecItem(
                                label = "Linterna/Flash",
                                value = if (cameraSpecs.hasFlashUnit) "Disponible" else "No disponible"
                            )
                            SpecItem(
                                label = "Comp. Exposición",
                                value = if (cameraSpecs.isExposureCompensationSupported)
                                    "[${cameraSpecs.exposureRangeMin}, +${cameraSpecs.exposureRangeMax}]"
                                else "No soportada"
                            )
                            SpecItem(
                                label = "Zoom Óptico",
                                value = String.format(java.util.Locale.US, "%.1fx - %.1fx", cameraSpecs.minZoomRatio, cameraSpecs.maxZoomRatio)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Modo Noche / Linterna Assist Interactive Switch Row
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isTorchEnabled) Color(0xFF2E3B1F) else Color(0xFF1E2433)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isTorchEnabled) Color(0xFF7CB342) else Color.White.copy(alpha = 0.15f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = null,
                                tint = if (isTorchEnabled) Color(0xFFFFD54F) else Color.Gray,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Modo Noche / Linterna Assist",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (cameraSpecs.hasFlashUnit)
                                        "Aumenta la exposición y activa luz frontal para baja iluminación nocturna"
                                    else
                                        "Esta cámara no cuenta con flash físico para luz frontal",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.LightGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Switch(
                            checked = isTorchEnabled,
                            onCheckedChange = { onToggleTorch() },
                            enabled = cameraSpecs.hasFlashUnit,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF689F38)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Checklist Section: Mínimos vs Recomendados
                Text(
                    text = "Evaluación de Requisitos",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F2C)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "📌 Requisitos Mínimos Indispensables",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        ChecklistItem(
                            isMet = cameraSpecs.meetsMinCameraPresence,
                            title = "Cámara activa (Frontal / Trasera)",
                            subtitle = "Lente: ${cameraSpecs.lensFacingName}"
                        )
                        ChecklistItem(
                            isMet = cameraSpecs.meetsMinResolution,
                            title = "Resolución mínima >= 480p",
                            subtitle = "Detectada: ${cameraSpecs.resolutionLabel}"
                        )
                        ChecklistItem(
                            isMet = cameraSpecs.meetsMinFps,
                            title = "Captura de fotogramas >= 15 FPS",
                            subtitle = "Tasa actual: ${cameraSpecs.maxFps} FPS"
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "🌙 Recomendaciones para Conducción Nocturna",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF81D4FA)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        ChecklistItem(
                            isMet = cameraSpecs.hasFlashUnit,
                            title = "Flash / Linterna para entornos oscuros",
                            subtitle = if (cameraSpecs.hasFlashUnit) "Unidad de flash disponible" else "Sin flash en lente actual"
                        )
                        ChecklistItem(
                            isMet = cameraSpecs.isExposureCompensationSupported,
                            title = "Compensación de exposición (AE)",
                            subtitle = if (cameraSpecs.isExposureCompensationSupported) "Ajuste de ganancia disponible" else "No soportado"
                        )
                        ChecklistItem(
                            isMet = cameraSpecs.meetsRecResolution,
                            title = "Resolución HD recomendada >= 720p",
                            subtitle = "Mayor resolución mejora precisión en párpados"
                        )
                        ChecklistItem(
                            isMet = cameraSpecs.meetsRecFps,
                            title = "Fluidez recomendada >= 30 FPS",
                            subtitle = "Permite capturar micro-parpadeos rápidos"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // EAR Accuracy Explanation Note
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF132030)),
                    border = BorderStroke(1.dp, Color(0xFF1E88E5).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.RemoveRedEye,
                            contentDescription = null,
                            tint = Color(0xFF64B5F6),
                            modifier = Modifier
                                .size(24.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "¿Por qué la iluminación y resolución afectan al EAR?",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF90CAF9)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "El índice EAR (Eye Aspect Ratio) calcula la apertura ocular a partir de los puntos faciales (landmarks). En la noche o con sombras intensas, el contraste entre la esclerótica y las pestañas disminuye. La linterna assist y una buena resolución garantizan que el sistema identifique con precisión la apertura del ojo, evitando falsas alarmas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2962FF))
                ) {
                    Text(
                        text = "Entendido / Cerrar",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun VerdictBadgeBanner(verdict: CameraStatusVerdict) {
    val (backgroundColor, borderColor, icon, iconTint) = when (verdict) {
        CameraStatusVerdict.OPTIMAL -> Quadruple(
            Color(0xFF1B5E20),
            Color(0xFF4CAF50),
            Icons.Default.CheckCircle,
            Color(0xFF81C784)
        )
        CameraStatusVerdict.COMPATIBLE -> Quadruple(
            Color(0xFF0D47A1),
            Color(0xFF2196F3),
            Icons.Default.Info,
            Color(0xFF64B5F6)
        )
        CameraStatusVerdict.LIMITED_LOW_LIGHT -> Quadruple(
            Color(0xFFE65100),
            Color(0xFFFF9800),
            Icons.Default.Warning,
            Color(0xFFFFB74D)
        )
        CameraStatusVerdict.INSUFFICIENT -> Quadruple(
            Color(0xFFB71C1C),
            Color(0xFFF44336),
            Icons.Default.Warning,
            Color(0xFFE57373)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = verdict.badgeTitle,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = verdict.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun SpecItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.LightGray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun ChecklistItem(isMet: Boolean, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isMet) Color(0xFF66BB6A) else Color(0xFFFF7043),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
