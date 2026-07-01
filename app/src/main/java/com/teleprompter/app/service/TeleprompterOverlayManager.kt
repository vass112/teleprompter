package com.teleprompter.app.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.teleprompter.app.data.db.ScriptEntity

class TeleprompterOverlayManager {

    companion object {
        private const val ACTION_OVERLAY_SETTINGS = "android.settings.action.MANAGE_OVERLAY_PERMISSION"

        fun isOverlayPermissionGranted(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        }

        fun openOverlaySettings(context: Context) {
            val intent = Intent(
                ACTION_OVERLAY_SETTINGS,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }

        fun isServiceRunning(): Boolean = TeleprompterOverlayService.isOverlayRunning()
    }

    fun startOverlay(context: Context, script: ScriptEntity) {
        if (!TeleprompterOverlayManager.isOverlayPermissionGranted(context)) {
            TeleprompterOverlayManager.openOverlaySettings(context)
            return
        }
        TeleprompterOverlayService.start(context, script)
    }

    fun startOverlay(script: ScriptEntity) {
        // This is a simplified version; in Compose, use the context-based version
    }

    fun stopOverlay(context: Context) {
        TeleprompterOverlayService.stop(context)
    }
}
