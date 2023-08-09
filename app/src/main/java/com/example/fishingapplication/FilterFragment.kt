package com.example.fishingapplication

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class FilterFragment : Fragment() {


    private lateinit var textView: TextView
     var selectedLanguage: BooleanArray = booleanArrayOf()
     var langList: ArrayList<Int> = ArrayList()
     var langArray: Array<String> = arrayOf("Java", "C++", "Kotlin", "C", "Python", "Javascript","Djole","mika","pera")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_filter, container, false)

        textView = view.findViewById(R.id.textView)

       fetchUsernamesFromFirebase{
           usernameList ->
           Log.d("UserNameListItem",usernameList[0]);

           // initialize selected language array
           selectedLanguage = BooleanArray(usernameList.size)

           textView.setOnClickListener(View.OnClickListener {
               // Initialize alert dialog
               val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

               // set title
               builder.setTitle("Select user")

               // set dialog non cancelable
               builder.setCancelable(false)

               builder.setMultiChoiceItems(
                   usernameList.toTypedArray(),
                   selectedLanguage,
                   DialogInterface.OnMultiChoiceClickListener { dialogInterface, i, b ->
                       // check condition
                       if (b) {
                           // when checkbox selected
                           // Add position in lang list
                           langList.add(i)
                           // Sort array list
                           langList.sort()
                       } else {
                           // when checkbox unselected
                           // Remove position from langList
                           langList.remove(Integer.valueOf(i))
                       }
                   }
               )

               builder.setPositiveButton(
                   "OK",
                   DialogInterface.OnClickListener { dialogInterface, i ->
                       // Initialize string builder
                       val stringBuilder = StringBuilder()
                       // use for loop
                       for (j in 0 until langList.size) {
                           // concat array value
                           stringBuilder.append(langArray[langList[j]])
                           // check condition
                           if (j != langList.size - 1) {
                               // When j value not equal
                               // to lang list size - 1
                               // add comma
                               stringBuilder.append(", ")
                           }
                       }
                       // set text on textView
                       textView.text = stringBuilder.toString()
                   })

               builder.setNegativeButton(
                   "Cancel",
                   DialogInterface.OnClickListener { dialogInterface, i ->
                       // dismiss dialog
                       dialogInterface.dismiss()
                   })
               builder.setNeutralButton(
                   "Clear All",
                   DialogInterface.OnClickListener { dialogInterface, i ->
                       // use for loop
                       for (j in selectedLanguage.indices) {
                           // remove all selection
                           selectedLanguage[j] = false
                           // clear language list
                           langList.clear()
                           // clear text view value
                           textView.text = ""
                       }
                   })
               // show dialog
               builder.show()
           })
       }


        return view
    }
    private fun fetchUsernamesFromFirebase(callback: (ArrayList<String>) -> Unit) {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val usersReference: DatabaseReference = database.getReference("users")

        val usernameList: ArrayList<String> = ArrayList()

        usersReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapshot in dataSnapshot.children) {
                    val username = userSnapshot.child("username").getValue(String::class.java)
                    username?.let {
                        usernameList.add(it)
                    }
                }
                callback(usernameList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error, if needed
                callback(ArrayList()) // Empty list if there's an error
            }
        })
    }
    private fun makeFilter(){

    }
}