package com.hyper.game.space.viewmodel

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hyper.game.space.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppListViewModel : ViewModel() {
    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    private val _selectedAppPackages = MutableStateFlow<Set<String>>(emptySet())
    val selectedAppPackages: StateFlow<Set<String>> = _selectedAppPackages.asStateFlow()

    fun loadInstalledApps(context: Context) {
        viewModelScope.launch {
            val apps = withContext(Dispatchers.IO) {
                val packageManager = context.packageManager
                val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                val launchableIntent = android.content.Intent(android.content.Intent.ACTION_MAIN, null)
                launchableIntent.addCategory(android.content.Intent.CATEGORY_LAUNCHER)
                val launchablePackages = packageManager.queryIntentActivities(launchableIntent, 0).map { it.activityInfo.packageName }.toSet()
                
                packages.filter { it.packageName in launchablePackages && it.packageName != context.packageName }
                    .map {
                        AppInfo(
                            packageName = it.packageName,
                            name = packageManager.getApplicationLabel(it).toString(),
                            icon = packageManager.getApplicationIcon(it)
                        )
                    }
                    .sortedBy { it.name }
            }
            _installedApps.value = apps
        }
    }

    fun toggleAppSelection(packageName: String) {
        _selectedAppPackages.update { current ->
            if (current.contains(packageName)) {
                current - packageName
            } else {
                current + packageName
            }
        }
    }
}
