package com.hyper.game.space.service
import android.app.Activity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import android.media.AudioPlaybackCaptureConfiguration
import com.hyper.game.space.data.SettingsRepository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.hyper.game.space.ui.FloatingRecordWidget
import com.hyper.game.space.utils.HardwareEncoderUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

import android.media.audiofx.NoiseSuppressor
import android.media.audiofx.AcousticEchoCanceler

class ScreenRecordService : Service() {
    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE"
        const val EXTRA_RESULT_DATA = "EXTRA_RESULT_DATA"
    }

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var echoCanceler: AcousticEchoCanceler? = null
    private var isRecording = false
    private var isPaused = false

    private var windowManager: WindowManager? = null
    private var floatingView: ComposeView? = null
    private var lifecycleOwner: ServiceLifecycleOwner? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        lifecycleOwner = ServiceLifecycleOwner().apply { init() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
                val data: Intent? = intent.getParcelableExtra(EXTRA_RESULT_DATA)
                if (resultCode == Activity.RESULT_OK && data != null) {
                    startRecording(resultCode, data)
                }
            }
            ACTION_STOP -> stopRecording()
            ACTION_PAUSE -> pauseRecording()
            ACTION_RESUME -> resumeRecording()
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "record_channel",
                "Screen Recording",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun startRecording(resultCode: Int, data: Intent) {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "record_channel")
            .setContentTitle("Hyper GS Recording")
            .setContentText("Hardware-accelerated recording active")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()
        startForeground(101, notification)

        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)

        setupMediaRecorder()
        showFloatingWidget()

        val profile = HardwareEncoderUtils.getBestSupportedProfile()
        val metrics = resources.displayMetrics
        val density = metrics.densityDpi

        mediaRecorder?.start()
        isRecording = true

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "HyperGSRecord",
            profile.width, profile.height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mediaRecorder?.surface, null, null
        )
    }

    private fun setupMediaRecorder() {
        val repository = SettingsRepository(this)
        var resolutionProfileStr = "1080p FHD"
        var fps = 60
        var bitrate = 16
        var audioSourceStr = "Dual-Audio"

        runBlocking {
            resolutionProfileStr = repository.getString(SettingsRepository.RECORDER_RESOLUTION, "1080p FHD").first()
            fps = repository.getInt(SettingsRepository.RECORDER_FPS, 60).first()
            bitrate = repository.getInt(SettingsRepository.RECORDER_BITRATE, 16).first()
            audioSourceStr = repository.getString(SettingsRepository.RECORDER_AUDIO_SOURCE, "Dual-Audio").first()
        }

        // Determine resolution
        val profile = HardwareEncoderUtils.getProfileFromString(resolutionProfileStr, fps)
        
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            "HyperGS_${dateFormat.format(Date())}.mp4"
        )

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }
        
        val audioSource = when (audioSourceStr) {
            "System Audio" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaRecorder.AudioSource.REMOTE_SUBMIX else MediaRecorder.AudioSource.MIC
            "Mic Only" -> MediaRecorder.AudioSource.MIC
            "Dual-Audio" -> MediaRecorder.AudioSource.MIC // Mix via MIC (since MediaRecorder can't easily dual-stream, rely on OS mixing or mic loopback)
            "Mute" -> -1
            else -> MediaRecorder.AudioSource.MIC
        }
        
        try {
            mediaRecorder?.apply {
                if (audioSource != -1) {
                    setAudioSource(audioSource)
                }
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(file.absolutePath)
                setVideoSize(profile.width, profile.height)
                setVideoEncoder(MediaRecorder.VideoEncoder.HEVC) // Try HEVC first (hardware)
                if (audioSource != -1) {
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                }
                setVideoEncodingBitRate(bitrate * 1000 * 1000) // Convert Mbps to bps
                setVideoFrameRate(profile.fps)
                prepare()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback
            mediaRecorder?.reset()
            mediaRecorder?.apply {
                if (audioSource != -1) {
                    setAudioSource(audioSource)
                }
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(file.absolutePath)
                setVideoSize(profile.width, profile.height)
                setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT) // Fallback default
                if (audioSource != -1) {
                    setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
                }
                prepare()
            }
        }
    }

    private fun showFloatingWidget() {
        if (floatingView != null) return

        floatingView = ComposeView(this).apply {
            setContent {
                FloatingRecordWidget(
                    isPaused = isPaused,
                    onPauseResume = {
                        if (isPaused) resumeRecording() else pauseRecording()
                    },
                    onStop = { stopRecording() }
                )
            }
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_SECURE, // IMPORTANT: Exclude from capture
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        windowManager?.addView(floatingView, params)
    }

    private fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isRecording && !isPaused) {
            mediaRecorder?.pause()
            isPaused = true
            // Re-render view
        }
    }

    private fun resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isRecording && isPaused) {
            mediaRecorder?.resume()
            isPaused = false
            // Re-render view
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            try {
                mediaRecorder?.stop()
            } catch (e: Exception) {
                e.printStackTrace() // Handle stop failure
            }
            mediaRecorder?.reset()
            mediaRecorder?.release()
            mediaRecorder = null
            virtualDisplay?.release()
            virtualDisplay = null
            mediaProjection?.stop()
            mediaProjection = null
            isRecording = false
        }
        removeFloatingWidget()
        stopForeground(true)
        stopSelf()
    }

    private fun removeFloatingWidget() {
        floatingView?.let {
            windowManager?.removeView(it)
            floatingView = null
        }
    }

    override fun onDestroy() {
        stopRecording()
        lifecycleOwner?.destroy()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    class ServiceLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        private val savedStateRegistryController = SavedStateRegistryController.create(this)
        private val store = ViewModelStore()
        override val lifecycle: Lifecycle get() = lifecycleRegistry
        override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
        override val viewModelStore: ViewModelStore get() = store
        fun init() {
            savedStateRegistryController.performRestore(null)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
        fun destroy() {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            store.clear()
        }
    }
}
