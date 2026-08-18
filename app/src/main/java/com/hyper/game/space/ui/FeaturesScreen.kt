package com.hyper.game.space.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hyper.game.space.viewmodel.FeaturesViewModel
import com.hyper.game.space.data.SettingsRepository

enum class ActiveModal {
    NONE, VSENSITIVITY, HARDWARE_DISPLAY, DND, THERMAL, GPU, TOUCH_RESPONSE, AUDIO_EQ, RESOLUTION_SCALER, GHOST_TOUCH, SCREEN_RECORDER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeaturesScreen(viewModel: FeaturesViewModel = viewModel()) {
    var activeModal by remember { mutableStateOf(ActiveModal.NONE) }

    // Telemetry
    val cpuLoad by viewModel.cpuLoad.collectAsState()
    val gpuLoad by viewModel.gpuLoad.collectAsState()
    val ramUsage by viewModel.ramUsage.collectAsState()
    
    // Toggles
    val autoRamBoost by viewModel.autoRamBoost.collectAsState()
    val deepKillEngine by viewModel.deepKillEngine.collectAsState()
    val fpsDropShield by viewModel.fpsDropShield.collectAsState()
    val autoCrashRecovery by viewModel.autoCrashRecovery.collectAsState()
    val screenBrightnessLock by viewModel.screenBrightnessLock.collectAsState()
    val networkLatencySaver by viewModel.networkLatencySaver.collectAsState()
    val dedicatedBandwidth by viewModel.dedicatedBandwidth.collectAsState()
    val bypassCharging by viewModel.bypassCharging.collectAsState()
    val overloadOptimizer by viewModel.overloadOptimizer.collectAsState()

    // Status Strings
    val vSensX by viewModel.vSensX.collectAsState()
    val vSensY by viewModel.vSensY.collectAsState()
    val vSensZ by viewModel.vSensZ.collectAsState()
    val liveFps by viewModel.liveFps.collectAsState()
    val refreshLock by viewModel.refreshLock.collectAsState()
    val dndThirdParty by viewModel.dndThirdParty.collectAsState()
    val dndCalls by viewModel.dndCalls.collectAsState()
    val dndSms by viewModel.dndSms.collectAsState()
    val thermalProfile by viewModel.thermalProfile.collectAsState()
    val renderer by viewModel.renderer.collectAsState()
    val sensitivityProfile by viewModel.sensitivityProfile.collectAsState()
    val audioMode by viewModel.audioMode.collectAsState()
    val resolutionProfile by viewModel.resolutionProfile.collectAsState()
    val calibrationState by viewModel.calibrationState.collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                item { SystemMonitorCard("RAM Usage", "$ramUsage%", Icons.Default.Memory) }
                item { SystemMonitorCard("CPU Load", "$cpuLoad%", Icons.Default.DeveloperBoard) }
                item { SystemMonitorCard("GPU Load", "$gpuLoad%", Icons.Default.GraphicEq) }
            }
        }

        // --- SYSTEM OPTIMIZATIONS ---
        item { SectionHeader("System Optimizations") }
        item {
            val recEnabled by viewModel.recorderEnabled.collectAsState()
            FeatureModalCard(
                title = "Screen Recorder",
                status = if (recEnabled) "Recording Engine Enabled" else "Recording Engine Disabled",
                icon = Icons.Default.Videocam,
                onClick = { activeModal = ActiveModal.SCREEN_RECORDER }
            )
        }
        item {
            FeatureToggleCard(
                title = "System Optimizer",
                status = if (overloadOptimizer) "Overload Shield: Active" else "Overload Shield: Disabled",
                icon = Icons.Default.Security,
                state = overloadOptimizer,
                onCheckedChange = { viewModel.setBoolean(SettingsRepository.OVERLOAD_OPTIMIZER, it) }
            )
        }
        item {
            FeatureToggleCard(
                title = "Memory Optimizer",
                status = "Dynamic RAM Management",
                icon = Icons.Default.Memory,
                state = autoRamBoost,
                onCheckedChange = { viewModel.setBoolean(SettingsRepository.AUTO_RAM_BOOST, it) }
            )
        }
        item {
            FeatureToggleCard(
                title = "Force Stop Activities",
                status = "Force stop background apps",
                icon = Icons.Default.Stop,
                state = deepKillEngine,
                onCheckedChange = { viewModel.setBoolean(SettingsRepository.DEEP_KILL_ENGINE, it) }
            )
        }
        item {
            FeatureToggleCard(
                title = "Frame Rate Stabilizer",
                status = if (fpsDropShield) "FPS Drop Shield: Enabled" else "FPS Drop Shield: Disabled",
                icon = Icons.Default.Shield,
                state = fpsDropShield,
                onCheckedChange = { viewModel.setBoolean(SettingsRepository.FPS_DROP_SHIELD, it) }
            )
        }
        item {
            FeatureToggleCard(
                title = "Crash Recovery",
                status = if (autoCrashRecovery) "Auto Crash-Recovery Monitor: Active" else "Auto Crash-Recovery Monitor: Standby",
                icon = Icons.Default.Healing,
                state = autoCrashRecovery,
                onCheckedChange = { viewModel.setBoolean(SettingsRepository.AUTO_CRASH_RECOVERY, it) }
            )
        }

        // --- DISPLAY & GRAPHICS ---
        item { SectionHeader("Display & Graphics") }
        item {
            FeatureModalCard(
                title = "Refresh Rate Stabilizer",
                status = "Locks stable refresh rate",
                icon = Icons.Default.MonitorHeart,
                onClick = { activeModal = ActiveModal.HARDWARE_DISPLAY }
            )
        }
        item {
            FeatureModalCard(
                title = "Graphics Renderer",
                status = "$renderer Optimized",
                icon = Icons.Default.GraphicEq,
                onClick = { activeModal = ActiveModal.GPU }
            )
        }
        item {
            FeatureModalCard(
                title = "Resolution Scaler",
                status = "Resolution: $resolutionProfile",
                icon = Icons.Default.FitScreen,
                onClick = { activeModal = ActiveModal.RESOLUTION_SCALER }
            )
        }
        item {
            FeatureToggleCard(
                title = "Brightness Lock",
                status = "Locks screen brightness level",
                icon = Icons.Default.BrightnessAuto,
                state = screenBrightnessLock,
                onCheckedChange = { viewModel.setBoolean(SettingsRepository.SCREEN_BRIGHTNESS_LOCK, it) }
            )
        }

        // --- TOUCH & SCREEN ---
        item { SectionHeader("Touch & Controls") }
        item {
            FeatureModalCard(
                title = "Touch Calibration",
                status = "Touch Calibration: $calibrationState",
                icon = Icons.Default.PanTool,
                onClick = { activeModal = ActiveModal.GHOST_TOUCH }
            )
        }
        item {
            FeatureModalCard(
                title = "Virtual Sensitivity",
                status = "X: ${(vSensX * 100).toInt()}% | Y: ${(vSensY * 100).toInt()}% | Z: ${(vSensZ * 100).toInt()}%",
                icon = Icons.Default.ControlCamera,
                onClick = { activeModal = ActiveModal.VSENSITIVITY }
            )
        }

        // --- SOUND & FX ---
        item { SectionHeader("Sound & Audio") }
        item {
            FeatureModalCard(
                title = "Audio Equalizer",
                status = "Audio Mode: $audioMode",
                icon = Icons.Default.VolumeUp,
                onClick = { activeModal = ActiveModal.AUDIO_EQ }
            )
        }

        // --- NETWORK & CONNECTIVITY ---
        item { SectionHeader("Network & Latency") }
        item {
            FeatureModalCard(
                title = "Do Not Disturb",
                status = "Third-Party: ${if(dndThirdParty) "Blocked" else "Allowed"} | Calls: ${if(dndCalls) "Muted" else "Allowed"}",
                icon = Icons.Default.DoNotDisturbOn,
                onClick = { activeModal = ActiveModal.DND }
            )
        }
        item {
            FeatureToggleCard(
                title = "Latency Optimizer (Ping Management)",
                status = if (networkLatencySaver) "Priority Bandwidth: ON" else "Priority Bandwidth: OFF",
                icon = Icons.Default.Speed,
                state = networkLatencySaver,
                onCheckedChange = { viewModel.setBoolean(SettingsRepository.NETWORK_LATENCY_SAVER, it) }
            )
        }
        item {
            FeatureToggleCard(
                title = "Network Traffic Manager",
                status = "Prioritizes gaming network bandwidth",
                icon = Icons.Default.WifiTethering,
                state = dedicatedBandwidth,
                onCheckedChange = { viewModel.setBoolean(SettingsRepository.DEDICATED_BANDWIDTH, it) }
            )
        }

        // --- BATTERY & POWER ---
        item { SectionHeader("Battery & Thermal") }
        item {
            FeatureModalCard(
                title = "Thermal Controller",
                status = "Thermal Mode: $thermalProfile",
                icon = Icons.Default.AcUnit,
                onClick = { activeModal = ActiveModal.THERMAL }
            )
        }
        item {
            FeatureToggleCard(
                title = "Bypass Charging & Thermal Protection",
                status = if (bypassCharging) "Thermal Charge Protection: Active" else "Standard Charging",
                icon = Icons.Default.BatteryChargingFull,
                state = bypassCharging,
                onCheckedChange = { viewModel.setBoolean(SettingsRepository.BYPASS_CHARGING, it) }
            )
        }
    }

    if (activeModal != ActiveModal.NONE) {
        ModalBottomSheet(
            onDismissRequest = { activeModal = ActiveModal.NONE },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                when (activeModal) {
                    ActiveModal.VSENSITIVITY -> VSensitivityModalContent(viewModel)
                    ActiveModal.HARDWARE_DISPLAY -> HardwareDisplayModalContent(viewModel)
                    ActiveModal.DND -> DNDModalContent(viewModel)
                    ActiveModal.THERMAL -> ThermalModalContent(viewModel)
                    ActiveModal.GPU -> GPUModalContent(viewModel)
                    ActiveModal.TOUCH_RESPONSE -> TouchResponseModalContent(viewModel)
                    ActiveModal.AUDIO_EQ -> AudioEqModalContent(viewModel)
                    ActiveModal.RESOLUTION_SCALER -> ResolutionScalerModalContent(viewModel)
                    ActiveModal.GHOST_TOUCH -> GhostTouchModalContent(viewModel)
                    ActiveModal.SCREEN_RECORDER -> ScreenRecorderModalContent(viewModel)
                    ActiveModal.NONE -> {}
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 4.dp)
    )
}

@Composable
fun SystemMonitorCard(title: String, percentage: String, icon: ImageVector) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.width(140.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = percentage, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun FeatureModalCard(title: String, status: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun FeatureToggleCard(title: String, status: String, icon: ImageVector, state: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (state) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = status, style = MaterialTheme.typography.bodySmall, color = if (state) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = state, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun VSensitivityModalContent(viewModel: FeaturesViewModel) {
    val x by viewModel.vSensX.collectAsState()
    val y by viewModel.vSensY.collectAsState()
    val z by viewModel.vSensZ.collectAsState()
    Text("Virtual Sensitivity", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
    SliderSetting("X-Axis Touch Response", x, 0f..10f) { viewModel.setFloat(SettingsRepository.VSENS_X, it) }
    SliderSetting("Y-Axis Touch Response", y, 0f..10f) { viewModel.setFloat(SettingsRepository.VSENS_Y, it) }
    SliderSetting("Z-Axis Touch Response", z, 0f..10f) { viewModel.setFloat(SettingsRepository.VSENS_Z, it) }
}

@Composable
fun HardwareDisplayModalContent(viewModel: FeaturesViewModel) {
    val hwSync by viewModel.hwSync.collectAsState()
    val refreshLock by viewModel.refreshLock.collectAsState()
    val showLiveFps by viewModel.showLiveFps.collectAsState()
    val liveFps by viewModel.liveFps.collectAsState()
    Text("Refresh Rate Stabilizer", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
    ToggleSetting("SurfaceFlinger Hardware Sync", hwSync) { viewModel.setBoolean(SettingsRepository.HW_SYNC, it) }
    ToggleSetting("Refresh Rate Lock", refreshLock) { viewModel.setBoolean(SettingsRepository.REFRESH_LOCK, it) }
    ToggleSetting("Real-time Hardware FPS Detector", showLiveFps) { viewModel.setBoolean(SettingsRepository.LIVE_FPS, it) }
    
    if (showLiveFps) {
        Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Live Detected FPS", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("$liveFps FPS", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
fun DNDModalContent(viewModel: FeaturesViewModel) {
    val tp by viewModel.dndThirdParty.collectAsState()
    val calls by viewModel.dndCalls.collectAsState()
    val sms by viewModel.dndSms.collectAsState()
    Text("Do Not Disturb", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
    ToggleSetting("Third-Party App DND", tp) { viewModel.setBoolean(SettingsRepository.DND_THIRD_PARTY, it) }
    ToggleSetting("System Call DND", calls) { viewModel.setBoolean(SettingsRepository.DND_CALLS, it) }
    ToggleSetting("SMS/System Notification DND", sms) { viewModel.setBoolean(SettingsRepository.DND_SMS, it) }
}

@Composable
fun ThermalModalContent(viewModel: FeaturesViewModel) {
    val tg by viewModel.thermalGovernor.collectAsState()
    val tp by viewModel.thermalProfile.collectAsState()
    Text("Thermal Controller", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
    ToggleSetting("CPU Throttling Governor", tg) { viewModel.setBoolean(SettingsRepository.THERMAL_GOVERNOR, it) }
    Spacer(modifier = Modifier.height(16.dp))
    Text("Thermal Profiles", style = MaterialTheme.typography.labelLarge)
    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ProfileChip("Balanced", tp == "Balanced") { viewModel.setString(SettingsRepository.THERMAL_PROFILE, "Balanced") }
        ProfileChip("Smart Cooling", tp == "Smart Cooling") { viewModel.setString(SettingsRepository.THERMAL_PROFILE, "Smart Cooling") }
        ProfileChip("Turbo", tp == "Turbo") { viewModel.setString(SettingsRepository.THERMAL_PROFILE, "Turbo") }
    }
}

@Composable
fun GPUModalContent(viewModel: FeaturesViewModel) {
    val fo by viewModel.forceOptimize.collectAsState()
    val rnd by viewModel.renderer.collectAsState()
    Text("Graphics Renderer", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
    ToggleSetting("Force-optimize system graphics", fo) { viewModel.setBoolean(SettingsRepository.FORCE_OPTIMIZE, it) }
    Spacer(modifier = Modifier.height(16.dp))
    Text("Renderer", style = MaterialTheme.typography.labelLarge)
    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ProfileChip("Vulkan", rnd == "Vulkan") { viewModel.setString(SettingsRepository.RENDERER, "Vulkan") }
        ProfileChip("OpenGL", rnd == "OpenGL") { viewModel.setString(SettingsRepository.RENDERER, "OpenGL") }
    }
}

@Composable
fun TouchResponseModalContent(viewModel: FeaturesViewModel) {
    val tl by viewModel.touchLatency.collectAsState()
    val pa by viewModel.pollingAccel.collectAsState()
    val dopt by viewModel.dragOptimize.collectAsState()
    val prof by viewModel.sensitivityProfile.collectAsState()
    Text("Touch Response & Sampling Rate Booster", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
    ToggleSetting("Touch Latency Reduction", tl) { viewModel.setBoolean(SettingsRepository.TOUCH_LATENCY, it) }
    ToggleSetting("Polling Rate Acceleration", pa) { viewModel.setBoolean(SettingsRepository.POLLING_ACCEL, it) }
    ToggleSetting("Drag-Response Optimization", dopt) { viewModel.setBoolean(SettingsRepository.DRAG_OPTIMIZE, it) }
    Spacer(modifier = Modifier.height(16.dp))
    Text("Sensitivity Profile", style = MaterialTheme.typography.labelLarge)
    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ProfileChip("Standard", prof == "Standard") { viewModel.setString(SettingsRepository.SENSITIVITY_PROFILE, "Standard") }
        ProfileChip("Gaming", prof == "Gaming") { viewModel.setString(SettingsRepository.SENSITIVITY_PROFILE, "Gaming") }
        ProfileChip("Ultra", prof == "Ultra") { viewModel.setString(SettingsRepository.SENSITIVITY_PROFILE, "Ultra") }
    }
}

@Composable
fun AudioEqModalContent(viewModel: FeaturesViewModel) {
    val sa by viewModel.spatialAudio.collectAsState()
    val mode by viewModel.audioMode.collectAsState()
    Text("Audio Equalizer", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
    ToggleSetting("Spatial Audio Clarity", sa) { viewModel.setBoolean(SettingsRepository.SPATIAL_AUDIO, it) }
    Spacer(modifier = Modifier.height(16.dp))
    Text("Audio Mode", style = MaterialTheme.typography.labelLarge)
    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ProfileChip("Standard", mode == "Standard") { viewModel.setString(SettingsRepository.AUDIO_MODE, "Standard") }
        ProfileChip("Footstep Enhancer", mode == "Footstep Enhancer") { viewModel.setString(SettingsRepository.AUDIO_MODE, "Footstep Enhancer") }
        ProfileChip("Spatial 3D", mode == "Spatial 3D") { viewModel.setString(SettingsRepository.AUDIO_MODE, "Spatial 3D") }
    }
}

@Composable
fun ResolutionScalerModalContent(viewModel: FeaturesViewModel) {
    val res by viewModel.resolutionProfile.collectAsState()
    Text("Screen Resolution & Density Scaler", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
    Text("Scaling reduces rendering density to boost raw FPS on mid/low-spec hardware.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))
    Text("Resolution Profile", style = MaterialTheme.typography.labelLarge)
    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ProfileChip("100% Native", res == "100% Native") { viewModel.setString(SettingsRepository.RESOLUTION_PROFILE, "100% Native") }
        ProfileChip("85% Scaled", res == "85% Scaled") { viewModel.setString(SettingsRepository.RESOLUTION_PROFILE, "85% Scaled") }
        ProfileChip("720p Scaled", res == "720p Scaled") { viewModel.setString(SettingsRepository.RESOLUTION_PROFILE, "720p Scaled") }
    }
}

@Composable
fun GhostTouchModalContent(viewModel: FeaturesViewModel) {
    val gf by viewModel.ghostFilter.collectAsState()
    val ath by viewModel.autoTouchHeat.collectAsState()
    val calib by viewModel.calibrationState.collectAsState()
    Text("Touch Calibration", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
    ToggleSetting("Filter Accidental Ghost-Touches", gf) { viewModel.setBoolean(SettingsRepository.GHOST_FILTER, it) }
    ToggleSetting("Anti Heat-Induced Auto-Touch", ath) { viewModel.setBoolean(SettingsRepository.AUTO_TOUCH_HEAT, it) }
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = { viewModel.triggerDigitizerCalibration() },
        modifier = Modifier.fillMaxWidth(),
        enabled = calib == "Safe" || calib == "Calibrated & Safe"
    ) {
        Text(if (calib == "Calibrating...") "Calibrating Digitizer..." else "Calibrate Digitizer Now")
    }
}

@Composable
fun ScreenRecorderModalContent(viewModel: FeaturesViewModel) {
    val enabled by viewModel.recorderEnabled.collectAsState()
    val res by viewModel.recorderResolution.collectAsState()
    val fps by viewModel.recorderFps.collectAsState()
    val bitrate by viewModel.recorderBitrate.collectAsState()
    val audio by viewModel.recorderAudioSource.collectAsState()
    val orientation by viewModel.recorderOrientation.collectAsState()

    Text("Screen Recorder", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
    ToggleSetting("Enable Recording Engine", enabled) { viewModel.setBoolean(SettingsRepository.RECORDER_ENABLED, it) }
    
    Spacer(modifier = Modifier.height(16.dp))
    Text("Resolution", style = MaterialTheme.typography.labelLarge)
    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ProfileChip("720p HD", res == "720p HD") { viewModel.setString(SettingsRepository.RECORDER_RESOLUTION, "720p HD") }
        ProfileChip("1080p FHD", res == "1080p FHD") { viewModel.setString(SettingsRepository.RECORDER_RESOLUTION, "1080p FHD") }
        ProfileChip("2K QHD", res == "2K QHD") { viewModel.setString(SettingsRepository.RECORDER_RESOLUTION, "2K QHD") }
        ProfileChip("4K UHD", res == "4K UHD") { viewModel.setString(SettingsRepository.RECORDER_RESOLUTION, "4K UHD") }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    Text("Frame Rate", style = MaterialTheme.typography.labelLarge)
    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ProfileChip("30 FPS", fps == 30) { viewModel.setInt(SettingsRepository.RECORDER_FPS, 30) }
        ProfileChip("60 FPS", fps == 60) { viewModel.setInt(SettingsRepository.RECORDER_FPS, 60) }
        ProfileChip("90 FPS", fps == 90) { viewModel.setInt(SettingsRepository.RECORDER_FPS, 90) }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Bitrate (Mbps)", style = MaterialTheme.typography.bodyMedium)
            Text(text = "${bitrate} Mbps", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = bitrate.toFloat(),
            valueRange = 1f..64f,
            onValueChange = { viewModel.setInt(SettingsRepository.RECORDER_BITRATE, it.toInt()) }
        )
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text("Audio Source", style = MaterialTheme.typography.labelLarge)
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProfileChip("System Audio", audio == "System Audio") { viewModel.setString(SettingsRepository.RECORDER_AUDIO_SOURCE, "System Audio") }
            ProfileChip("Mic Only", audio == "Mic Only") { viewModel.setString(SettingsRepository.RECORDER_AUDIO_SOURCE, "Mic Only") }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProfileChip("Dual-Audio", audio == "Dual-Audio") { viewModel.setString(SettingsRepository.RECORDER_AUDIO_SOURCE, "Dual-Audio") }
            ProfileChip("Mute", audio == "Mute") { viewModel.setString(SettingsRepository.RECORDER_AUDIO_SOURCE, "Mute") }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    Text("Orientation", style = MaterialTheme.typography.labelLarge)
    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ProfileChip("Auto", orientation == "Auto-Detect") { viewModel.setString(SettingsRepository.RECORDER_ORIENTATION, "Auto-Detect") }
        ProfileChip("Landscape", orientation == "Landscape") { viewModel.setString(SettingsRepository.RECORDER_ORIENTATION, "Landscape") }
        ProfileChip("Portrait", orientation == "Portrait") { viewModel.setString(SettingsRepository.RECORDER_ORIENTATION, "Portrait") }
    }
}

@Composable
fun SliderSetting(label: String, value: Float, valueRange: ClosedFloatingPointRange<Float> = 0f..1f, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(text = "${(value * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
        Slider(value = value, valueRange = valueRange, onValueChange = onValueChange)
    }
}

@Composable
fun ToggleSetting(label: String, state: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = state, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun ProfileChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
