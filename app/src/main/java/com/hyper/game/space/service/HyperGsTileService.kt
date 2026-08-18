package com.hyper.game.space.service
import android.content.Intent

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.hyper.game.space.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HyperGsTileService : TileService() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var repository: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        repository = SettingsRepository(applicationContext)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return
        val isCurrentlyActive = tile.state == Tile.STATE_ACTIVE
        val newState = !isCurrentlyActive
        
        scope.launch {
            repository.saveBoolean(SettingsRepository.MASTER_TOGGLE, newState)
            updateTileUI(newState)
            if (!newState) {
                stopService(Intent(applicationContext, OverlayService::class.java))
            }
        }
    }

    private fun updateTileState() {
        scope.launch {
            val isActive = repository.getBoolean(SettingsRepository.MASTER_TOGGLE, false).first()
            updateTileUI(isActive)
        }
    }

    private fun updateTileUI(isActive: Boolean) {
        val tile = qsTile ?: return
        tile.state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = if (isActive) "Hyper GS: ACTIVE" else "Hyper GS: OFF"
        tile.updateTile()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
