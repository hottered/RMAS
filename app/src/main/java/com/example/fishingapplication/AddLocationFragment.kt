package com.example.fishingapplication

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.FirebaseDatabase

class AddLocationFragment : Fragment() {

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

        markerTitle = view.findViewById(R.id.title_marker_custom)
        btnAddLocation = view.findViewById(R.id.addPlace_button)

        currentLocation = arguments?.getParcelable("current_location")
        Log.d("LocationFragment",currentLocation.toString());
        btnAddLocation.setOnClickListener {
//            addLocationToMap()
            addLocationToFirebase()
        }


        return view
    }

    private fun addLocationToFirebase() {
        val title = markerTitle.text.toString().trim()
        if (currentLocation != null) {
            val markerData = MarkerData(title, currentLocation!!.latitude, currentLocation!!.longitude)
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

    private fun addLocationToMap() {
        val title = markerTitle.text.toString().trim()
        if (currentLocation != null) {
            // Add a marker with the title and current location to the map in MapFragment
            val mapFragment = parentFragmentManager.fragments.find { it is MapFragment } as MapFragment?
            mapFragment?.googleMap?.addMarker(
                MarkerOptions()
                    .position(currentLocation!!)
                    .title(title)
            )
            mapFragment?.googleMap?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(currentLocation!!, DEFAULT_ZOOM)
            )
        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 15f
    }
}