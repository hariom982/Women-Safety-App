package com.example.womensafetyapp.recievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import com.example.womensafetyapp.services.VolumeButtonService

class VolumeButtonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_MEDIA_BUTTON) {
            val event = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
            if (event?.action == KeyEvent.ACTION_DOWN) {
                when (event.keyCode) {
                    KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                        val serviceIntent = Intent(context, VolumeButtonService::class.java).apply {
                            action = "VOLUME_BUTTON_PRESSED"
                        }
                        context?.startService(serviceIntent)
                    }
                }
            }
        }
    }
}