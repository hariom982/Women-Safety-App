package com.example.womensafetyapp.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.womensafetyapp.R
import com.example.womensafetyapp.signup_page
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class profile : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        currentUser = auth.currentUser
    }
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val uphoneno = view.findViewById<TextView>(R.id.phoneno)
        val uname = view.findViewById<TextView>(R.id.uname)
        val uemail = view.findViewById<TextView>(R.id.uemail)
        val signoutbtn = view.findViewById<Button>(R.id.signoutbtn)

        currentUser?.let {user->
            val userId = user.uid
            database.child("users").child(userId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Extract user data from the snapshot
                        val name = snapshot.child("name").getValue(String::class.java)
                        val phone = snapshot.child("phone").getValue(String::class.java)
                        val email = snapshot.child("email").getValue(String::class.java)

                        // Set the data to TextViews
                        uname.text = name
                        uphoneno.text = phone
                        uemail.text = email
                    } else {
                        Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to retrieve user data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } ?: run {
            Toast.makeText(context, "User is not logged in", Toast.LENGTH_SHORT).show()
        }

        signoutbtn.setOnClickListener() {
            auth.signOut()
                val intent = Intent(activity, signup_page::class.java)
                startActivity(intent)
            Toast.makeText(context,"Sign out successful",Toast.LENGTH_SHORT).show()
            }

        return view
    }

}