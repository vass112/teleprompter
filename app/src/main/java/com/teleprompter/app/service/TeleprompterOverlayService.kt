package com.teleprompter.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.teleprompter.app.R
import com.teleprompter.app.data.db.ScriptEntity

class TeleprompterOverlayService : Service() {

    companion object {
        const val TAG = "TeleprompterSvc"
        const val CHANNEL_ID = "teleprompter_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "start"
        const val ACTION_STOP = "stop"

        private var isRunning = false

        fun isOverlayRunning(): Boolean = isRunning

        fun start(context: Context, script: ScriptEntity) {
            Log.d(TAG, "start() called")
            isRunning = true
            val intent = Intent(context, TeleprompterOverlayService::class.java).apply {
                action = ACTION_START
                putExtra("content", script.content)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                Log.d(TAG, "Service start intent sent")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start service", e)
                isRunning = false
            }
        }

        fun stop(context: Context) {
            isRunning = false
            try {
                context.startService(Intent(context, TeleprompterOverlayService::class.java).apply {
                    action = ACTION_STOP
                })
            } catch (_: Exception) {}
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand action=${intent?.action}")
        try {
            when (intent?.action) {
                ACTION_START -> {
                    val content = intent.getStringExtra("content") ?: ""
                    Log.d(TAG, "Starting overlay, content length=${content.length}")

                    val notification = createNotification()
                    startForeground(NOTIFICATION_ID, notification)
                    Log.d(TAG, "startForeground succeeded")

                    showOverlay(content)
                }
                ACTION_STOP -> {
                    cleanup()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
                else -> {
                    Log.w(TAG, "Unknown action: ${intent?.action}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onStartCommand", e)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun showOverlay(content: String) {
        try {
            Log.d(TAG, "showOverlay start")

            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val rootView = inflater.inflate(R.layout.overlay_teleprompter, null)

            Log.d(TAG, "Layout inflated")

            val scriptText = rootView.findViewById<TextView>(R.id.script_text)
            scriptText.text = content.ifEmpty { "Script content here..." }

            val scrollContainer = rootView.findViewById<ScrollView>(R.id.scroll_container)
            val playPauseBtn = rootView.findViewById<ImageButton>(R.id.btn_play_pause)
            val stopBtn = rootView.findViewById<ImageButton>(R.id.btn_stop)
            val minimizeBtn = rootView.findViewById<ImageButton>(R.id.btn_minimize)

            val wm = getSystemService(WINDOW_SERVICE) as? WindowManager ?: run {
                Log.e(TAG, "WindowManager not available"); stopSelf(); return
            }
            val metrics = resources.displayMetrics
            val width = metrics.widthPixels
            val height = (metrics.heightPixels * 0.6).toInt()

            Log.d(TAG, "Window size: ${width}x$height")

            overlayParams = WindowManager.LayoutParams(
                width,
                height,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 0
                y = (metrics.heightPixels * 0.08).toInt()
            }

            wm.addView(rootView, overlayParams)
            overlayView = rootView
            Log.d(TAG, "Overlay view added to window manager")
            this.windowManagerRef = wm

            playPauseBtn.setOnClickListener {
                isPlaying = !isPlaying
                playPauseBtn.setImageResource(
                    if (isPlaying) android.R.drawable.ic_media_pause
                    else android.R.drawable.ic_media_play
                )
                if (isPlaying) {
                    scrollHandler.post(scrollRunnable)
                } else {
                    scrollHandler.removeCallbacks(scrollRunnable)
                }
            }

            stopBtn.setOnClickListener {
                isPlaying = false
                scrollHandler.removeCallbacks(scrollRunnable)
                currentPosition = 0f
                scrollContainer.scrollTo(0, 0)
                playPauseBtn.setImageResource(android.R.drawable.ic_media_play)
            }

            minimizeBtn.setOnClickListener {
                cleanup()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }

            rootView.setOnTouchListener { _, event ->
                handleTouch(event)
            }

        } catch (e: Exception) {
            Log.e(TAG, "showOverlay failed", e)
            stopSelf()
        }
    }

    private var windowManagerRef: WindowManager? = null
    private var overlayView: android.view.View? = null
    private var overlayParams: WindowManager.LayoutParams? = null
    private var isPlaying = false
    private var currentPosition = 0f
    private val scrollSpeed = 1.0f
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private val scrollHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val scrollRunnable = object : Runnable {
        override fun run() {
            if (isPlaying) {
                try {
                    val view = overlayView ?: return
                    val sc = view.findViewById<ScrollView>(R.id.scroll_container) ?: return
                    val st = view.findViewById<TextView>(R.id.script_text) ?: return
                    val total = st.height - sc.height
                    if (total > 0) {
                        currentPosition += scrollSpeed * 2f
                        if (currentPosition >= total) {
                            currentPosition = total.toFloat()
                            isPlaying = false
                            scrollHandler.removeCallbacks(this)
                            view.findViewById<ImageButton>(R.id.btn_play_pause)
                                ?.setImageResource(android.R.drawable.ic_media_play)
                        }
                        sc.scrollTo(0, currentPosition.toInt())
                    }
                } catch (_: Exception) {}
                if (isPlaying) scrollHandler.postDelayed(this, 16L)
            }
        }
    }

    private fun handleTouch(event: MotionEvent): Boolean {
        val params = overlayParams ?: return false
        val view = overlayView ?: return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = params.x; initialY = params.y
                initialTouchX = event.rawX; initialTouchY = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                params.x = initialX + (event.rawX - initialTouchX).toInt()
                params.y = initialY + (event.rawY - initialTouchY).toInt()
                try { windowManagerRef?.updateViewLayout(view, params) } catch (_: Exception) {}
            }
        }
        return true
    }

    private fun cleanup() {
        isPlaying = false
        scrollHandler.removeCallbacks(scrollRunnable)
        isRunning = false
        try {
            overlayView?.let {
                if (it.isAttachedToWindow) windowManagerRef?.removeView(it)
            }
        } catch (_: Exception) {}
        overlayView = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    CHANNEL_ID, "Teleprompter", NotificationManager.IMPORTANCE_LOW
                ).apply { setShowBadge(false) }
                val manager = getSystemService(NotificationManager::class.java)
                manager?.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create channel", e)
            }
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Teleprompter")
            .setContentText("Overlay is active")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        cleanup()
        super.onDestroy()
    }
}
