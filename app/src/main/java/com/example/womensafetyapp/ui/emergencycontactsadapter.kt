package com.example.womensafetyapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.womensafetyapp.R


class emergencycontactsadapter(val contactList:List<Contacts>):RecyclerView.Adapter<emergencycontactsadapter.ViewHolder>() {
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.textViewContactName)
        val phone = itemView.findViewById<TextView>(R.id.textViewContactPhoneNumber)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): emergencycontactsadapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder:ViewHolder, position: Int) {
        val contact = contactList[position]
        holder.name.setText(contact.name)  // Use setText() for EditText
        holder.phone.setText(contact.phoneNumber)  // Use setText() for EditText
    }

    override fun getItemCount(): Int {
        return contactList.size
    }
    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }
}