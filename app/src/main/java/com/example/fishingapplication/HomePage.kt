package com.example.fishingapplication

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.fishingapplication.databinding.ActivityHomePageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class HomePage : AppCompatActivity() {

    private lateinit var binding: ActivityHomePageBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var uid: String
    private lateinit var user: User


    private lateinit var imageCircleMenu: CircleImageView
    private lateinit var username: TextView
    private lateinit var email: TextView


    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance();
        uid = firebaseAuth.currentUser?.uid.toString();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");


        val drawerLayout = binding.drawerLayout;
        val navView = binding.navigationView;

        val headerView = navView.getHeaderView(0);
        username = headerView.findViewById(R.id.name_menu)
        email = headerView.findViewById(R.id.email_menu)
        imageCircleMenu = headerView.findViewById(R.id.circleImage_navmenu)

        if (uid.isNotEmpty()) {
            getUserData()
        }


        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            navView.setCheckedItem(R.id.nav_home)
        }

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {

                R.id.nav_home -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment()).commit()

                R.id.nav_map -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, MapFragment()).commit()

                R.id.nav_logout -> logoutUser();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            true
        }


    }

    //    private fun getUserData() {
//        databaseReference.child(uid).addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                user = snapshot.getValue(User::class.java)!!
//                username.setText(user.username)
//                email.setText(firebaseAuth.currentUser?.email);
//                getUserProfilePicture()
//            }
//
//            override fun onCancelled(error: DatabaseError) {
////                Toast.makeText(this,"Something went wrong!",Toast.LENGTH_SHORT).show();
//                TODO("Not yet implemented")
//            }
//
//        })
//    }
    private fun getUserData() {
        databaseReference.child(uid).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                user = snapshot.getValue(User::class.java)!!
                username.text = user.username
                email.text = firebaseAuth.currentUser?.email
                getUserProfilePicture()
            } else {
                // Handle the case when the user data does not exist in the database
            }
        }.addOnFailureListener { error ->
            // Handle any error that occurs while fetching the data
        }
    }

    private fun getUserProfilePicture() {
        storageReference = FirebaseStorage.getInstance().reference.child("Profile/$uid")
        val localFile = File.createTempFile("tempImage", "jpeg")
        storageReference.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            imageCircleMenu.setImageBitmap(bitmap)
        }.addOnFailureListener {
            Toast.makeText(this, "Something went wrong with picture", Toast.LENGTH_SHORT).show()
        }

    }
//    override fun onBackPressed() {
//        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//            drawerLayout.closeDrawer(GravityCompat.START)
//        } else {
//            onBackPressedDispatcher.onBackPressed()
//        }
//    }

    private fun logoutUser() {
        firebaseAuth.signOut();
        startActivity(
            Intent(this, LoginActivity::class.java)
        )
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}