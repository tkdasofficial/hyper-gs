package com.hyper.game.space.ui
import android.content.Intent
import com.hyper.game.space.ui.ScreenRecordPromptActivity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hyper.game.space.data.SettingsRepository
import com.hyper.game.space.viewmodel.FeaturesViewModel
import kotlin.math.abs

@Composable
fun CornerTrigger(isLeft: Boolean, onTriggered: () -> Unit) {
    // 50x50 transparent box that listens for diagonal inward swipe
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val dx = dragAmount.x
                        val dy = dragAmount.y
                        
                        // Check for diagonal inward movement
                        // If left corner: drag must be positive X and positive Y
                        // If right corner: drag must be negative X and positive Y
                        val isDiagonal = if (isLeft) {
                            dx > 15 && dy > 15
                        } else {
                            dx < -15 && dy > 15
                        }

                        if (isDiagonal) {
                            onTriggered()
                        }
                    }
                )
            }
    )
}

@Composable
fun OverlayModal(fromLeft: Boolean, onClose: () -> Unit) {
    val context = LocalContext.current
    // Using a new instance of FeaturesViewModel manually or via factory is needed in service, 
    // but since we are not in an Activity, standard viewModel() might crash if not properly set up.
    // However, our MyLifecycleOwner provides a ViewModelStore, so viewModel() works.
    val viewModel: FeaturesViewModel = viewModel(factory = FeaturesViewModel.Factory(context))
    
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = {
                visible = false
            }),
        contentAlignment = if (fromLeft) Alignment.TopStart else Alignment.TopEnd
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.8f),
            exit = fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.8f)
        ) {
            DisposableEffect(Unit) {
                onDispose {
                    if (!visible) onClose()
                }
            }

            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .width(360.dp)
                    .clickable(enabled = false) {}, // Intercept clicks so they don't close the modal
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xEB000000)) // 92% opacity black
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, Color.Cyan, RoundedCornerShape(0.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        // Top Header: Live Telemetry
                        TelemetryHeader(viewModel)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color.Cyan.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Toggles
                        QuickTogglesSection(viewModel)

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color.Cyan.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))

                        // Interactive Sliders
                        SlidersSection(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetryHeader(viewModel: FeaturesViewModel) {
    val cpuLoad by viewModel.cpuLoad.collectAsState()
    val gpuLoad by viewModel.gpuLoad.collectAsState()
    val ramUsage by viewModel.ramUsage.collectAsState()
    val liveFps by viewModel.liveFps.collectAsState()

    Column {
        Text("FPS: $liveFps / 60Hz - HARDWARE LOCKED", style = MaterialTheme.typography.titleSmall, color = Color.Cyan)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatItem("CPU", "$cpuLoad%")
            StatItem("GPU", "$gpuLoad%")
            StatItem("RAM", "$ramUsage%")
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.White)
    }
}

@Composable
fun QuickTogglesSection(viewModel: FeaturesViewModel) {
    val context = LocalContext.current
    val dnd by viewModel.dndThirdParty.collectAsState()
    val ramBoost by viewModel.autoRamBoost.collectAsState()
    val overload by viewModel.overloadOptimizer.collectAsState()
    var vSensEnabled by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OverlayToggleRow("Do Not Disturb", dnd) { viewModel.setBoolean(SettingsRepository.DND_THIRD_PARTY, it) }
        OverlayToggleRow("Memory Optimizer", ramBoost) { viewModel.setBoolean(SettingsRepository.AUTO_RAM_BOOST, it) }
        OverlayToggleRow("System Optimizer", overload) { viewModel.setBoolean(SettingsRepository.OVERLOAD_OPTIMIZER, it) }
        OverlayToggleRow("Network Traffic Manager", true) { }
        OverlayToggleRow("Brightness Lock", true) { }
        
        OverlayToggleRow("Virtual Sensitivity", vSensEnabled) { isChecked ->
            if (isChecked) {
                if (com.hyper.game.space.service.VSensitivityService.instance != null) {
                    vSensEnabled = true
                } else {
                    android.widget.Toast.makeText(context, "System restriction detected. Please re-enable Accessibility.", android.widget.Toast.LENGTH_LONG).show()
                    vSensEnabled = false
                }
            } else {
                vSensEnabled = false
            }
        }
        
        Button(
            onClick = {
                val intent = Intent(context, ScreenRecordPromptActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
            shape = RoundedCornerShape(4.dp)
        ) {
            Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("REC Quick Action", color = Color.White)
        }
    }
}

@Composable
fun OverlayToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = if (checked) Color.Cyan else Color.White)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Cyan,
                checkedTrackColor = Color.Cyan.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun SlidersSection(viewModel: FeaturesViewModel) {
    val vSensX by viewModel.vSensX.collectAsState()
    val vSensY by viewModel.vSensY.collectAsState()
    val vSensZ by viewModel.vSensZ.collectAsState()

    Column {
        Text("V-Sens Axis Control", style = MaterialTheme.typography.labelMedium, color = Color.Cyan)
        OverlaySlider("X-Axis", vSensX) { viewModel.setFloat(SettingsRepository.VSENS_X, it) }
        OverlaySlider("Y-Axis", vSensY) { viewModel.setFloat(SettingsRepository.VSENS_Y, it) }
        OverlaySlider("Z-Axis", vSensZ) { viewModel.setFloat(SettingsRepository.VSENS_Z, it) }
    }
}

@Composable
fun OverlaySlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
            Text("${(value * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = Color.Cyan)
        }
        Slider(
            value = value,
            valueRange = 0f..10f,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = Color.Cyan,
                activeTrackColor = Color.Cyan,
                inactiveTrackColor = Color.DarkGray
            )
        )
    }
}
