package com.example.iosdock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
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

class DockOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null
    private lateinit var params: WindowManager.LayoutParams

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = store

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val prefs = getSharedPreferences("dock_prefs", Context.MODE_PRIVATE)

        val defaultYPx = dpToPx(35f)
        val savedY = prefs.getInt("dock_y_position", defaultYPx)
        val isLocked = prefs.getBoolean("dock_is_locked", true)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            dpToPx(100f),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            getFlags(isLocked),
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = savedY
        }

        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@DockOverlayService)
            setViewTreeSavedStateRegistryOwner(this@DockOverlayService)
            setViewTreeViewModelStoreOwner(this@DockOverlayService)
            setContent {
                IosStyleEmptyDock()
            }
            visibility = View.VISIBLE
        }

        setupTouchListener(prefs)

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        try {
            windowManager.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        ).toInt()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "شريط iOS Dock",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "إشعار استقرار شريط iOS Dock في الخلفية"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("iOS Dock يعمل الآن")
            .setContentText("الشريط الشفاف العائم نشط ومستقر")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun getFlags(isLocked: Boolean): Int {
        return if (isLocked) {
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        } else {
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        }
    }

    private fun setupTouchListener(prefs: android.content.SharedPreferences) {
        var initialY = 0
        var initialTouchY = 0f

        overlayView?.setOnTouchListener { _, event ->
            val isLocked = prefs.getBoolean("dock_is_locked", true)
            if (isLocked) return@setOnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialY = params.y
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = (event.rawY - initialTouchY).toInt()
                    params.y = initialY - deltaY
                    try {
                        windowManager.updateViewLayout(overlayView, params)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    prefs.edit().putInt("dock_y_position", params.y).apply()
                    true
                }
                else -> false
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = getSharedPreferences("dock_prefs", Context.MODE_PRIVATE)
        val isLocked = prefs.getBoolean("dock_is_locked", true)

        when (intent?.action) {
            DockAccessibilityService.ACTION_SHOW_DOCK -> {
                overlayView?.visibility = View.VISIBLE
            }
            DockAccessibilityService.ACTION_HIDE_DOCK -> {
                overlayView?.visibility = View.GONE
            }
            ACTION_UPDATE_LOCK_STATE -> {
                params.flags = getFlags(isLocked)
                overlayView?.visibility = View.VISIBLE
                overlayView?.setContent {
                    IosStyleEmptyDock()
                }
                try {
                    windowManager.updateViewLayout(overlayView, params)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            ACTION_COVER_SEARCH -> {
                val searchYPx = dpToPx(35f)
                params.y = searchYPx
                prefs.edit().putInt("dock_y_position", searchYPx).apply()
                overlayView?.visibility = View.VISIBLE
                try {
                    windowManager.updateViewLayout(overlayView, params)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            else -> {
                overlayView?.visibility = View.VISIBLE
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()

        if (overlayView != null) {
            try {
                windowManager.removeView(overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "ios_dock_foreground_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_UPDATE_LOCK_STATE = "com.example.iosdock.UPDATE_LOCK_STATE"
        const val ACTION_COVER_SEARCH = "com.example.iosdock.COVER_SEARCH"
    }
}
