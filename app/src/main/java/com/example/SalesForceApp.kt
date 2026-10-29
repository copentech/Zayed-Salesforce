package com.example

import android.app.Application
import com.example.util.NotificationHelper

class SalesForceApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
