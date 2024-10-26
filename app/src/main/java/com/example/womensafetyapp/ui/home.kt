package com.example.womensafetyapp.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.womensafetyapp.R


class home : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(com.example.womensafetyapp.R.layout.fragment_home, container, false)
        val emercontact = view.findViewById<LinearLayout>(com.example.womensafetyapp.R.id.emercontacts )
        val nearbyps = view.findViewById<LinearLayout>(R.id.nearpolicesta)
        val nearbyhos = view.findViewById<LinearLayout>(R.id.nearhospitals)
        val nearhotels = view.findViewById<LinearLayout>(R.id.nearhotels)
        val nightshelters = view.findViewById<LinearLayout>(R.id.nightshelters)
        emercontact.setOnClickListener() {
            performTransaction()
        }
        nearbyps.setOnClickListener(){
            val intent = Intent(requireActivity(),MapsActivity::class.java)
            var extra = "Police Station"
            intent.putExtra("extra",extra)
            startActivity(intent)
        }
        nearbyhos.setOnClickListener(){
            val intent = Intent(requireActivity(),MapsActivity::class.java)
            var extra = "Hospital"
            intent.putExtra("extra",extra)
            startActivity(intent)
        }
        nearhotels.setOnClickListener(){
            val intent = Intent(requireActivity(),MapsActivity::class.java)
            var extra = "Hotels"
            intent.putExtra("extra",extra)
            startActivity(intent)
        }
        nightshelters.setOnClickListener(){
            val intent = Intent(requireActivity(),MapsActivity::class.java)
            var extra = "Night Shelters"
            intent.putExtra("extra",extra)
            startActivity(intent)
        }

                // Inflate the layout for this fragment
        return view
    }
    @SuppressLint("SuspiciousIndentation")
    private fun performTransaction() {
    val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container,emergencycontacts())
        transaction.commit()
        transaction.addToBackStack("replacement")
      }
    }
