package com.example.fishingapplication

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class FilterFragment : Fragment() {

    private lateinit var btnApplyFilters: Button
    private lateinit var usersTextHolder: TextView
    private lateinit var specieTextHolder: TextView
    private lateinit var dateTextHolder: TextView
    private lateinit var filterByRadius : EditText
    private lateinit var buttonResetFilters:Button

    private var selectedUser: BooleanArray = booleanArrayOf()
    private var newListUsers: ArrayList<Int> = ArrayList()
    private var selectedUserNamesList: ArrayList<String> = ArrayList()

    private var selectedSpecie: BooleanArray = booleanArrayOf()
    private var newListSpecie: ArrayList<Int> = ArrayList()
    private var selectedSpecieNamesList: ArrayList<String> = ArrayList()

    private var dateStart : Long = 0
    private var dateEnd : Long = 0

    private var radiusNumber : Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_filter, container, false)

        usersTextHolder = view.findViewById(R.id.textView)
        btnApplyFilters = view.findViewById(R.id.apply_filters_button);
        specieTextHolder = view.findViewById(R.id.filterBy_specie_textView)
        dateTextHolder = view.findViewById(R.id.filterBy_daterange)
        filterByRadius = view.findViewById(R.id.filterBy_radius)
        buttonResetFilters = view.findViewById(R.id.button_clear_filters)

        btnApplyFilters.setOnClickListener {

            Log.d("SelectedUsers", selectedUserNamesList.toString())
            Log.d("SelectedSpecies", selectedSpecieNamesList.toString())

            val textRadiusNumber = filterByRadius.text.toString()
            if(textRadiusNumber.isNotEmpty()){
                radiusNumber = textRadiusNumber.toDouble()
            }
            else
            {
                radiusNumber = 0.0
            }

            val bundle = Bundle()
            bundle.putStringArrayList("selectedUsers", selectedUserNamesList)
            bundle.putStringArrayList("selectedSpecies", selectedSpecieNamesList)

            bundle.putLong("dateStart", dateStart)
            bundle.putLong("dateEnd",dateEnd)

            bundle.putDouble("filterRadius",radiusNumber)

            // Create a new instance of the MapFragment
            val mapFragment = MapFragment()
            mapFragment.arguments = bundle

            // Replace the current fragment with the MapFragment
            val fragmentManager = requireActivity().supportFragmentManager
            fragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    mapFragment
                ) // R.id.fragment_container should be the ID of the container where you want to display the MapFragment
                .addToBackStack(null) // Add this transaction to the back stack
                .commit()
        }

        buttonResetFilters.setOnClickListener {
            selectedUserNamesList = ArrayList()
            selectedSpecieNamesList = ArrayList()

            dateStart = 0
            dateEnd = 0
            radiusNumber = 0.0

//            usersTextHolder.text = ""
//            specieTextHolder.text = ""
            dateTextHolder.text = ""
            filterByRadius.setText(null);

            for (j in selectedUser.indices) {
                // remove all selection
                selectedUser[j] = false
                // clear language list
                newListUsers.clear()
                // clear text view value
                usersTextHolder.text = ""
            }

            for (j in selectedSpecie.indices) {
                // remove all selection
                selectedSpecie[j] = false
                // clear language list
                newListSpecie.clear()
                // clear text view value
                specieTextHolder.text = ""
            }



        }

        dateTextHolder.setOnClickListener {
            Log.d("Clicked on date", "Error here")
            showDateRangePicker()
        }

        fetchUsernamesFromFirebase { usernameList ->
            // initialize selected language array
            selectedUser = BooleanArray(usernameList.size)

            usersTextHolder.setOnClickListener(View.OnClickListener {
                // Initialize alert dialog
                val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                // set title
                builder.setTitle("Select user")
                // set dialog non cancelable
                builder.setCancelable(false)
                builder.setMultiChoiceItems(
                    usernameList.toTypedArray(),
                    selectedUser,
                    DialogInterface.OnMultiChoiceClickListener { dialogInterface, i, b ->
                        if (b) {
                            newListUsers.add(i)
                            newListUsers.sort()
                            selectedUserNamesList.add(usernameList[i])
                        } else {
                            // when checkbox unselected
                            // Remove position from newListUsers
                            newListUsers.remove(Integer.valueOf(i))
                        }
                    }
                )

                builder.setPositiveButton(
                    "OK",
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        val stringBuilder = StringBuilder()
                        // use for loop
                        for (j in 0 until newListUsers.size) {
                            // concat array value
                            stringBuilder.append(usernameList[newListUsers[j]])
                            // check condition
                            if (j != newListUsers.size - 1) {
                                // When j value not equal
                                // to lang list size - 1
                                // add comma
                                stringBuilder.append(", ")
                            }
                        }
                        // set text on usersTextHolder
                        usersTextHolder.text = stringBuilder.toString()

                    })

                builder.setNegativeButton(
                    "Cancel",
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        dialogInterface.dismiss()
                    })
                builder.setNeutralButton(
                    "Clear All",
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        // use for loop
                        for (j in selectedUser.indices) {
                            // remove all selection
                            selectedUser[j] = false
                            // clear language list
                            newListUsers.clear()
                            // clear text view value
                            usersTextHolder.text = ""
                        }
                    })
                // show dialog
                builder.show()
            })
        }

        fetchSpeciesFromRaw { speciesList ->

            // initialize selected language array
            selectedSpecie = BooleanArray(speciesList.size)

            specieTextHolder.setOnClickListener(View.OnClickListener {
                // Initialize alert dialog
                val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

                // set title
                builder.setTitle("Select species")

                // set dialog non cancelable
                builder.setCancelable(false)

                builder.setMultiChoiceItems(
                    speciesList.toTypedArray(),
                    selectedSpecie,
                    DialogInterface.OnMultiChoiceClickListener { dialogInterface, i, b ->
                        // check condition
                        if (b) {
                            // when checkbox selected
                            // Add position in lang list
                            newListSpecie.add(i)
                            // Sort array list
                            newListSpecie.sort()

                            selectedSpecieNamesList.add(speciesList[i])
                        } else {
                            // when checkbox unselected
                            // Remove position from newListUsers
                            newListSpecie.remove(Integer.valueOf(i))
                        }
                    }
                )

                builder.setPositiveButton(
                    "OK",
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        // Initialize string builder
                        val stringBuilder = StringBuilder()
                        // use for loop
                        for (j in 0 until newListSpecie.size) {
                            // concat array value
                            stringBuilder.append(speciesList[newListSpecie[j]])
                            // check condition
                            if (j != newListSpecie.size - 1) {
                                // When j value not equal
                                // to lang list size - 1
                                // add comma
                                stringBuilder.append(", ")
                            }
                        }
                        // set text on usersTextHolder
                        specieTextHolder.text = stringBuilder.toString()

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
                        for (j in selectedSpecie.indices) {
                            // remove all selection
                            selectedSpecie[j] = false
                            // clear language list
                            newListSpecie.clear()
                            // clear text view value
                            specieTextHolder.text = ""
                        }
                    })
                // show dialog
                builder.show()
            })
        }

        return view
    }

    private fun showDateRangePicker() {
        val datePicker = MaterialDatePicker.Builder
            .dateRangePicker()
            .setTheme(R.style.ThemeMaterialCalendar)
            .setTitleText("Selectm a date range")
            .setSelection(Pair(null, null))
            .build()
        datePicker.show(requireActivity().supportFragmentManager, "DatePicker")

//         Setting up the event for when ok is clicked
        datePicker.addOnPositiveButtonClickListener {

            dateStart = it.first
            dateEnd = it.second
//            selectedDatePair = Pair(it.first, it.second)
            dateTextHolder.text = (convertTimeToDate(it.first) + " " + convertTimeToDate(it.second))
            Toast.makeText(
                requireContext(),
                "${datePicker.headerText} is selected",
                Toast.LENGTH_LONG
            ).show()
        }

        // Setting up the event for when cancelled is clicked
        datePicker.addOnNegativeButtonClickListener {
            datePicker.dismiss()
            Toast.makeText(
                requireContext(),
                "${datePicker.headerText} is cancelled",
                Toast.LENGTH_LONG
            ).show()
        }

        // Setting up the event for when back button is pressed
        datePicker.addOnCancelListener {
            datePicker.dismiss()
            Toast.makeText(requireContext(), "Date Picker Cancelled", Toast.LENGTH_LONG).show()
        }
    }
    private fun convertTimeToDate(time: Long): String {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utc.timeInMillis = time
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return format.format(utc.time);
    }
    private fun fetchSpeciesFromRaw(callback: (ArrayList<String>) -> Unit) {
        val speciesList: ArrayList<String> = ArrayList()

        try {
            val inputStream = resources.openRawResource(R.raw.species)
            val reader = BufferedReader(InputStreamReader(inputStream))

            var line: String? = reader.readLine()
            while (line != null) {
                speciesList.add(line)
                line = reader.readLine()
            }

            reader.close()
            inputStream.close()

            callback(speciesList)
        } catch (e: Exception) {
            // Handle the error, if needed
            callback(ArrayList()) // Empty list if there's an error
        }
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

}