package com.example.womensafetyapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.womensafetyapp.R

class shelf : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_shelf, container, false)
        val safety = view.findViewById<LinearLayout>(R.id.safetytips)
        val yoga = view.findViewById<LinearLayout>(R.id.yoga)
        val periodstracker = view.findViewById<LinearLayout>(R.id.periodtracker)
        val plans = view.findViewById<LinearLayout>(R.id.myplans)
        safety.setOnClickListener {
            performtransaction(safetytips())
        }
        yoga.setOnClickListener {
            performtransaction(Yoga())
        }
        periodstracker.setOnClickListener {
            performtransaction(periodstracker())
        }
        plans.setOnClickListener {
            performtransaction(myplans())
        }
        return view
    }
    private fun performtransaction(fragment:Fragment){
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}