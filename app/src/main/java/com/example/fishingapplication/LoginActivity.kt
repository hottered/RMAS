package com.example.fishingapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fishingapplication.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance();

//        if(firebaseAuth.currentUser != null)
//        {
//            val intent = Intent(this,HomePage::class.java)
//            startActivity(intent);
//            finish()
//        }

        binding.gotosignupTextview.setOnClickListener{
            startActivity(
                Intent(this,RegisterActivity::class.java)
            )
            finish()
        }
        binding.loginbuttonLogin.setOnClickListener {
            val email = binding.emailEdittextLogin.text.toString();
            val password  = binding.passwordEdittextLogin.text.toString();

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this,"Please enter email and password",Toast.LENGTH_SHORT).show();
                return@setOnClickListener
            }
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Login:", "signInWithEmail:success")
                        val intent = Intent(this,HomePage::class.java)
                        startActivity(intent);
                        finish()
//                        val user = auth.currentUser
//                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Login:", "signInWithEmail:failure", task.exception)
                        Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT,).show()
//                        updateUI(null)
                    }
                }
        }
    }

    override fun onStart() {
        super.onStart()


        if(firebaseAuth.currentUser != null)
        {
            Toast.makeText(this,"Ovde sam u login",Toast.LENGTH_SHORT).show();
            val intent = Intent(this,HomePage::class.java)
            startActivity(intent);
            finish()
        }
    }
}