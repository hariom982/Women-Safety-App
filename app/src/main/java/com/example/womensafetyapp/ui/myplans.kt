package com.example.womensafetyapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.womensafetyapp.R
import com.example.womensafetyapp.karatechapters.chapter1_fragment
import com.example.womensafetyapp.karatechapters.chapter2_fragment
import com.example.womensafetyapp.karatechapters.chapter3_fragment
import com.example.womensafetyapp.karatechapters.chapter4_fragemnt
import com.example.womensafetyapp.karatechapters.chapter6_fragment
import com.example.womensafetyapp.karatechapters.chatper5_fragment

class myplans : Fragment() {
    private lateinit var chapter1Card: CardView
    private lateinit var chapter2Card: CardView
    private lateinit var chapter3Card: CardView
    private lateinit var chapter4Card: CardView
    private lateinit var chapter5Card: CardView
    private lateinit var chapter6Card: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_myplans, container, false)

        // Initialize CardViews
        chapter1Card = view.findViewById(R.id.chapter1_card)
        chapter2Card = view.findViewById(R.id.chapter2_card)
        chapter3Card = view.findViewById(R.id.chapter3_card)
        chapter4Card = view.findViewById(R.id.chapter4_card)
        chapter5Card = view.findViewById(R.id.chapter5_card)
        chapter6Card = view.findViewById(R.id.chapter6_card)

        // Set click listeners
         setupClickListeners()

        return view
    }

    private fun setupClickListeners() {
        chapter1Card.setOnClickListener {
            performtransaction(chapter1_fragment())
        }

        chapter2Card.setOnClickListener {
            // Navigate to Chapter 2 Fragment
            performtransaction(chapter2_fragment())
        }

        chapter3Card.setOnClickListener {
            // Navigate to Chapter 3 Fragment
            performtransaction(chapter3_fragment())
        }

        chapter4Card.setOnClickListener {
            // Navigate to Chapter 4 Fragment
            performtransaction(chapter4_fragemnt())
        }

        chapter5Card.setOnClickListener {
            // Navigate to Chapter 5 Fragment
            performtransaction(chatper5_fragment())
        }

        chapter6Card.setOnClickListener {
            // Navigate to Chapter 6 Fragment
            performtransaction(chapter6_fragment())
        }
    }
    private fun performtransaction(fragment: Fragment){
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}