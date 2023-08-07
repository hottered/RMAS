package com.example.fishingapplication
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MapFragment : Fragment(), OnMapReadyCallback{

    private lateinit var googleMap: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var btnPlaceMarker:FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        mapView = view.findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)

        btnPlaceMarker = view.findViewById(R.id.btn_place_marker)
        btnPlaceMarker.setOnClickListener {
            placeMarkerAtCurrentLocation()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        enableMyLocation()
        fetchMarkersFromFirebase()
    }
    private fun fetchMarkersFromFirebase() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("markers")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (markerSnapshot in snapshot.children) {
                    val markerData = markerSnapshot.getValue(MarkerData::class.java)
                    if (markerData != null && markerData.latitude != null && markerData.longitude != null) {
                        val markerLatLng = LatLng(markerData.latitude, markerData.longitude)
                        val marker = googleMap.addMarker(
                            MarkerOptions()
                                .position(markerLatLng)
                                .title(markerData.title)
                        )
                        marker?.snippet = "Rating: ${markerData.rating ?: "Not rated yet"}"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MapFragment", "Failed to fetch markers: ${error.message}")
            }
        })

        googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoContents(marker: Marker): View {
                val view  = layoutInflater.inflate(R.layout.marker_info_contents,null)
                val titleView = view.findViewById<TextView>(R.id.marker_title)
                val ratingView = view.findViewById<TextView>(R.id.marker_rating)

                titleView.text = marker.title
                ratingView.text = marker.snippet

                return view;
            }

            override fun getInfoWindow(marker: Marker): View? {
                return null
            }

        })
        googleMap.setOnMarkerClickListener {marker->
            marker.showInfoWindow()
            true
        }
    }
    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        googleMap.isMyLocationEnabled = true
    }
    private fun placeMarkerAtCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        // If permission is granted, add a marker at the current location

//        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
//            if (location != null) {
//                val currentLatLng = LatLng(location.latitude, location.longitude)
//                googleMap.addMarker(
//                    MarkerOptions()
//                        .position(currentLatLng)
//                        .title("Current Location"))
//                googleMap.moveCamera(
//                    CameraUpdateFactory
//                        .newLatLngZoom(currentLatLng, DEFAULT_ZOOM))
//            }
//        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)

                // Open AddLocationFragment and pass the current location as an argument
                val addLocationFragment = AddLocationFragment()
                val args = Bundle()
                args.putParcelable("current_location", currentLatLng)
                addLocationFragment.arguments = args

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, addLocationFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val DEFAULT_ZOOM = 15f
    }


}
