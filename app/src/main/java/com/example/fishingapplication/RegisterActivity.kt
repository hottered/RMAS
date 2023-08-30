package com.example.fishingapplication

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var selectedImg: Uri

    private val REQUEST_IMAGE_GALLERY = 132
    private val REQUEST_IMAGE_CAMERA = 142

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        val loading = LoadingDialog(this)

        val permission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                1
            )
        }

        val buttonRegister = findViewById<Button>(R.id.registerbutton_register);
        val alreadyHaveAccount = findViewById<TextView>(R.id.already_have_account);
        val userImage = findViewById<CircleImageView>(R.id.imageView);

        userImage.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select image from ?")
            builder.setMessage("Choose your option?")
            builder.setPositiveButton("Gallery") { dialog, which ->
                dialog.dismiss()

                val intent = Intent();
                intent.action = Intent.ACTION_PICK
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_IMAGE_GALLERY)
            }
            builder.setNegativeButton("Camera") { dialog, which ->
                dialog.dismiss()

                Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE
                ).also { takePictureIntent ->
                    takePictureIntent.resolveActivity(packageManager)?.also {
                        val permission = ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.CAMERA
                        )
                        if (permission != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(android.Manifest.permission.CAMERA),
                                1
                            )

                        } else {
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAMERA)
                        }
                    }
                }

            }
            val dialog: AlertDialog = builder.create()
            dialog.show()

        }
        buttonRegister.setOnClickListener {


            val username = findViewById<EditText>(R.id.username_edittext_register).text.toString();
            val email = findViewById<EditText>(R.id.email_edittext_register).text.toString();
            val password = findViewById<EditText>(R.id.password_edittext_register).text.toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please insert email and passowrd", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loading.startLoading()

            Toast.makeText(this, email, Toast.LENGTH_SHORT).show()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity()) { task ->
                    if (task.isSuccessful) {
                        Log.d("Main", "createUserWithEmail:success")
                        Toast.makeText(this, "Authentication success.", Toast.LENGTH_SHORT).show()
                        uploadData(username, email, loading);
                    } else {
                        Log.w("Main", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT)
                            .show()
//                        loading.isDismiss()
                    }
                }
        }
        alreadyHaveAccount.setOnClickListener {
            Log.d("MainActivity", "Try to show login");

            val intent = Intent(this, LoginActivity::class.java);
            startActivity(intent)
            finish()
        }

    }

    private fun uploadData(username: String, email: String, loadingDialog: LoadingDialog) {
        val reference = storage.reference.child("Profile").child(auth.currentUser?.uid.toString())
        reference.putFile(selectedImg).addOnCompleteListener {
            if (it.isSuccessful) {
                reference.downloadUrl.addOnSuccessListener { task ->
                    uploadInfo(username, email, task.toString(), loadingDialog)

                }
            }

        }
    }

    private fun uploadInfo(
        username: String,
        email: String,
        imgUrl: String,
        loadingDialog: LoadingDialog
    ) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, username, email, imgUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Successfullt added to database", Toast.LENGTH_SHORT).show();
                loadingDialog.isDismiss()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish();
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val userImage = findViewById<CircleImageView>(R.id.imageView);
        if (data != null && requestCode == REQUEST_IMAGE_GALLERY) {
            if (data.data != null) {
                selectedImg = data.data!!
                userImage.setImageURI(selectedImg)
            }
        }
        else if(data!= null && requestCode == REQUEST_IMAGE_CAMERA){
            val imageBitmap = data.extras?.get("data") as Bitmap
            selectedImg = getImageUriFromBitmap(imageBitmap)
            userImage.setImageURI(selectedImg)
        }
        else{
            Toast.makeText(this,"Something went wrong",Toast.LENGTH_SHORT).show()
        }
    }
    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap,
            "Title",
            null
        )
        return Uri.parse(path)
    }
}