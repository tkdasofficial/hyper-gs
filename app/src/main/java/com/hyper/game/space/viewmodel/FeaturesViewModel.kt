package com.hyper.game.space.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.content.Context

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hyper.game.space.data.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random


class FeaturesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)
    
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FeaturesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FeaturesViewModel(context.applicationContext as Application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    // Telemetry State
    private val _cpuLoad = MutableStateFlow(42)
    val cpuLoad = _cpuLoad.asStateFlow()

    private val _gpuLoad = MutableStateFlow(85)
    val gpuLoad = _gpuLoad.asStateFlow()

    private val _ramUsage = MutableStateFlow(64)
    val ramUsage = _ramUsage.asStateFlow()

    private val _liveFps = MutableStateFlow(120)
    val liveFps = _liveFps.asStateFlow()

    // Calibration State
    private val _calibrationState = MutableStateFlow("Safe")
    val calibrationState = _calibrationState.asStateFlow()

    init {
        startTelemetrySimulation()
    }

    private fun startTelemetrySimulation() {
        viewModelScope.launch {
            while (true) {
                // Simulate realistic fluctuating metrics
                _cpuLoad.value = (_cpuLoad.value + Random.nextInt(-5, 6)).coerceIn(20, 95)
                _gpuLoad.value = (_gpuLoad.value + Random.nextInt(-8, 9)).coerceIn(40, 99)
                _ramUsage.value = (_ramUsage.value + Random.nextInt(-2, 3)).coerceIn(50, 90)
                _liveFps.value = if (Random.nextBoolean()) 120 else 119
                delay(1000)
            }
        }
    }

    fun triggerDigitizerCalibration() {
        if (_calibrationState.value == "Calibrating...") return
        viewModelScope.launch {
            _calibrationState.value = "Calibrating..."
            delay(3000)
            _calibrationState.value = "Calibrated & Safe"
            delay(2000)
            _calibrationState.value = "Safe"
        }
    }

    // State Flows for all settings
    val masterToggle = repository.getBoolean(SettingsRepository.MASTER_TOGGLE, false)
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val autoRamBoost = repository.getBoolean(SettingsRepository.AUTO_RAM_BOOST, true)
        .stateIn(viewModelScope, SharingStarted.Lazily, true)
    val deepKillEngine = repository.getBoolean(SettingsRepository.DEEP_KILL_ENGINE, true)
        .stateIn(viewModelScope, SharingStarted.Lazily, true)
    val fpsDropShield = repository.getBoolean(SettingsRepository.FPS_DROP_SHIELD, true)
        .stateIn(viewModelScope, SharingStarted.Lazily, true)
    val autoCrashRecovery = repository.getBoolean(SettingsRepository.AUTO_CRASH_RECOVERY, false)
        .stateIn(viewModelScope, SharingStarted.Lazily, false)
    val screenBrightnessLock = repository.getBoolean(SettingsRepository.SCREEN_BRIGHTNESS_LOCK, false)
        .stateIn(viewModelScope, SharingStarted.Lazily, false)
    val networkLatencySaver = repository.getBoolean(SettingsRepository.NETWORK_LATENCY_SAVER, true)
        .stateIn(viewModelScope, SharingStarted.Lazily, true)
    val dedicatedBandwidth = repository.getBoolean(SettingsRepository.DEDICATED_BANDWIDTH, true)
        .stateIn(viewModelScope, SharingStarted.Lazily, true)
    val bypassCharging = repository.getBoolean(SettingsRepository.BYPASS_CHARGING, true)
        .stateIn(viewModelScope, SharingStarted.Lazily, true)
    val overloadOptimizer = repository.getBoolean(SettingsRepository.OVERLOAD_OPTIMIZER, true)
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val vSensX = repository.getFloat(SettingsRepository.VSENS_X, 1.0f)
        .stateIn(viewModelScope, SharingStarted.Lazily, 1.0f)
    val vSensY = repository.getFloat(SettingsRepository.VSENS_Y, 1.0f)
        .stateIn(viewModelScope, SharingStarted.Lazily, 1.0f)
    val vSensZ = repository.getFloat(SettingsRepository.VSENS_Z, 1.0f)
        .stateIn(viewModelScope, SharingStarted.Lazily, 1.0f)

    val hwSync = repository.getBoolean(SettingsRepository.HW_SYNC, true)
        .stateIn(viewModelScope, SharingStarted.Lazily, true)
    val refreshLock = repository.getBoolean(SettingsRepository.REFRESH_LOCK, true)
        .stateIn(viewModelScope, SharingStarted.Lazily, true)
    val showLiveFps = repository.getBoolean(SettingsRepository.LIVE_FPS, false)
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val dndThirdParty = repository.getBoolean(SettingsRepository.DND_THIRD_PARTY, true)
        .stateIn(viewModelScope, SharingStarted.Lazily, true)
    val dndCalls = repository.getBoolean(SettingsRepository.DND_CALLS, true)
        .stateIn(viewModelScope, SharingStarted.Lazily, true)
    val dndSms = repository.getBoolean(SettingsRepository.DND_SMS, true)
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val thermalGovernor = repository.getBoolean(SettingsRepository.THERMAL_GOVERNOR, true)
        .stateIn(viewModelScope, SharingStarted.Lazily, true)
    val thermalProfile = repository.getString(SettingsRepository.THERMAL_PROFILE, "Smart Cooling")
        .stateIn(viewModelScope, SharingStarted.Lazily, "Smart Cooling")

    val forceOptimize = repository.getBoolean(SettingsRepository.FORCE_OPTIMIZE, true)
        .stateIn(viewModelScope, SharingStarted.Lazily, true)
    val renderer = repository.getString(SettingsRepository.RENDERER, "Vulkan")
        .stateIn(viewModelScope, SharingStarted.Lazily, "Vulkan")

    val touchLatency = repository.getBoolean(SettingsRepository.TOUCH_LATENCY, true)
        .stateIn(viewModelScope, SharingStarted.Lazily, true)
    val pollingAccel = repository.getBoolean(SettingsRepository.POLLING_ACCEL, true)
        .stateIn(viewModelScope, SharingStarted.Lazily, true)
    val dragOptimize = repository.getBoolean(SettingsRepository.DRAG_OPTIMIZE, true)
        .stateIn(viewModelScope, SharingStarted.Lazily, true)
    val sensitivityProfile = repository.getString(SettingsRepository.SENSITIVITY_PROFILE, "Ultra")
        .stateIn(viewModelScope, SharingStarted.Lazily, "Ultra")

    val spatialAudio = repository.getBoolean(SettingsRepository.SPATIAL_AUDIO, true)
        .stateIn(viewModelScope, SharingStarted.Lazily, true)
    val audioMode = repository.getString(SettingsRepository.AUDIO_MODE, "Footstep Enhancer")
        .stateIn(viewModelScope, SharingStarted.Lazily, "Footstep Enhancer")

    val resolutionProfile = repository.getString(SettingsRepository.RESOLUTION_PROFILE, "100% Native")
        .stateIn(viewModelScope, SharingStarted.Lazily, "100% Native")

    val ghostFilter = repository.getBoolean(SettingsRepository.GHOST_FILTER, true)
        .stateIn(viewModelScope, SharingStarted.Lazily, true)
    val autoTouchHeat = repository.getBoolean(SettingsRepository.AUTO_TOUCH_HEAT, true)
        .stateIn(viewModelScope, SharingStarted.Lazily, true)
        
    // Recorder States
    val recorderEnabled = repository.getBoolean(SettingsRepository.RECORDER_ENABLED, true)
        .stateIn(viewModelScope, SharingStarted.Lazily, true)
    val recorderResolution = repository.getString(SettingsRepository.RECORDER_RESOLUTION, "1080p FHD")
        .stateIn(viewModelScope, SharingStarted.Lazily, "1080p FHD")
    val recorderFps = repository.getInt(SettingsRepository.RECORDER_FPS, 60)
        .stateIn(viewModelScope, SharingStarted.Lazily, 60)
    val recorderBitrate = repository.getInt(SettingsRepository.RECORDER_BITRATE, 16)
        .stateIn(viewModelScope, SharingStarted.Lazily, 16)
    val recorderAudioSource = repository.getString(SettingsRepository.RECORDER_AUDIO_SOURCE, "Dual-Audio")
        .stateIn(viewModelScope, SharingStarted.Lazily, "Dual-Audio")
    val recorderOrientation = repository.getString(SettingsRepository.RECORDER_ORIENTATION, "Auto-Detect")
        .stateIn(viewModelScope, SharingStarted.Lazily, "Auto-Detect")

    // Update Functions
    fun setBoolean(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, value: Boolean) {
        viewModelScope.launch { repository.saveBoolean(key, value) }
    }
    fun setFloat(key: androidx.datastore.preferences.core.Preferences.Key<Float>, value: Float) {
        viewModelScope.launch { repository.saveFloat(key, value) }
    }
    fun setInt(key: androidx.datastore.preferences.core.Preferences.Key<Int>, value: Int) {
        viewModelScope.launch { repository.saveInt(key, value) }
    }
    fun setString(key: androidx.datastore.preferences.core.Preferences.Key<String>, value: String) {
        viewModelScope.launch { repository.saveString(key, value) }
    }
}
