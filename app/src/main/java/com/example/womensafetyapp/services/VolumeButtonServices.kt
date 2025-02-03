// VolumeButtonService.kt
package com.example.womensafetyapp.services

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.IBinder
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.womensafetyapp.MainActivity
import com.example.womensafetyapp.R
import com.example.womensafetyapp.recievers.VolumeButtonReceiver
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class VolumeButtonService : Service() {
    private var lastButtonPressTime: Long = 0
    private var consecutivePressCount = 0
    private val PRESS_TIMEOUT = 1000L
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String
    private lateinit var notificationManager: NotificationManager

    private lateinit var volumeButtonReceiver: VolumeButtonReceiver

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        firestore = FirebaseFirestore.getInstance()
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //for foreground service
        volumeButtonReceiver = VolumeButtonReceiver()
        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        registerReceiver(volumeButtonReceiver, filter)
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "VOLUME_BUTTON_PRESSED" -> {
                handleVolumeButtonPress()
            }
        }
        return START_STICKY
    }

    private fun handleVolumeButtonPress() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastButtonPressTime < PRESS_TIMEOUT) {
            consecutivePressCount++
            if (consecutivePressCount >= 3) { // Changed to 3 presses
                consecutivePressCount = 0
                if (checkPermissions()) {
                    getCurrentLocationAndSendSMS()
                }
            }
        } else {
            consecutivePressCount = 1
        }
        lastButtonPressTime = currentTime
    }

    private fun checkPermissions(): Boolean {
        val locationPermission = ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        val smsPermission = ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.SEND_SMS
        )
        return locationPermission == PackageManager.PERMISSION_GRANTED &&
                smsPermission == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocationAndSendSMS() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val locationLink = "https://maps.google.com/?q=${it.latitude},${it.longitude}"
                sendSmsToEmergencyContacts(locationLink)
            } ?: run {
                showToast("Unable to fetch location")
            }
        }.addOnFailureListener {
            showToast("Failed to get location")
        }
    }

    private fun sendSmsToEmergencyContacts(locationLink: String) {
        if (userId.isNotEmpty()) {
            firestore.collection("users")
                .document(userId)
                .collection("emergency_contacts")
                .document("shared_contacts")
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val contactsMap = document.get("contacts") as? List<HashMap<String, String>>
                        if (contactsMap.isNullOrEmpty()) {
                            showToast("No emergency contacts found")
                            return@addOnSuccessListener
                        }

                        var successfulSends = 0
                        contactsMap.forEach { contact ->
                            val phone = contact["contact"]
                            val name = contact["name"]
                            if (!phone.isNullOrEmpty()) {
                                sendSms(
                                    phone,
                                    "Hey $name! It's an Emergency! Track my location here: $locationLink"
                                ) { success ->
                                    if (success) {
                                        successfulSends++
                                        if (successfulSends == contactsMap.size) {
                                            showToast("Emergency alert sent to all contacts")
                                            showEmergencySmsNotification(contactsMap.size)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        showToast("No emergency contacts found")
                    }
                }
                .addOnFailureListener {
                    showToast("Failed to fetch contacts")
                }
        }
    }

    private fun sendSms(phone: String, message: String, onComplete: (Boolean) -> Unit) {
        try {
            val smsManager = SmsManager.getDefault()

            val sentIntent = Intent("SMS_SENT")
            val deliveredIntent = Intent("SMS_DELIVERED")

            val sentPI = PendingIntent.getBroadcast(
                this, 0, sentIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val deliveredPI = PendingIntent.getBroadcast(
                this, 0, deliveredIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    when (resultCode) {
                        Activity.RESULT_OK -> {
                            onComplete(true)
                        }
                        SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                            showToast("Generic failure")
                            onComplete(false)
                        }
                        SmsManager.RESULT_ERROR_NO_SERVICE -> {
                            showToast("No service")
                            onComplete(false)
                        }
                        SmsManager.RESULT_ERROR_NULL_PDU -> {
                            showToast("Null PDU")
                            onComplete(false)
                        }
                        SmsManager.RESULT_ERROR_RADIO_OFF -> {
                            showToast("Radio off")
                            onComplete(false)
                        }
                    }
                    try {
                        unregisterReceiver(this)
                    } catch (e: Exception) {
                        // Receiver already unregistered
                    }
                }
            }, IntentFilter("SMS_SENT"), Context.RECEIVER_NOT_EXPORTED)

            smsManager.sendTextMessage(phone, null, message, sentPI, deliveredPI)

        } catch (e: Exception) {
            showToast("Failed to send SMS: ${e.message}")
            onComplete(false)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun createNotification(): Notification {
        val channelId = "safety_service_channel"
        val channelName = "Safety Service"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Women Safety Active")
            .setContentText("Protection service is running")
            .setSmallIcon(R.drawable.ic_notifications_black_24dp) // Create this icon in your drawable folder
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun showEmergencySmsNotification(contactCount: Int) {
        val channelId = "emergency_notification_channel"
        val channelName = "Emergency Notifications"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 300, 400)
                enableLights(true)
                lightColor = android.graphics.Color.RED
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            1,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Emergency Alert Sent")
            .setContentText("Emergency SMS sent to $contactCount emergency contacts")
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your emergency location has been shared with $contactCount emergency contacts. Stay safe!")
            )
            .build()

        notificationManager.notify(EMERGENCY_NOTIFICATION_ID, notification)
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val EMERGENCY_NOTIFICATION_ID = 2
    }
    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(volumeButtonReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}