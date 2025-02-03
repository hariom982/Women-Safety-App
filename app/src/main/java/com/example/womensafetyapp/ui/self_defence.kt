package com.example.womensafetyapp.ui

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.womensafetyapp.R

class self_defence : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =inflater.inflate(R.layout.fragment_self_defence, container, false)
        val textView: TextView = view.findViewById(R.id.self_defence_text_view)
        val fullText = getString(R.string.self_defence)
        val spannableString = SpannableString(fullText)

        // Define the text to make clickable
        val clickableText = "pepper spray"

        // Find the start and end index of the clickable text
        val startIndex = fullText.indexOf(clickableText)
        val endIndex = startIndex + clickableText.length

        if (startIndex != -1) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    Toast.makeText(requireContext(),"Pepper spray clicked!",Toast.LENGTH_SHORT).show()
                }
            }
            spannableString.setSpan(
                clickableSpan,
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance() // Makes the link clickable

        return view
    }

}