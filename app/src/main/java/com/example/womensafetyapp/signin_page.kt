package com.example.womensafetyapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class signin_page : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var mGoogleSignInClient: GoogleSignInClient
    val RC_SIGN_IN: Int =  1
    private lateinit var database: DatabaseReference
    private var currentUser: FirebaseUser? = null
    lateinit var gso: GoogleSignInOptions
    private lateinit var loginProgressBar: ProgressBar
    private lateinit var googleProgressBar: ProgressBar
    @SuppressLint("MissingInflatedId", "SuspiciousIndentation")

        override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        enableEdgeToEdge()
        setContentView(R.layout.activity_signin_page)
        //for changing status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser
        database = FirebaseDatabase.getInstance().getReference("Users")
        val uemail = findViewById<EditText>(R.id.uemail)
        val upassword = findViewById<EditText>(R.id.upassword)
        val loginbtn = findViewById<Button>(R.id.signinbtn)
        val signuptv = findViewById<TextView>(R.id.signuptv)
        val googlebtn = findViewById<SignInButton>(R.id.googlebtn)

        // Add progress bar references
        loginProgressBar = findViewById(R.id.loginProgressBar)
        googleProgressBar = findViewById(R.id.googleProgressBar)

        //signing using email and password
        loginbtn.setOnClickListener(){
            val email = uemail.text.toString()
            val pass = upassword.text.toString()

            if(email.isBlank()||pass.isBlank()){
                Toast.makeText(this, "Email and Password can't be blank", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show progress bar, hide button text
            loginbtn.text = ""
            loginProgressBar.visibility = View.VISIBLE

            auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    val currentUser = auth.currentUser
                    currentUser?.let {
                        val userid = it.uid
                        database.child("users").child(userid).get()
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    Toast.makeText(this, "Sign-Up Successful", Toast.LENGTH_SHORT).show()
                                    loginProgressBar.visibility = View.GONE
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                } else {
                                    loginProgressBar.visibility = View.GONE
                                    loginbtn.text = "Login In"
                                    Toast.makeText(this, "Failed to save user data: ${dbTask.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else
                    loginProgressBar.visibility = View.GONE
                    loginbtn.text = "Sign In"
                    Toast.makeText(this, "Log In failed ", Toast.LENGTH_SHORT).show()
            }
        }
        signuptv.setOnClickListener(){
            val intent = Intent(this,signup_page::class.java)
            startActivity(intent)
        }
        //Now signing using Google
        createRequest()

        googlebtn.setOnClickListener(){
            googlebtn.visibility = View.GONE
            googleProgressBar.visibility = View.VISIBLE
            signIn();
        }
    }
    private fun createRequest() {
        // Configure Google Sign In
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception=task.exception
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            }
            catch (e: ApiException) {
                googleProgressBar.visibility = View.GONE
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                // Reset Google button
                googleProgressBar.visibility = View.GONE
                findViewById<SignInButton>(R.id.googlebtn).visibility = View.VISIBLE
                if (task.isSuccessful) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val exception = task.exception
                    Toast.makeText(this, "Login Failed: ${exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}