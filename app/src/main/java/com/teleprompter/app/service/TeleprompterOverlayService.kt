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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TeleprompterOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: FrameLayout? = null
    private var overlayParams: WindowManager.LayoutParams? = null

    private var isPlaying = false
    private var isMinimized = false
    private var isLocked = false
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
                mainHandler.postDelayed(this, 16)
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "teleprompter_overlay_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.teleprompter.app.START_OVERLAY"
        const val ACTION_STOP = "com.teleprompter.app.STOP_OVERLAY"
        const val EXTRA_SCRIPT_ID = "extra_script_id"
        const val EXTRA_SCRIPT_TITLE = "extra_script_title"
        const val EXTRA_SCRIPT_CONTENT = "extra_script_content"

        private var currentScript: ScriptEntity? = null
        private var isRunning = false

        fun isOverlayRunning(): Boolean = isRunning
        fun getCurrentScript(): ScriptEntity? = currentScript

        fun start(context: Context, script: ScriptEntity) {
            currentScript = script
            isRunning = true
            val intent = Intent(context, TeleprompterOverlayService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SCRIPT_ID, script.id)
                putExtra(EXTRA_SCRIPT_TITLE, script.title)
                putExtra(EXTRA_SCRIPT_CONTENT, script.content)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            isRunning = false
            currentScript = null
            val intent = Intent(context, TeleprompterOverlayService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
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
                val title = intent.getStringExtra(EXTRA_SCRIPT_TITLE) ?: "Script"
                val content = intent.getStringExtra(EXTRA_SCRIPT_CONTENT) ?: ""
                scriptContent = content
                try {
                    startForeground(NOTIFICATION_ID, createNotification(title))
                    showOverlay(title, content)
                } catch (e: Exception) {
                    e.printStackTrace()
                    stopSelf()
                }
            }
            ACTION_STOP -> {
                stopOverlay()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun showOverlay(title: String, content: String) {
        if (overlayView?.isAttachedToWindow == true) return

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView = inflater.inflate(R.layout.overlay_teleprompter, null) as FrameLayout

        val scriptText = rootView.findViewById<TextView>(R.id.script_text)
        scriptText.text = content.ifEmpty { "Your script will appear here..." }

        val scrollContainer = rootView.findViewById<ScrollView>(R.id.scroll_container)

        val playPauseBtn = rootView.findViewById<ImageButton>(R.id.btn_play_pause)
        val stopBtn = rootView.findViewById<ImageButton>(R.id.btn_stop)
        val minimizeBtn = rootView.findViewById<ImageButton>(R.id.btn_minimize)

        playPauseBtn.setOnClickListener {
            togglePlayPause()
            updatePlayPauseButton(playPauseBtn)
        }

        stopBtn.setOnClickListener {
            isPlaying = false
            mainHandler.removeCallbacks(scrollRunnable)
            scrollContainer.scrollTo(0, 0)
            currentPosition = 0f
            updatePlayPauseButton(playPauseBtn)
        }

        minimizeBtn.setOnClickListener {
            stopOverlay()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        rootView.setOnTouchListener { _, event ->
            if (isLocked) return@setOnTouchListener true
            handleTouch(event)
        }

        val flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH

        overlayParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            (resources.displayMetrics.heightPixels * 0.6).toInt(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = (resources.displayMetrics.heightPixels * 0.08).toInt()
        }

        try {
            windowManager.addView(rootView, overlayParams)
            overlayView = rootView
        } catch (e: SecurityException) {
            e.printStackTrace()
            stopSelf()
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun togglePlayPause() {
        isPlaying = !isPlaying
        if (isPlaying) {
            mainHandler.post(scrollRunnable)
        } else {
            mainHandler.removeCallbacks(scrollRunnable)
        }
    }

    private fun updatePlayPauseButton(button: ImageButton) {
        button.setImageResource(
            if (isPlaying) android.R.drawable.ic_media_pause
            else android.R.drawable.ic_media_play
        )
    }

    private fun scrollOverlay() {
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
            view.findViewById<ImageButton>(R.id.btn_play_pause)?.let {
                updatePlayPauseButton(it)
            }
        }

        scrollContainer.scrollTo(0, currentPosition.toInt())
    }

    private fun handleTouch(event: MotionEvent): Boolean {
        val params = overlayParams ?: return false

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
                    windowManager.updateViewLayout(overlayView, params)
                } catch (_: Exception) {}
            }
        }
        return true
    }

    private fun stopOverlay() {
        mainHandler.removeCallbacks(scrollRunnable)
        isPlaying = false
        try {
            overlayView?.let { view ->
                if (view.isAttachedToWindow) {
                    windowManager.removeView(view)
                }
            }
        } catch (_: Exception) {}
        overlayView = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Teleprompter Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notification for teleprompter floating overlay"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(title: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Teleprompter")
            .setContentText("Overlay active - $title")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        stopOverlay()
        isRunning = false
        super.onDestroy()
    }
}
