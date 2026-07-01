package com.teleprompter.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.teleprompter.app.R
import com.teleprompter.app.data.db.ScriptEntity

class TeleprompterOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var overlayParams: WindowManager.LayoutParams? = null

    private var isPlaying = false
    private var currentPosition = 0f
    private var scrollSpeed = 1.0f
    private var scriptContent = ""
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private val mainHandler = Handler(Looper.getMainLooper())
    private val scrollRunnable = object : Runnable {
        override fun run() {
            if (isPlaying) {
                scrollOverlay()
                mainHandler.postDelayed(this, 16L)
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "teleprompter_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "start_overlay"
        const val ACTION_STOP = "stop_overlay"
        const val EXTRA_SCRIPT_CONTENT = "script_content"

        private var isRunning = false

        fun isOverlayRunning(): Boolean = isRunning

        fun start(context: Context, script: ScriptEntity) {
            isRunning = true
            val intent = Intent(context, TeleprompterOverlayService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SCRIPT_CONTENT, script.content)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
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
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                scriptContent = intent.getStringExtra(EXTRA_SCRIPT_CONTENT) ?: ""
                try {
                    val notification = createNotification()
                    startForeground(NOTIFICATION_ID, notification)
                    showOverlay()
                } catch (e: Exception) {
                    stopSelf()
                }
            }
            ACTION_STOP -> {
                cleanup()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun showOverlay() {
        try {
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val rootView = inflater.inflate(R.layout.overlay_teleprompter, null) as FrameLayout

            val scriptText = rootView.findViewById<TextView>(R.id.script_text)
            scriptText.text = scriptContent.ifEmpty { "Your script will appear here..." }

            val playPauseBtn = rootView.findViewById<ImageButton>(R.id.btn_play_pause)
            val stopBtn = rootView.findViewById<ImageButton>(R.id.btn_stop)
            val minimizeBtn = rootView.findViewById<ImageButton>(R.id.btn_minimize)

            playPauseBtn.setOnClickListener { _ ->
                isPlaying = !isPlaying
                playPauseBtn.setImageResource(
                    if (isPlaying) android.R.drawable.ic_media_pause
                    else android.R.drawable.ic_media_play
                )
                if (isPlaying) {
                    mainHandler.post(scrollRunnable)
                } else {
                    mainHandler.removeCallbacks(scrollRunnable)
                }
            }

            stopBtn.setOnClickListener { _ ->
                isPlaying = false
                mainHandler.removeCallbacks(scrollRunnable)
                currentPosition = 0f
                rootView.findViewById<ScrollView>(R.id.scroll_container)?.scrollTo(0, 0)
                playPauseBtn.setImageResource(android.R.drawable.ic_media_play)
            }

            minimizeBtn.setOnClickListener { _ ->
                cleanup()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }

            rootView.setOnTouchListener { _, event ->
                handleTouch(event)
            }

            val display = windowManager?.defaultDisplay ?: run { stopSelf(); return }
            val metrics = resources.displayMetrics

            overlayParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                (metrics.heightPixels * 0.6).toInt(),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 0
                y = (metrics.heightPixels * 0.08).toInt()
            }

            windowManager?.addView(rootView, overlayParams)
            overlayView = rootView
        } catch (e: Exception) {
            stopSelf()
        }
    }

    private fun handleTouch(event: MotionEvent): Boolean {
        val params = overlayParams ?: return false
        val view = overlayView ?: return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = params.x
                initialY = params.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                params.x = initialX + (event.rawX - initialTouchX).toInt()
                params.y = initialY + (event.rawY - initialTouchY).toInt()
                try {
                    windowManager?.updateViewLayout(view, params)
                } catch (_: Exception) {}
            }
        }
        return true
    }

    private fun scrollOverlay() {
        try {
            val view = overlayView ?: return
            val scrollContainer = view.findViewById<ScrollView>(R.id.scroll_container) ?: return
            val scriptText = view.findViewById<TextView>(R.id.script_text) ?: return

            val totalScroll = scriptText.height - scrollContainer.height
            if (totalScroll <= 0) return

            currentPosition += scrollSpeed * 2f
            if (currentPosition >= totalScroll) {
                currentPosition = totalScroll.toFloat()
                isPlaying = false
                mainHandler.removeCallbacks(scrollRunnable)
                view.findViewById<ImageButton>(R.id.btn_play_pause)
                    ?.setImageResource(android.R.drawable.ic_media_play)
            }

            scrollContainer.scrollTo(0, currentPosition.toInt())
        } catch (_: Exception) {}
    }

    private fun cleanup() {
        isPlaying = false
        mainHandler.removeCallbacks(scrollRunnable)
        isRunning = false
        try {
            overlayView?.let { view ->
                if (view.isAttachedToWindow) {
                    windowManager?.removeView(view)
                }
            }
        } catch (_: Exception) {}
        overlayView = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Teleprompter",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Shows teleprompter overlay status"
                    setShowBadge(false)
                }
                val manager = getSystemService(NotificationManager::class.java)
                manager?.createNotificationChannel(channel)
            } catch (_: Exception) {}
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
        cleanup()
        super.onDestroy()
    }
}
