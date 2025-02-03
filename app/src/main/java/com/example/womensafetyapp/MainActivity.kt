package com.example.womensafetyapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.womensafetyapp.databinding.ActivityMainBinding
import com.example.womensafetyapp.services.VolumeButtonService
import com.example.womensafetyapp.ui.PanicModeFragment
import com.example.womensafetyapp.ui.home
import com.example.womensafetyapp.ui.map
import com.example.womensafetyapp.ui.profile
import com.example.womensafetyapp.ui.shelf

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.SEND_SMS,
        Manifest.permission.POST_NOTIFICATIONS
    )

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (!allGranted) {
            Toast.makeText(this, "Permissions are required for safety features", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.pink_dark)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Start VolumeButtonService


        startService(Intent(this, VolumeButtonService::class.java))

        // Handle bottom navigation selection
        binding.navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> loadFragment(home())
                R.id.panic_mode -> loadFragment(PanicModeFragment())
                R.id.map -> loadFragment(map())
                R.id.profile -> loadFragment(profile())
                R.id.shelf -> loadFragment(shelf())
                else -> false
            }
        }

        // Load initial fragment
        if (savedInstanceState == null) {
            loadFragment(home())
        }

        // Request necessary permissions
        checkAndRequestLocationPermissions()
    }

    private fun checkAndRequestLocationPermissions() {
        val missingPermissions = locationPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(locationPermissions)
        }
    }

    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
        return true
    }
@SuppressLint("MissingSuperCall")
override fun onBackPressed() {
    val intent = Intent(this, signup_page::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
    finish()
}

}
