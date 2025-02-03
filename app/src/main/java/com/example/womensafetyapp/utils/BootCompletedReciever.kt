// BootCompletedReceiver.kt
package com.example.womensafetyapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.womensafetyapp.services.VolumeButtonService

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Check if service should be started (based on user preference)
            val prefs = context?.getSharedPreferences("safety_prefs", Context.MODE_PRIVATE)
            val shouldStartService = prefs?.getBoolean("background_service_enabled", false) ?: false

            if (shouldStartService) {
                val serviceIntent = Intent(context, VolumeButtonService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context?.startForegroundService(serviceIntent)
                } else {
                    context?.startService(serviceIntent)
                }
            }
        }
    }
}