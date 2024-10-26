package com.example.womensafetyapp.ui
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.womensafetyapp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class emergencycontacts: Fragment() {
    private lateinit var contactAdapter: emergencycontactsadapter
    private val contacts = mutableListOf<Contacts>()
    private val _contacts = MutableLiveData<List<Contacts>>()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance()
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val view = inflater.inflate(R.layout.fragment_emergencycontacts, container, false)
        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        val recyclerViewContacts = view.findViewById<RecyclerView>(R.id.recyclerViewContacts)
        recyclerViewContacts.layoutManager = LinearLayoutManager(context)
        contactAdapter = emergencycontactsadapter(contacts)
        recyclerViewContacts.adapter = contactAdapter
        //newly added
        recyclerViewContacts.setHasFixedSize(true)

        fab.setOnClickListener() {
            val dialogView =
                LayoutInflater.from(context).inflate(R.layout.dialogbox_addnewcontacts, null)

            // Create an AlertDialog builder
            val builder = AlertDialog.Builder(context)
                .setTitle("Add New Emergency Contact")
                .setView(dialogView)
                .setPositiveButton("Submit") { dialog, _ ->
                    // Find the EditText in the dialog layout
                    val addname = dialogView.findViewById<EditText>(R.id.addname)
                    val addNewContacts = dialogView.findViewById<EditText>(R.id.addnewcontact)
                    if (TextUtils.isEmpty(addname.text.toString())) {
                        addname.setError("This field cannot be empty")
                    } else if (TextUtils.isEmpty(addNewContacts.text.toString())) {
                        addNewContacts.setError("This field cannot be empty")
                    }
                    val enteredname = addname.text.toString()
                    val enterdcontact = addNewContacts.text.toString()

                    //adding new contact
                    val newContact = Contacts(enteredname, enterdcontact)
                    contacts.add(newContact)
                    contactAdapter.notifyDataSetChanged()
                    saveContactsToFirebase(contacts)
                    // findNavController().navigate(AddGuardianDirections.actionAddGuardianToGuardianInfo())
                    dialog.dismiss()  // Dismiss the dialog
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()  // Dismiss the dialog without doing anything
                }

            // Show the dialog
            builder.create().show()
        }
        loadContactsFromFirebase()
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

private fun loadContactsFromFirebase() {
    if (userId.isNotEmpty()) {
        firestore.collection("users")
            .document(userId)  // Use current user ID to fetch contacts
            .collection("emergency_contacts")
            .document("shared_contacts")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    contacts.clear()  // Clear the current list before adding new data
                    val contactsMap = document.get("contacts") as? List<HashMap<String, String>>
                    contactsMap?.forEach { contact ->
                        val name = contact["name"]
                        val phoneNumber = contact["contact"]
                        if (name != null && phoneNumber != null) {
                            contacts.add(Contacts(name, phoneNumber))
                        }
                    }
                    contactAdapter.notifyDataSetChanged()  // Notify adapter about data changes
                }
            }
            .addOnFailureListener { exception ->
                // Handle the error if the data fetch fails
            }
    }
}
    fun saveContactsToFirebase(contacts: List<Contacts>) {
        // Save contacts specific to the current user
        if (userId.isNotEmpty()) {
            val contactsMap = contacts.map { contact ->
                hashMapOf(
                    "name" to contact.name,
                    "contact" to contact.phoneNumber
                )
            }
            firestore.collection("users")
                .document(userId)  // Save contacts under the current user's ID
                .collection("emergency_contacts")
                .document("shared_contacts")  // Store all contacts in a single document for this user
                .set(hashMapOf("contacts" to contactsMap))  // Overwrite or update the document
                .addOnSuccessListener {
                    // Handle success
                }
                .addOnFailureListener { e ->
                    // Handle failure
                }
        }
    }


}


