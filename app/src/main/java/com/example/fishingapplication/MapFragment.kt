package com.example.fishingapplication

//import kotlinx.coroutines.tasks.await
import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var btnPlaceMarker: FloatingActionButton
    private lateinit var btnFilters: Button


    private lateinit var selectedSpecies: ArrayList<String>
    private lateinit var selectedUsers: ArrayList<String>

    private lateinit var markerList: ArrayList<Marker>

    private var dateStart: Long = 0L
    private var dateEnd: Long = 0L

    private var radiusFilter: Double = 0.0

    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        mapView = view.findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        btnPlaceMarker = view.findViewById(R.id.btn_place_marker)
        btnFilters = view.findViewById(R.id.button_filters_map)
        searchView = view.findViewById(R.id.searchView)

        selectedUsers = arguments?.getStringArrayList("selectedUsers") ?: ArrayList()
        selectedSpecies = arguments?.getStringArrayList("selectedSpecies") ?: ArrayList()
        dateStart = arguments?.getLong("dateStart") ?: 0L
        dateEnd = arguments?.getLong("dateEnd") ?: 0L


        radiusFilter = arguments?.getDouble("filterRadius") ?: 0.0

        Log.d("FiltersFromMap", selectedSpecies.toString())
        Log.d("FiltersFromMap", selectedUsers.toString())
        Log.d("FiltersFromMap", dateStart.toString())
        Log.d("FiltersFromMap", dateEnd.toString())
//        Log.d("FiltersFromMap", radiusFilter.toString())


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    Log.d("MarkerSearch", query)
                    Log.d("marker1is", markerList[0].title.toString())
                    searchAndFocusMarkerByTitle(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false

            }
        })

        btnPlaceMarker.setOnClickListener {
            placeMarkerAtCurrentLocation()
        }

        btnFilters.setOnClickListener {
            val filtersFragment = FilterFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, filtersFragment)
                .addToBackStack(null)
                .commit()
        }


        return view
    }

    override fun onMapReady(map: GoogleMap) {

        googleMap = map
        enableMyLocation()
        fetchMarkersFromFirebase()

    }

    private fun fetchMarkersFromFirebase() {

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        markerList = ArrayList()

        Log.d("MapFragmentGledam", "fetchMarkersHere")

        val databaseReference = FirebaseDatabase.getInstance().getReference("markers")


        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                var currentLatLng = LatLng(location.latitude, location.longitude)

                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (markerSnapshot in snapshot.children) {

                            val markerData = markerSnapshot.getValue(MarkerData::class.java)

                            if (markerData != null && markerData.latitude != null && markerData.longitude != null) {
                                val markerLatLng = LatLng(markerData.latitude, markerData.longitude)
                                if (
                                    (selectedSpecies.isNullOrEmpty() || selectedSpecies.contains(
                                        markerData.commonSpecie
                                    ))
                                    && (selectedUsers.isNullOrEmpty() || selectedUsers.contains(
                                        markerData.user?.username
                                    ))
                                    && (dateStart == 0L || dateEnd == 0L || ((markerData.createdAtUtc!! > dateStart) && (markerData.createdAtUtc!! < dateEnd)))
                                    && (radiusFilter == 0.0 || isMarkerInRadiusInKilometers(
                                        markerLatLng,
                                        currentLatLng,
                                        radiusFilter
                                    ))
                                ) {
                                    val marker = googleMap.addMarker(
                                        MarkerOptions()
                                            .position(markerLatLng)
                                            .title(markerData.title)
                                    )
                                    marker?.snippet =
                                        "Rating: ${markerData.rating ?: "Not rated yet"}"
                                    if (marker != null) {
                                        markerList.add(marker)
                                    }
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("MapFragment", "Failed to fetch markers: ${error.message}")
                    }
                })

                googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
                    override fun getInfoContents(marker: Marker): View {
                        val view = layoutInflater.inflate(R.layout.marker_info_contents, null)
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
                googleMap.setOnMarkerClickListener { marker ->
                    marker.showInfoWindow()

                    true
                }
                googleMap.setOnInfoWindowClickListener { marker->
                    Log.d("Kliknuo sam na marker ",marker.position.latitude.toString())

                    val locationFragment = LocationFragment()
                    val args = Bundle()
                    args.putDouble("locationFragmentLatitude", marker.position.latitude)
                    args.putDouble("locationFragmentLongitude", marker.position.longitude)
                    locationFragment.arguments = args

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, locationFragment)
                        .addToBackStack(null)
                        .commit()
                }
                googleMap.addCircle(
                    CircleOptions()
                        .center(currentLatLng)
                        .radius(radiusFilter * 1000)
                        .strokeWidth(2f)
                        .strokeColor(Color.BLUE)
                        .fillColor(Color.parseColor("#500084d3"))
                )

            }

        }


    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
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


        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)

                // Open AddLocationFragment and pass the current location as an argument
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        currentLatLng,
                        DEFAULT_ZOOM
                    )
                )
//                googleMap.addCircle(
//                    CircleOptions()
//                    .center(currentLatLng)
//                    .radius(50.0)
//                    .strokeWidth(2f)
//                    .strokeColor(Color.BLUE)
//                    .fillColor(Color.parseColor("#500084d3")))

            }

        }
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

        googleMap.isMyLocationEnabled = true

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

    private fun searchAndFocusMarkerByTitle(title: String) {
        var flag = 0
        for (marker in markerList) {
            if (marker.title == title) {
                val markerPosition = marker.position
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        markerPosition,
                        DEFAULT_ZOOM
                    )
                )
                marker.showInfoWindow()
                flag = 1
                break
            }
        }
        if (flag == 0) {
            val alertDialogBuilder = AlertDialog.Builder(requireContext())
            alertDialogBuilder.setMessage("There is no location with a title ${title}")
            alertDialogBuilder.setPositiveButton("OK") { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
            }

            val alertDialog: AlertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

    }

    private fun calculateDistanceInKilometers(latLng1: LatLng, latLng2: LatLng): Double {
        val results = FloatArray(1)
        Location.distanceBetween(
            latLng1.latitude,
            latLng1.longitude,
            latLng2.latitude,
            latLng2.longitude,
            results
        )
        val distanceInMeters = results[0]
        return distanceInMeters / 1000.0 // Convert meters to kilometers
    }

    private fun isMarkerInRadiusInKilometers(
        markerLatLng: LatLng,
        userLatLng: LatLng,
        radiusKm: Double
    ): Boolean {
        val distanceKm = calculateDistanceInKilometers(markerLatLng, userLatLng)
        return distanceKm <= radiusKm
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val DEFAULT_ZOOM = 15f
    }


}
