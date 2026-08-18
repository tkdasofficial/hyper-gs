package com.hyper.game.space.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

enum class SlotState { WAITING, LOADING, DONE, EXITING, GONE }

data class HudItem(val label: String, val icon: ImageVector)

@Composable
fun ActivationHud(onFinish: () -> Unit) {
    val items = remember {
        listOf(
            HudItem("Memory Boost", Icons.Default.Memory),
            HudItem("Network QoS Policy", Icons.Default.NetworkCheck),
            HudItem("Overload Shield", Icons.Default.Security),
            HudItem("DND Rules Engine", Icons.Default.Build),
            HudItem("V-Sens Touch Hook", Icons.Default.TouchApp)
        )
    }

    var currentItemIndex by remember { mutableIntStateOf(0) }
    var itemPhase by remember { mutableStateOf(SlotState.LOADING) }

    LaunchedEffect(Unit) {
        for (i in items.indices) {
            currentItemIndex = i
            itemPhase = SlotState.LOADING
            delay(150) // spinner time
            itemPhase = SlotState.DONE
            delay(150) // tick time
        }
        itemPhase = SlotState.EXITING
        delay(200) // slide out time
        onFinish()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        items.forEachIndexed { index, item ->
            val state = when {
                index > currentItemIndex -> SlotState.WAITING
                index == currentItemIndex -> itemPhase
                index == currentItemIndex - 1 && itemPhase != SlotState.EXITING -> SlotState.EXITING
                else -> SlotState.GONE
            }

            val offset by animateDpAsState(
                targetValue = when (state) {
                    SlotState.WAITING -> 64.dp
                    SlotState.LOADING, SlotState.DONE -> 0.dp
                    SlotState.EXITING -> (-64).dp
                    SlotState.GONE -> (-100).dp
                },
                animationSpec = tween(150, easing = LinearOutSlowInEasing), label = ""
            )
            
            val scale by animateFloatAsState(
                targetValue = when (state) {
                    SlotState.WAITING -> 0.9f
                    SlotState.LOADING, SlotState.DONE -> 1.0f
                    SlotState.EXITING -> 0.7f
                    SlotState.GONE -> 0.3f
                },
                animationSpec = tween(150, easing = LinearOutSlowInEasing), label = ""
            )
            
            val alpha by animateFloatAsState(
                targetValue = when (state) {
                    SlotState.WAITING -> 0.6f
                    SlotState.LOADING, SlotState.DONE -> 1.0f
                    SlotState.EXITING -> 0.4f
                    SlotState.GONE -> 0.0f
                },
                animationSpec = tween(150, easing = LinearOutSlowInEasing), label = ""
            )

            if (alpha > 0f) {
                HudSlot(
                    item = item,
                    state = state,
                    modifier = Modifier
                        .offset(y = offset)
                        .scale(scale)
                        .alpha(alpha)
                )
            }
        }
    }
}

@Composable
fun HudSlot(item: HudItem, state: SlotState, modifier: Modifier) {
    Surface(
        modifier = modifier
            .width(260.dp)
            .height(56.dp),
        color = Color(0xFF1E1E1E), // Minimalist system dark
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(item.icon, contentDescription = null, tint = Color(0xFFAAAAAA), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(item.label, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFEEEEEE))
            }

            when (state) {
                SlotState.WAITING -> { }
                SlotState.LOADING -> {
                    CircularProgressIndicator(
                        color = Color(0xFFEEEEEE), // Clean white spinner
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                }
                SlotState.DONE, SlotState.EXITING, SlotState.GONE -> {
                    Icon(
                        Icons.Default.Check, 
                        contentDescription = "Done", 
                        tint = Color(0xFF4CAF50), // Sharp green tick
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
