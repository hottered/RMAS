package com.example.fishingapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AddLocationFragment : Fragment() {


//    val players = arrayOf("Ronaldo","messi","djole","kataBaka")

    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var user: User

    private lateinit var markerTitle: EditText
    private lateinit var markerDescription: EditText
    private lateinit var commonSpecie: String
    private lateinit var btnAddLocation: Button
    private lateinit var imageForMarker: ImageView
    private lateinit var selectedImg: Uri

    private var currentLocation: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_location, container, false)


        val loading = LoadingDialog(requireActivity())

        val spinner = view.findViewById<Spinner>(R.id.spinner)

        val speciesNames = loadSpeciesNamesFromResource()
        val arrayAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            speciesNames
        )
        spinner.adapter = arrayAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                commonSpecie = parent?.getItemAtPosition(position).toString()
                Toast.makeText(
                    requireContext(),
                    "Selected player: $commonSpecie",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing here
            }
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        firebaseAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        markerTitle = view.findViewById(R.id.title_marker_custom)
        btnAddLocation = view.findViewById(R.id.addPlace_button)
        imageForMarker = view.findViewById(R.id.image_location_marker)
        markerDescription = view.findViewById(R.id.marker_description)


        currentLocation = arguments?.getParcelable("current_location")
        Log.d("LocationFragment", currentLocation.toString());
        btnAddLocation.setOnClickListener {
            loading.startLoading()
            getUserData(loading)
        }
        imageForMarker.setOnClickListener {
            val intent = Intent();
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        return view
    }


    private fun uploadMarkerImageUrl(markerKey: DatabaseReference,loading:LoadingDialog) {
        val reference = storage.reference.child("Markers").child(markerKey.key.toString())
        reference.putFile(selectedImg).addOnSuccessListener {
            reference.downloadUrl.addOnCompleteListener {
                if (it.isSuccessful) {
                    reference.downloadUrl.addOnSuccessListener { task ->
                        Log.d("ImageUrl", task.toString())

                        val title = markerTitle.text.toString().trim()

                        val markerData = MarkerData(
                            title = title,
                            description = markerDescription?.text.toString(),
                            commonSpecie = commonSpecie,
                            latitude = currentLocation!!.latitude,
                            longitude = currentLocation!!.longitude,
                            user = user,
                            imageMarker = task.toString(),
                            createdAtUtc = System.currentTimeMillis()
                        )
                        markerKey.setValue(markerData).addOnSuccessListener {
                            // Marker data uploaded successfully
                            loading.isDismiss()
                            navigateBackToMapFragment()
                        }.addOnFailureListener {
                            // Failed to upload marker data
                            Log.e(
                                "AddLocationFragment",
                                "Failed to upload marker data: ${it.message}"
                            )
                        }

//                        getUserData(task.toString())
                    }
                }
            }
        }
    }

    private fun loadSpeciesNamesFromResource(): List<String> {
        val inputStream = resources.openRawResource(R.raw.species)
        val speciesNames = mutableListOf<String>()
        inputStream.bufferedReader().useLines { lines ->
            lines.forEach { speciesNames.add(it) }
        }
        return speciesNames
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val userImage = view?.findViewById<ImageView>(R.id.image_location_marker);
        if (data != null) {
            if (data.data != null) {
                selectedImg = data.data!!
                userImage?.setImageURI(selectedImg)
//                val textToGoAway = view?.findViewById<TextView>(R.id.uploadimage_textview_register)
//                textToGoAway.visibility = View.GONE
            }
        }
    }

    private fun getUserData(loading: LoadingDialog) {
        val uid = firebaseAuth.currentUser?.uid.toString();

        databaseReference.child(uid).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                user = snapshot.getValue(User::class.java)!!

                addLocationToFirebase(loading)

            } else {
                // Handle the case when the user data does not exist in the database
            }
        }.addOnFailureListener { error ->
            // Handle any error that occurs while fetching the data
        }
    }

    private fun addLocationToFirebase(loading: LoadingDialog) {
        if (currentLocation != null) {

            val databaseReference = FirebaseDatabase.getInstance().getReference("markers")
            val newMarkerReference = databaseReference.push()
            Log.d("MarkerKey", newMarkerReference.key.toString());

            uploadMarkerImageUrl(newMarkerReference,loading)

        }
    }

    private fun navigateBackToMapFragment() {
        parentFragmentManager.popBackStack()
    }

    companion object {
        private const val DEFAULT_ZOOM = 15f
    }
}