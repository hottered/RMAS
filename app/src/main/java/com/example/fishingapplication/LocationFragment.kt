package com.example.fishingapplication

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LocationFragment : Fragment() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var authReference: FirebaseAuth

    private lateinit var imageLocation: ImageView
    private lateinit var titleLocation: TextView
    private lateinit var descriptionLocation: TextView
    private lateinit var ratingLocation: TextView
    private lateinit var submitButton: Button

    private var sumOfAllUsers: Double? = 0.0
    private var numOfUsersWhoRated: Double? = 0.0

    private lateinit var markerKey: String

    private lateinit var ratingBar: RatingBar

    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0

    private lateinit var glide: RequestManager


    override fun onAttach(context: Context) {
        super.onAttach(context)

        // Perform context-related operations here
       glide = Glide.with(this)
        // ... Continue with the rest of your code ...
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_location, container, false)

        databaseReference = FirebaseDatabase.getInstance().getReference("markers")
        authReference = FirebaseAuth.getInstance()

        imageLocation = view.findViewById(R.id.image_location_view)
        titleLocation = view.findViewById<TextView>(R.id.title_location_view)
        descriptionLocation =
            view.findViewById<TextView>(R.id.description_location_view)
        ratingLocation = view.findViewById<TextView>(R.id.rating_location_view)

        ratingBar = view.findViewById(R.id.ratingBar_adapter)
        submitButton = view.findViewById(R.id.button_submit_rating)

        latitude = arguments?.getDouble("locationFragmentLatitude") ?: 0.0
        longitude = arguments?.getDouble("locationFragmentLongitude") ?: 0.0

        Log.d("LocationFragmentHere", latitude.toString())
        Log.d("LocationFragmentHere", longitude.toString())
        retrieveMarkerInformation(latitude, longitude) { markerData ->

            if (markerData.user?.uid == authReference.currentUser?.uid) {
                ratingBar.visibility = View.GONE
                submitButton.visibility = View.GONE
            }

            sumOfAllUsers = markerData.sumOfRatings
            numOfUsersWhoRated = markerData.numOfUsersRated

            Log.d("markerDataTitlte", markerData.title.toString())
            titleLocation.text = markerData.title.toString()
            descriptionLocation.text = markerData.description.toString()
            if (markerData.rating == null) {
                ratingLocation.text = "Not rated yet"
            } else {
                ratingLocation.text = markerData.rating.toString()
            }
            glide.load(markerData.imageMarker)
                .into(imageLocation)

        }
        submitButton.setOnClickListener {
            Log.d("RatingBarChanged", ratingBar.rating.toString())

            numOfUsersWhoRated = numOfUsersWhoRated!!.plus(1.0)
            sumOfAllUsers = sumOfAllUsers!!.plus(ratingBar.rating.toDouble())
            updateMarkerUsersAndNumber(markerKey, sumOfAllUsers!!, numOfUsersWhoRated!!)
//            updateMarkerRating(markerKey, ratingBar.rating.toDouble())
            val newRating =
                String.format("%.2f", sumOfAllUsers!!.div(numOfUsersWhoRated!!)).toDouble()
            updateMarkerRating(markerKey, newRating)
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
                                Log.d("MarkerKays", locationsSnapshot.key.toString())
                                markerKey = locationsSnapshot.key.toString()
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

    private fun updateMarkerUsersAndNumber(markerId: String, newSum: Double, newCount: Double) {
        val numOfUsersRated = databaseReference.child(markerId).child("numOfUsersRated")
        val sumOfUsers = databaseReference.child(markerId).child("sumOfRatings")
        numOfUsersRated.setValue(newCount).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("here", "success")
//                Toast.makeText(requireContext(), "numOfUsersRated updated successfully", Toast.LENGTH_SHORT)
//                    .show()
            } else {
//                Toast.makeText(requireContext(), "numOfUsersRated to update rating", Toast.LENGTH_SHORT)
//                    .show()
            }
        }
        sumOfUsers.setValue(newSum).addOnCompleteListener { task ->
            if (task.isSuccessful) {
//                Toast.makeText(requireContext(), "sumOfUsers updated successfully", Toast.LENGTH_SHORT)
//                    .show()
                Log.d("here", "success")

            } else {
//                Toast.makeText(requireContext(), "sumOfUsers to update rating", Toast.LENGTH_SHORT)
//                    .show()
            }
        }
    }

    private fun updateMarkerRating(markerId: String, newRating: Double) {
        val ratingRef = databaseReference.child(markerId).child("rating")

        ratingRef.setValue(newRating).addOnCompleteListener { task ->
            if (task.isSuccessful) {
//                Toast.makeText(requireContext(), "Rating updated successfully", Toast.LENGTH_SHORT)
//                    .show()
                Log.d("here", "success")

//                parentFragmentManager.popBackStack()

            } else {
//                Toast.makeText(requireContext(), "Failed to update rating", Toast.LENGTH_SHORT)
//                    .show()
            }
        }
    }

    companion object {

    }
}