package com.teleprompter.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TeleprompterApp : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}
