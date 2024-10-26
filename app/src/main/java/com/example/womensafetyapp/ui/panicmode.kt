package com.example.womensafetyapp.ui

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.womensafetyapp.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class panicmode : Fragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var firestore: FirebaseFirestore
    private lateinit var toggleLocationSwitch: Switch
    private lateinit var userId: String  // To store current user ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_panicmode, container, false)

        // Initialize Firestore, FusedLocationProviderClient, and get the current user ID
        firestore = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""  // Get current user ID

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

        return view
    }

    private fun checkPermissions(): Boolean {
        val locationPermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
        val smsPermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.SEND_SMS)
        return locationPermission == PackageManager.PERMISSION_GRANTED && smsPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.SEND_SMS),
            101
        )
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun getCurrentLocationAndSendSMS() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val locationLink = "https://maps.google.com/?q=${it.latitude},${it.longitude}"
                sendSmsToEmergencyContacts(locationLink)
            }
        }
    }

    private fun sendSmsToEmergencyContacts(locationLink: String) {
        if (userId.isNotEmpty()) {
            firestore.collection("users")
                .document(userId)  // Fetch contacts specific to the current user
                .collection("emergency_contacts")
                .document("shared_contacts")
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val contactsMap = document.get("contacts") as? List<HashMap<String, String>>
                        contactsMap?.forEach { contact ->
                            val phone = contact["contact"]
                            val name = contact["name"]
                            if (!phone.isNullOrEmpty()) {
                                sendSms(phone, "Hey $name! It's an Emergency! Track my location here: $locationLink")
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to fetch contacts", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun sendSms(phone: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phone, null, message, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to send SMS", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocationAndSendSMS()
        } else {
            Toast.makeText(requireContext(), "Permissions are required to send location", Toast.LENGTH_SHORT).show()
        }
    }
}
