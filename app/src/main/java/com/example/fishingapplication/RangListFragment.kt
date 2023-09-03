package com.example.fishingapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RangListFragment : Fragment() {

    private lateinit var databaseReference: FirebaseDatabase
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var usersArrayList: ArrayList<User>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_rang_list, container, false)

        usersRecyclerView = view.findViewById(R.id.recycler_view_ranglist)
        usersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        usersRecyclerView.setHasFixedSize(true)

        usersArrayList = arrayListOf<User>()

        databaseReference = FirebaseDatabase.getInstance()
        fetchUsersFromDatabase()

        return view

    }

    private fun fetchUsersFromDatabase() {
        val usersFromFirebase = databaseReference.getReference("users")
        usersFromFirebase.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                usersArrayList.clear();
                if(snapshot.exists()){
                    for (userSnapshot in snapshot.children){
                        val user = userSnapshot.getValue(User::class.java)
                        usersArrayList.add(user!!)
                    }
                    usersArrayList.sortByDescending {
                        it.score
                    }
                    val adapter = RangListAdapter(usersArrayList)
                    usersRecyclerView.adapter = adapter
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