package com.example.womensafetyapp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.womensafetyapp.R
import com.example.womensafetyapp.ViewPagerAdapter


class safetytips : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_safetytips, container, false)
        val viewPager: ViewPager2 = view.findViewById(R.id.viewPager)

        // List of layouts
        val layouts = listOf(
            R.layout.personal_awareness,
            R.layout.fragment_self_defence,
            R.layout.home_safety,
            R.layout.mental_phy_depen,
            R.layout.danger
        )

        // Set adapter
        viewPager.adapter = ViewPagerAdapter(layouts)

        // Optional: Enable swipe transitions
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        return view
    }

}