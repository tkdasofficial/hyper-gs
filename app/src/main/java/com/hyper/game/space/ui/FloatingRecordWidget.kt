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

@Composable
fun FloatingRecordWidget(
    isPaused: Boolean,
    onPauseResume: () -> Unit,
    onStop: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

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
                .clickable { expanded = !expanded },
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
            }
        }
    }
}
