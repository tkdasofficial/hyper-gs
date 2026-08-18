package com.hyper.game.space.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import android.app.StatusBarManager
import android.content.ComponentName
import android.graphics.drawable.Icon
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.hyper.game.space.R
import com.hyper.game.space.service.HyperGsTileService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Permissions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Security, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "HYPER GS requires advanced system-level permissions to hook into background processes, manage hardware overlays, and intercept memory events.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            PermissionItem(
                title = "Quick Settings Tile",
                description = "Add the Hyper GS toggle to your notification panel for instant access.",
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val statusBarManager = context.getSystemService(StatusBarManager::class.java)
                        statusBarManager?.requestAddTileService(
                            ComponentName(context, HyperGsTileService::class.java),
                            "Hyper GS",
                            Icon.createWithResource(context, R.drawable.ic_qs_tile),
                            context.mainExecutor
                        ) { result ->
                            // Result is handled by the system dialog
                        }
                    } else {
                        Toast.makeText(context, "Please pull down your notification shade, tap Edit, and drag the Hyper GS tile to your active tiles.", Toast.LENGTH_LONG).show()
                    }
                }
            )

            PermissionItem(
                title = "Draw Over Other Apps",
                description = "Required for Quick Edge In-Game Overlay.",
                onClick = {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                }
            )

            PermissionItem(
                title = "Write System Settings",
                description = "Required for Display Refresh Rate & Brightness Locks.",
                onClick = {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                }
            )

            PermissionItem(
                title = "Usage Access Stats",
                description = "Required to detect active gaming process state.",
                onClick = {
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    context.startActivity(intent)
                }
            )

            PermissionItem(
                title = "Do Not Disturb Access",
                description = "Required to silence Calls & SMS via the DND Engine.",
                onClick = {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                    context.startActivity(intent)
                }
            )
            
            // Note: KILL_BACKGROUND_PROCESSES is a normal permission declared in Manifest, no runtime prompt needed,
            // but we add it to the UI for user awareness of the feature requirement.
            PermissionItem(
                title = "Process Termination",
                description = "Deep Kill engine uses KILL_BACKGROUND_PROCESSES (Granted at Install).",
                onClick = { }
            )
        }
    }
}

@Composable
fun PermissionItem(title: String, description: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))
            Button(onClick = onClick) {
                Text("Grant / Manage")
            }
        }
    }
}
