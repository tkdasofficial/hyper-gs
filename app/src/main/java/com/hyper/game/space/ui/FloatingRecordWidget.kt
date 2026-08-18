package com.hyper.game.space.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hyper.game.space.viewmodel.FeaturesViewModel
import com.hyper.game.space.data.SettingsRepository
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings

@Composable
fun FloatingRecordWidget(
    isPaused: Boolean,
    onPauseResume: () -> Unit,
    onStop: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showQuickSettings by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val viewModel: FeaturesViewModel = viewModel(factory = FeaturesViewModel.Factory(context))

    if (showQuickSettings) {
        // Quick Settings Panel Morph
        Card(
            modifier = Modifier
                .width(280.dp)
                .padding(4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xEB000000))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Recorder Quick Settings", color = Color.White, style = MaterialTheme.typography.titleSmall)
                    IconButton(onClick = { showQuickSettings = false }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                val res by viewModel.recorderResolution.collectAsState()
                val fps by viewModel.recorderFps.collectAsState()
                val audio by viewModel.recorderAudioSource.collectAsState()

                Text("Resolution: $res", color = Color.Cyan, style = MaterialTheme.typography.bodySmall)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MiniChip("720p", res == "720p HD") { viewModel.setString(SettingsRepository.RECORDER_RESOLUTION, "720p HD") }
                    MiniChip("1080p", res == "1080p FHD") { viewModel.setString(SettingsRepository.RECORDER_RESOLUTION, "1080p FHD") }
                    MiniChip("4K", res == "4K UHD") { viewModel.setString(SettingsRepository.RECORDER_RESOLUTION, "4K UHD") }
                }

                Text("FPS: $fps", color = Color.Cyan, style = MaterialTheme.typography.bodySmall)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MiniChip("30", fps == 30) { viewModel.setInt(SettingsRepository.RECORDER_FPS, 30) }
                    MiniChip("60", fps == 60) { viewModel.setInt(SettingsRepository.RECORDER_FPS, 60) }
                    MiniChip("90", fps == 90) { viewModel.setInt(SettingsRepository.RECORDER_FPS, 90) }
                }

                Text("Audio: $audio", color = Color.Cyan, style = MaterialTheme.typography.bodySmall)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MiniChip("Dual", audio == "Dual-Audio") { viewModel.setString(SettingsRepository.RECORDER_AUDIO_SOURCE, "Dual-Audio") }
                    MiniChip("Mic", audio == "Mic Only") { viewModel.setString(SettingsRepository.RECORDER_AUDIO_SOURCE, "Mic Only") }
                    MiniChip("Mute", audio == "Mute") { viewModel.setString(SettingsRepository.RECORDER_AUDIO_SOURCE, "Mute") }
                }
            }
        }
    } else {
        Row(
            modifier = Modifier
                .background(Color(0xBB000000), shape = CircleShape)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main Record Indicator / Toggle Expansion
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isPaused) Color.DarkGray else Color.Red.copy(alpha = 0.8f))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { expanded = !expanded },
                            onLongPress = { showQuickSettings = true }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isPaused) "PAUSED" else "REC",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            Row(modifier = Modifier.padding(start = 8.dp)) {
                IconButton(onClick = onPauseResume) {
                    Icon(
                        if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "Pause/Resume",
                        tint = Color.White
                    )
                }
                IconButton(onClick = onStop) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = "Stop & Save",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { /* Toggle Mic */ }) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Toggle Mic",
                        tint = Color.Cyan
                    )
                }
                IconButton(onClick = { /* Trigger Highlight 10s Replay */ }) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Save Highlight Buffer",
                        tint = Color.Yellow
                    )
                }
            }
        }
    }
}
}

@Composable
fun MiniChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color.Cyan else Color.DarkGray)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.Black else Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
