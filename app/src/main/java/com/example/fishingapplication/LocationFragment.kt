package com.example.fishingapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LocationFragment : Fragment() {

    private lateinit var databaseReference: DatabaseReference

    private lateinit var imageLocation: ImageView
    private lateinit var titleLocation: TextView
    private lateinit var descriptionLocation: TextView
    private lateinit var ratingLocation: TextView
    private lateinit var numbereOfUsersRated: TextView

    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_location, container, false)

        databaseReference = FirebaseDatabase.getInstance().getReference("markers")

        imageLocation = view.findViewById(R.id.image_location_view)
        titleLocation = view.findViewById<TextView>(R.id.title_location_view)
        descriptionLocation =
            view.findViewById<TextView>(R.id.description_location_view)
        ratingLocation = view.findViewById<TextView>(R.id.rating_location_view)
        numbereOfUsersRated =
            view.findViewById<TextView>(R.id.numberofusers_location_View)

        latitude = arguments?.getDouble("locationFragmentLatitude") ?: 0.0
        longitude = arguments?.getDouble("locationFragmentLongitude") ?: 0.0

        Log.d("LocationFragmentHere", latitude.toString())
        Log.d("LocationFragmentHere", longitude.toString())
        retrieveMarkerInformation(latitude, longitude) { markerData ->
            Log.d("markerDataTitlte",markerData.title.toString())
            titleLocation.text = markerData.title.toString()
            descriptionLocation.text = markerData.description.toString()
            ratingLocation.text = markerData.rating.toString()
            Glide.with(requireContext())
                .load(markerData.imageMarker)
                .into(imageLocation)

        }

        return view
    }

    private fun retrieveMarkerInformation(
        latitude: Double?,
        longitude: Double?,
        callback: (MarkerData) -> Unit
    ) {
        if (latitude != null && longitude != null) {
            // Retrieve data from the database
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {
                        for (locationsSnapshot in snapshot.children) {

                            val location = locationsSnapshot.getValue(MarkerData::class.java)
                            if (location?.latitude == latitude && location?.longitude == longitude) {
                                Log.d("Naso sam te kurvo", "Evo me ")
                                callback(location)
                            }

                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }


            })
        }
    }

    companion object {

    }
}