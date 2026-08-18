package com.hyper.game.space

import com.hyper.game.space.data.SettingsRepository

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hyper.game.space.ui.AppsScreen
import com.hyper.game.space.ui.FeaturesScreen
import com.hyper.game.space.ui.PermissionsScreen
import com.hyper.game.space.ui.SystemMonitorCard
import com.hyper.game.space.ui.theme.MyApplicationTheme
import com.hyper.game.space.viewmodel.AppListViewModel
import com.hyper.game.space.viewmodel.FeaturesViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        HyperGameSpaceApp()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HyperGameSpaceApp() {
    val navController = rememberNavController()
    val viewModel: AppListViewModel = viewModel()
    val featuresViewModel: FeaturesViewModel = viewModel()
    val context = LocalContext.current
    val masterToggle by featuresViewModel.masterToggle.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadInstalledApps(context)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("Hyper GS") },
                    actions = {
                        IconButton(onClick = { navController.navigate("permissions") }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings & Permissions")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Only show bottom bar on main tabs
                if (currentRoute == "apps" || currentRoute == "features") {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Apps, contentDescription = "Apps") },
                            label = { Text("Apps") },
                            selected = currentRoute == "apps",
                            onClick = {
                                if (currentRoute != "apps") {
                                    navController.navigate("apps") {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Build, contentDescription = "Features") },
                            label = { Text("Features") },
                            selected = currentRoute == "features",
                            onClick = {
                                if (currentRoute != "features") {
                                    navController.navigate("features") {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "apps",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("apps") {
                    AppsScreen(viewModel = viewModel, featuresViewModel = featuresViewModel)
                }
                composable("features") {
                    FeaturesScreen(viewModel = featuresViewModel)
                }
                composable("permissions") {
                    PermissionsScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
