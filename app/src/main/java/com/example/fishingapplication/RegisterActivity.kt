package com.example.fishingapplication

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException
import java.util.Date

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage : FirebaseStorage
    private lateinit var selectedImg : Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        val buttonRegister = findViewById<Button>(R.id.registerbutton_register);
        val alreadyHaveAccount = findViewById<TextView>(R.id.already_have_account);
        val userImage = findViewById<CircleImageView>(R.id.imageView);

        userImage.setOnClickListener{
            val intent = Intent();
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent,1)
        }
        buttonRegister.setOnClickListener {

            val username = findViewById<EditText>(R.id.username_edittext_register).text.toString();
            val email = findViewById<EditText>(R.id.email_edittext_register).text.toString();
            val password = findViewById<EditText>(R.id.password_edittext_register).text.toString();

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this,"Please insert email and passowrd",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, email, Toast.LENGTH_SHORT).show()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Main", "createUserWithEmail:success")
                        Toast.makeText(baseContext, "Authentication success.", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this,HomePage::class.java)
                        startActivity(intent);
                        finish()
//                        saveUserToFirebaseDatabase(username);
                        uploadData(username);
                        val user = auth.currentUser

//                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Main", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
//                        updateUI(null)
                    }
                }
        }
        alreadyHaveAccount.setOnClickListener{
            Log.d("MainActivity","Try to show login");

            val intent = Intent(this,LoginActivity::class.java);
            startActivity(intent)
        }

    }

    private fun uploadData(username: String) {
        val reference = storage.reference.child("Profile").child(auth.currentUser?.uid.toString())
        reference.putFile(selectedImg).addOnCompleteListener{
            if(it.isSuccessful){
                reference.downloadUrl.addOnSuccessListener { task->
                    uploadInfo(username,task.toString())
                }
            }
        }
    }

    private fun uploadInfo(username: String,imgUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, username,imgUrl )
        ref.setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this,"Successfullt added to database",Toast.LENGTH_SHORT).show();
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val userImage = findViewById<CircleImageView>(R.id.imageView);
        if(data!=null){
            if(data.data != null) {
                selectedImg = data.data!!
                userImage.setImageURI(selectedImg)
                val textToGoAway = findViewById<TextView>(R.id.uploadimage_textview_register)
                textToGoAway.visibility = View.GONE
            }
        }
    }
}