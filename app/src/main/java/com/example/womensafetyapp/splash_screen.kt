package com.example.womensafetyapp

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class splash_screen : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)
        val womentv = findViewById<TextView>(R.id.womentv)
        val safetytv = findViewById<TextView>(R.id.safetytv)
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()


        // Create an ObjectAnimator to slide the logo down
        ObjectAnimator.ofFloat(womentv, "translationY", -300f,0f).apply {
            duration = 1500 // Set the duration of the animation
            start() // Start the animation
        }
        ObjectAnimator.ofFloat(safetytv, "translationY", 300f,0f).apply {
            duration = 1500 // Set the duration of the animation
            start()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, signin_page::class.java))
            finish() // Close the splash screen activity
        }, 2000)
    }
    override fun onStart() {
        super.onStart()
        val user = firebaseAuth.currentUser
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}