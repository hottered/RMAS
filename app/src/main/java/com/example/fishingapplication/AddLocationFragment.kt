package com.example.fishingapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddLocationFragment : Fragment() {


    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var user: User



    private lateinit var markerTitle : EditText
    private lateinit var btnAddLocation:Button
    private var currentLocation: LatLng? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_add_location, container, false)

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        firebaseAuth = FirebaseAuth.getInstance();

        markerTitle = view.findViewById(R.id.title_marker_custom)
        btnAddLocation = view.findViewById(R.id.addPlace_button)

        currentLocation = arguments?.getParcelable("current_location")
        Log.d("LocationFragment",currentLocation.toString());
        btnAddLocation.setOnClickListener {
            getUserData()
        }

        return view
    }

//    private fun getUserData(uid: String): User? {
//        val databaseReference = FirebaseDatabase.getInstance().getReference("users/$uid")
//
//        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()) {
//                    val user = snapshot.getValue(User::class.java)
//                    // User data retrieved successfully
//                    user?.let {
//                        // You can use the retrieved user data here or return it
//                        // For now, let's just log the user's username
//                        Log.d("AddLocationFragment", "Retrieved user: ${user.username}")
//                    }
//                } else {
//                    // User does not exist
//                    Log.d("AddLocationFragment", "User with UID $uid does not exist")
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Failed to retrieve user data
//                Log.e("AddLocationFragment", "Failed to retrieve user data: ${error.message}")
//            }
//        })
//
//        return null // The actual user data will be returned asynchronously in the callback
//    }

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

//            val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
//            val user = getUserData(uid);

//            Log.d("ProfileUser",user?.username.toString());

            val markerData = MarkerData(
                title,
                currentLocation!!.latitude,
                currentLocation!!.longitude,
                user = user)
            val databaseReference = FirebaseDatabase.getInstance().getReference("markers")
            val newMarkerReference = databaseReference.push()
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