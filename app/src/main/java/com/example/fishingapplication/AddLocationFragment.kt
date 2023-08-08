package com.example.fishingapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AddLocationFragment : Fragment() {


    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var user: User

    private lateinit var markerTitle: EditText
    private lateinit var btnAddLocation: Button
    private lateinit var imageForMarker: ImageView
    private lateinit var selectedImg: Uri
    private var currentLocation: LatLng? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_location, container, false)

        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        firebaseAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        markerTitle = view.findViewById(R.id.title_marker_custom)
        btnAddLocation = view.findViewById(R.id.addPlace_button)
        imageForMarker = view.findViewById(R.id.image_location_marker)

        currentLocation = arguments?.getParcelable("current_location")
        Log.d("LocationFragment", currentLocation.toString());
        btnAddLocation.setOnClickListener {
            getUserData()
//            uploadData()
        }
        imageForMarker.setOnClickListener {
            val intent = Intent();
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        return view
    }


//    private fun uploadMarkerImage(markerKey: String) {
//        val reference = storage.reference.child("Markers").child(markerKey)
//        reference.putFile(selectedImg).addOnSuccessListener {
//            reference.downloadUrl.addOnCompleteListener {
//                if (it.isSuccessful) {
//                    reference.downloadUrl.addOnSuccessListener { task ->
////                    uploadInfo(username,email,task.toString())
//                        Log.d("ImageUrl", task.toString())
////                        getUserData(task.toString())
//                    }
//                }
//            }
//        }
//    }

    private fun uploadMarkerImage(markerKey: String) {
        val reference = storage.reference.child("Markers").child(markerKey)
        reference.putFile(selectedImg).addOnSuccessListener {
            reference.downloadUrl.addOnCompleteListener {
                if (it.isSuccessful) {
                    reference.downloadUrl.addOnSuccessListener { task ->
//                    uploadInfo(username,email,task.toString())
                        Log.d("ImageUrl", task.toString())
//                        getUserData(task.toString())
                    }
                }
            }
        }
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


    private fun getUserData() {
        val uid = firebaseAuth.currentUser?.uid.toString();

        databaseReference.child(uid).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                user = snapshot.getValue(User::class.java)!!

                addLocationToFirebase()

            } else {
                // Handle the case when the user data does not exist in the database
            }
        }.addOnFailureListener { error ->
            // Handle any error that occurs while fetching the data
        }
    }

    private fun addLocationToFirebase() {
        val title = markerTitle.text.toString().trim()
        if (currentLocation != null) {


            //first get ImageUlr
            //second getuserData
            val markerData = MarkerData(
                title,
                currentLocation!!.latitude,
                currentLocation!!.longitude,
                user = user
            )


            val databaseReference = FirebaseDatabase.getInstance().getReference("markers")
            val newMarkerReference = databaseReference.push()
            Log.d("MarkerKey", newMarkerReference.key.toString());

            uploadMarkerImage(newMarkerReference.key.toString())

            newMarkerReference.setValue(markerData).addOnSuccessListener {
                // Marker data uploaded successfully
                navigateBackToMapFragment()
            }.addOnFailureListener {
                // Failed to upload marker data
                Log.e("AddLocationFragment", "Failed to upload marker data: ${it.message}")
            }
        }
    }

    private fun navigateBackToMapFragment() {
        parentFragmentManager.popBackStack()
    }

//    private fun addLocationToMap() {
//        val title = markerTitle.text.toString().trim()
//        if (currentLocation != null) {
//            // Add a marker with the title and current location to the map in MapFragment
//            val mapFragment = parentFragmentManager.fragments.find { it is MapFragment } as MapFragment?
//            mapFragment?.googleMap?.addMarker(
//                MarkerOptions()
//                    .position(currentLocation!!)
//                    .title(title)
//            )
//            mapFragment?.googleMap?.moveCamera(
//                CameraUpdateFactory.newLatLngZoom(currentLocation!!, DEFAULT_ZOOM)
//            )
//        }
//    }

    companion object {
        private const val DEFAULT_ZOOM = 15f
    }
}