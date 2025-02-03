package com.example.womensafetyapp.ui

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.telephony.SmsManager.RESULT_ERROR_GENERIC_FAILURE
import android.telephony.SmsManager.RESULT_ERROR_NO_SERVICE
import android.telephony.SmsManager.RESULT_ERROR_NULL_PDU
import android.telephony.SmsManager.RESULT_ERROR_RADIO_OFF
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.womensafetyapp.R
import com.example.womensafetyapp.services.VolumeButtonService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PanicModeFragment : Fragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var firestore: FirebaseFirestore
    private lateinit var toggleLocationSwitch: Switch
    private lateinit var userId: String
    private var lastButtonPressTime: Long = 0
    private var consecutivePressCount = 0
    private val PRESS_TIMEOUT = 1000L
    private var isServiceRunning = false

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_panicmode, container, false)

        // Initialize Firestore, FusedLocationProviderClient, and user ID
        firestore = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        toggleLocationSwitch = view.findViewById(R.id.switch_location_share)
        toggleLocationSwitch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                if (checkPermissions()) {
                    getCurrentLocationAndSendSMS()
                } else {
                    requestPermissions()
                }
            }
        }
        // Set up key event handling in activity
        requireActivity().window.callback = KeyEventHandler(requireActivity().window.callback)

        // Add a switch for background service
//        val backgroundServiceSwitch = view.findViewById<Switch>(R.id.switch_location_share)
//        backgroundServiceSwitch.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                startBackgroundService()
//            } else {
//                stopBackgroundService()
//            }
//        }
        return view
    }

    private fun checkPermissions(): Boolean {
        val locationPermission = ContextCompat.checkSelfPermission(
            requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        val smsPermission = ContextCompat.checkSelfPermission(
            requireContext(), android.Manifest.permission.SEND_SMS
        )
        return locationPermission == PackageManager.PERMISSION_GRANTED && smsPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.SEND_SMS
            ),
            101
        )
    }

    private fun startBackgroundService() {
        if (!isServiceRunning) {
            val prefs = requireContext().getSharedPreferences("safety_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("background_service_enabled", true).apply()

            val serviceIntent = Intent(requireContext(), VolumeButtonService::class.java)
            ContextCompat.startForegroundService(requireContext(), serviceIntent)
            isServiceRunning = true
            Toast.makeText(requireContext(), "Background protection enabled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopBackgroundService() {
        if (isServiceRunning) {
            val prefs = requireContext().getSharedPreferences("safety_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("background_service_enabled", false).apply()

            val serviceIntent = Intent(requireContext(), VolumeButtonService::class.java)
            requireContext().stopService(serviceIntent)
            isServiceRunning = false
            Toast.makeText(requireContext(), "Background protection disabled", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocationAndSendSMS() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val locationLink = "https://maps.google.com/?q=${it.latitude},${it.longitude}"
                sendSmsToEmergencyContacts(locationLink)
            } ?: run {
                Toast.makeText(requireContext(), "Unable to fetch location", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(requireContext(), "No emergency contacts found", Toast.LENGTH_SHORT).show()
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
                                            Toast.makeText(
                                                requireContext(),
                                                "Emergency alert sent to all contacts",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "No emergency contacts found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to fetch contacts", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun sendSms(phone: String, message: String, onComplete: (Boolean) -> Unit) {
        try {
            val smsManager = SmsManager.getDefault()

            // Create pending intents for delivery status
            val sentIntent = Intent("SMS_SENT")
            val deliveredIntent = Intent("SMS_DELIVERED")

            val sentPI = PendingIntent.getBroadcast(
                requireContext(), 0, sentIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val deliveredPI = PendingIntent.getBroadcast(
                requireContext(), 0, deliveredIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Register broadcast receivers for tracking SMS status
            requireContext().registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    when (resultCode) {
                        android.app.Activity.RESULT_OK -> {
                            onComplete(true)
                        }
                        RESULT_ERROR_GENERIC_FAILURE -> {
                            Toast.makeText(context, "Generic failure", Toast.LENGTH_SHORT).show()
                            onComplete(false)
                        }
                        RESULT_ERROR_NO_SERVICE -> {
                            Toast.makeText(context, "No service", Toast.LENGTH_SHORT).show()
                            onComplete(false)
                        }
                        RESULT_ERROR_NULL_PDU -> {
                            Toast.makeText(context, "Null PDU", Toast.LENGTH_SHORT).show()
                            onComplete(false)
                        }
                        RESULT_ERROR_RADIO_OFF -> {
                            Toast.makeText(context, "Radio off", Toast.LENGTH_SHORT).show()
                            onComplete(false)
                        }
                    }
                    context?.unregisterReceiver(this)
                }
            }, IntentFilter("SMS_SENT"), Context.RECEIVER_NOT_EXPORTED)

            // Send the SMS with delivery tracking
            smsManager.sendTextMessage(phone, null, message, sentPI, deliveredPI)

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to send SMS: ${e.message}", Toast.LENGTH_SHORT).show()
            onComplete(false)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            getCurrentLocationAndSendSMS()
        } else {
            Toast.makeText(requireContext(), "Permissions are required to send location", Toast.LENGTH_SHORT).show()
        }
    }

    private inner class KeyEventHandler(val originalCallback: android.view.Window.Callback) :
        android.view.Window.Callback by originalCallback {

        override fun dispatchKeyEvent(event: KeyEvent): Boolean {
            if ((event.keyCode == KeyEvent.KEYCODE_VOLUME_UP || event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) &&
                event.action == KeyEvent.ACTION_DOWN
            ) {
                handleVolumeButtonPress()
                return true
            }
            return originalCallback.dispatchKeyEvent(event)
        }
    }

    private fun handleVolumeButtonPress() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastButtonPressTime < PRESS_TIMEOUT) {
            consecutivePressCount++
            if (consecutivePressCount >= 3) {
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
}

