package com.example.womensafetyapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class signup_page : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var currentUser: FirebaseUser? = null
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup_page)
        auth = Firebase.auth
        currentUser = auth.currentUser
        database = FirebaseDatabase.getInstance().reference
        val uname = findViewById<EditText>(R.id.uname)
        val umobilenumber = findViewById<EditText>(R.id.umobilenumber)
        val uemail = findViewById<EditText>(R.id.uemail)
        val password = findViewById<EditText>(R.id.upassword)
        val signupbtn = findViewById<Button>(R.id.signinbtn)
        val signintv = findViewById<TextView>(R.id.signintv)

        signupbtn.setOnClickListener {
            val uname = uname.text.toString()
            val email = uemail.text.toString()
            val pass = password.text.toString()
            val umobilenumber = umobilenumber.text.toString()
            // check pass
            if (email.isBlank() || pass.isBlank() || umobilenumber.isBlank()) {
                Toast.makeText(this, "Email,Mobile Number and Password can't be blank", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val currentUser = auth.currentUser
                        currentUser?.let {
                            val userId = it.uid
                            val userMap = mapOf(
                                "name" to uname,
                                "phone" to umobilenumber,
                                "email" to email
                            )

                            // Save user info in Firebase Realtime Database
                            database.child("users").child(userId).setValue(userMap)
                                .addOnCompleteListener { dbTask ->
                                    if (dbTask.isSuccessful) {
                                        Toast.makeText(this, "Sign-Up Successful", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this, MainActivity::class.java)
                                        startActivity(intent)
                                    } else {
                                        Toast.makeText(this, "Failed to save user data: ${dbTask.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    } else {
                        Toast.makeText(this, "Sign-Up Failed: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // switching from signUp Activity to Login Activity
        signintv.setOnClickListener {
            val intent = Intent(this, signin_page::class.java)
            startActivity(intent)
        }

    }

}