package com.example.fishingapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage : FirebaseStorage
    private lateinit var selectedImg : Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
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
                .addOnCompleteListener(RegisterActivity()) { task ->
                    if (task.isSuccessful) {
                        Log.d("Main", "createUserWithEmail:success")
                        Toast.makeText(this, "Authentication success.", Toast.LENGTH_SHORT).show()

                        uploadData(username,email);

                    } else {
                        Log.w("Main", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        alreadyHaveAccount.setOnClickListener{
            Log.d("MainActivity","Try to show login");

            val intent = Intent(this,LoginActivity::class.java);
            startActivity(intent)
            finish()
        }

    }

    private fun uploadData(username: String,email :String) {
        val reference = storage.reference.child("Profile").child(auth.currentUser?.uid.toString())
        reference.putFile(selectedImg).addOnCompleteListener{
            if(it.isSuccessful){
                reference.downloadUrl.addOnSuccessListener { task->
                    uploadInfo(username,email,task.toString())

                }
            }

        }
    }

    private fun uploadInfo(username: String,email:String,imgUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, username,email,imgUrl )
        ref.setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this,"Successfullt added to database",Toast.LENGTH_SHORT).show();
                val intent = Intent(this,LoginActivity::class.java)
                startActivity(intent)
                finish();
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