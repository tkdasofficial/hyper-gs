package com.hyper.game.space.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hyper_gs_settings")

class SettingsRepository(private val context: Context) {
    
    companion object {
        val MASTER_TOGGLE = booleanPreferencesKey("master_toggle")
        val AUTO_RAM_BOOST = booleanPreferencesKey("auto_ram_boost")
        val DEEP_KILL_ENGINE = booleanPreferencesKey("deep_kill_engine")
        val FPS_DROP_SHIELD = booleanPreferencesKey("fps_drop_shield")
        val AUTO_CRASH_RECOVERY = booleanPreferencesKey("auto_crash_recovery")
        val SCREEN_BRIGHTNESS_LOCK = booleanPreferencesKey("screen_brightness_lock")
        val NETWORK_LATENCY_SAVER = booleanPreferencesKey("network_latency_saver")
        val DEDICATED_BANDWIDTH = booleanPreferencesKey("dedicated_bandwidth")
        val BYPASS_CHARGING = booleanPreferencesKey("bypass_charging")
        val OVERLOAD_OPTIMIZER = booleanPreferencesKey("overload_optimizer")
        
        val VSENS_X = floatPreferencesKey("vsens_x")
        val VSENS_Y = floatPreferencesKey("vsens_y")
        val VSENS_Z = floatPreferencesKey("vsens_z")
        
        val HW_SYNC = booleanPreferencesKey("hw_sync")
        val REFRESH_LOCK = booleanPreferencesKey("refresh_lock")
        val LIVE_FPS = booleanPreferencesKey("live_fps")
        
        val DND_THIRD_PARTY = booleanPreferencesKey("dnd_third_party")
        val DND_CALLS = booleanPreferencesKey("dnd_calls")
        val DND_SMS = booleanPreferencesKey("dnd_sms")
        
        val THERMAL_GOVERNOR = booleanPreferencesKey("thermal_governor")
        val THERMAL_PROFILE = stringPreferencesKey("thermal_profile")
        
        val FORCE_OPTIMIZE = booleanPreferencesKey("force_optimize")
        val RENDERER = stringPreferencesKey("renderer")
        
        val TOUCH_LATENCY = booleanPreferencesKey("touch_latency")
        val POLLING_ACCEL = booleanPreferencesKey("polling_accel")
        val DRAG_OPTIMIZE = booleanPreferencesKey("drag_optimize")
        val SENSITIVITY_PROFILE = stringPreferencesKey("sensitivity_profile")
        
        val SPATIAL_AUDIO = booleanPreferencesKey("spatial_audio")
        val AUDIO_MODE = stringPreferencesKey("audio_mode")
        
        val RESOLUTION_PROFILE = stringPreferencesKey("resolution_profile")

        val GHOST_FILTER = booleanPreferencesKey("ghost_filter")
        val AUTO_TOUCH_HEAT = booleanPreferencesKey("auto_touch_heat")
        
        // Recorder Keys
        val RECORDER_ENABLED = booleanPreferencesKey("recorder_enabled")
        val RECORDER_RESOLUTION = stringPreferencesKey("recorder_resolution")
        val RECORDER_FPS = intPreferencesKey("recorder_fps")
        val RECORDER_BITRATE = intPreferencesKey("recorder_bitrate")
        val RECORDER_AUDIO_SOURCE = stringPreferencesKey("recorder_audio_source")
        val RECORDER_ORIENTATION = stringPreferencesKey("recorder_orientation")
        
        val ENABLED_GAMES = stringSetPreferencesKey("enabled_games")
    }

    suspend fun saveStringSet(key: Preferences.Key<Set<String>>, value: Set<String>) {
        context.dataStore.edit { prefs -> prefs[key] = value }
    }

    fun getStringSet(key: Preferences.Key<Set<String>>, default: Set<String>): Flow<Set<String>> =
        context.dataStore.data.map { prefs -> prefs[key] ?: default }

    suspend fun saveBoolean(key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { prefs -> prefs[key] = value }
    }

    suspend fun saveFloat(key: Preferences.Key<Float>, value: Float) {
        context.dataStore.edit { prefs -> prefs[key] = value }
    }
    
    suspend fun saveInt(key: Preferences.Key<Int>, value: Int) {
        context.dataStore.edit { prefs -> prefs[key] = value }
    }

    suspend fun saveString(key: Preferences.Key<String>, value: String) {
        context.dataStore.edit { prefs -> prefs[key] = value }
    }

    fun getBoolean(key: Preferences.Key<Boolean>, default: Boolean): Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[key] ?: default }

    fun getFloat(key: Preferences.Key<Float>, default: Float): Flow<Float> =
        context.dataStore.data.map { prefs -> prefs[key] ?: default }
        
    fun getInt(key: Preferences.Key<Int>, default: Int): Flow<Int> =
        context.dataStore.data.map { prefs -> prefs[key] ?: default }

    fun getString(key: Preferences.Key<String>, default: String): Flow<String> =
        context.dataStore.data.map { prefs -> prefs[key] ?: default }
}
