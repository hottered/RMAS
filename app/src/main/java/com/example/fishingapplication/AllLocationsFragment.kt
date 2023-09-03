package com.example.fishingapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AllLocationsFragment : Fragment() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var locationsRecyclerView: RecyclerView
    private lateinit var locationsArrayList: ArrayList<MarkerData>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_all_locations, container, false)

        locationsRecyclerView = view.findViewById(R.id.locationsList)
        locationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        locationsRecyclerView.setHasFixedSize(true)

        locationsArrayList = arrayListOf<MarkerData>()

        fetchMarkersFromDatabase()

        return view
    }
    private fun fetchMarkersFromDatabase(){

        databaseReference = FirebaseDatabase.getInstance().getReference("markers")

        databaseReference.addValueEventListener(object  : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                locationsArrayList.clear()

                if(snapshot.exists()){
                    for(locationsSnapshot in snapshot.children){

                        val location = locationsSnapshot.getValue(MarkerData::class.java)

                        locationsArrayList.add(location!!)

                    }
                    locationsArrayList.sortByDescending {
                        it.rating
                    }
                    val adapter = MyAdapter(locationsArrayList)
                    locationsRecyclerView.adapter = adapter
                    adapter.setOnItemClickListener(object:MyAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            Toast.makeText(requireContext(),"Clicked ${locationsArrayList[position].title}",Toast.LENGTH_SHORT).show()


                            val locationFragment = LocationFragment()
                            val args = Bundle()
                            args.putDouble("locationFragmentLatitude", locationsArrayList[position].latitude ?: 0.0)
                            args.putDouble("locationFragmentLongitude", locationsArrayList[position].longitude ?: 0.0)
                            locationFragment.arguments = args
                            val fragmentManager = requireActivity().supportFragmentManager
                            fragmentManager.beginTransaction()
                                .replace(
                                    R.id.fragment_container,
                                    locationFragment
                                ) // R.id.fragment_container should be the ID of the container where you want to display the MapFragment
                                .addToBackStack(null) // Add this transaction to the back stack
                                .commit()
                        }

                    })
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })

    }
    companion object {
    }
}