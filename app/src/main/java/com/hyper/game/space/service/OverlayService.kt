package com.hyper.game.space.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
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
import com.hyper.game.space.ui.CornerTrigger
import com.hyper.game.space.ui.OverlayModal

class OverlayService : Service() {
    private var windowManager: WindowManager? = null
    private var topLeftView: ComposeView? = null
    private var topRightView: ComposeView? = null
    private var modalView: ComposeView? = null
    private var lifecycleOwner: MyLifecycleOwner? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        lifecycleOwner = MyLifecycleOwner().apply { init() }
        
        antiHangEngine = com.hyper.game.space.utils.AntiHangEngine(this)
        antiHangEngine?.boostProcessPriority()
        
        setupCornerTriggers()
        startMemoryCheckLoop()
    }

    private var hudView: android.view.View? = null
    private var antiHangEngine: com.hyper.game.space.utils.AntiHangEngine? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var memoryCheckRunnable: Runnable? = null
    
    private fun startMemoryCheckLoop() {
        memoryCheckRunnable = object : Runnable {
            override fun run() {
                antiHangEngine?.optimizeMemoryIfCritical()
                handler.postDelayed(this, 30000) // Check every 30 seconds
            }
        }
        handler.post(memoryCheckRunnable!!)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.getBooleanExtra("SHOW_HUD", false) == true) {
            showHud()
        }
        return START_STICKY
    }

    private fun showHud() {
        if (hudView != null) return
        
        hudView = com.hyper.game.space.ui.ActivationHudView(this) { hideHud() }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_SECURE or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.CENTER }

        windowManager?.addView(hudView as android.view.View, params)
    }

    private fun hideHud() {
        hudView?.let {
            windowManager?.removeView(it)
            hudView = null
        }
    }

    private fun setupCornerTriggers() {
        topLeftView = createTriggerView(isLeft = true)
        topRightView = createTriggerView(isLeft = false)
        
        val size = (50 * resources.displayMetrics.density).toInt()
        
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE

        val paramsLeft = WindowManager.LayoutParams(size, size, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START }

        val paramsRight = WindowManager.LayoutParams(size, size, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.END }

        windowManager?.addView(topLeftView, paramsLeft)
        windowManager?.addView(topRightView, paramsRight)
    }

    private fun createTriggerView(isLeft: Boolean): ComposeView {
        return ComposeView(this).apply {
            setContent {
                CornerTrigger(isLeft = isLeft, onTriggered = { showModal(isLeft) })
            }
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
        }
    }

    private fun showModal(fromLeft: Boolean) {
        if (modalView != null) return
        
        modalView = ComposeView(this).apply {
            setContent {
                OverlayModal(fromLeft = fromLeft, onClose = { hideModal() })
            }
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_SECURE or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START }

        windowManager?.addView(modalView, params)
    }

    private fun hideModal() {
        modalView?.let {
            windowManager?.removeView(it)
            modalView = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        topLeftView?.let { windowManager?.removeView(it) }
        topRightView?.let { windowManager?.removeView(it) }
        hideModal()
        lifecycleOwner?.destroy()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    class MyLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
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
