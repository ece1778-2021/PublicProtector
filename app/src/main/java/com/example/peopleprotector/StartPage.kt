package com.example.peopleprotector

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class StartPage : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_page)
        auth = Firebase.auth

        // Get the buttons
        val loginButton: Button = findViewById(R.id.login_button)
        val signupButton: Button = findViewById(R.id.signup_button)

        // Listen for clicks
        loginButton.setOnClickListener { login() }
        signupButton.setOnClickListener { singUpPage() }
    }

    public override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if(currentUser != null) {
            val db = Firebase.firestore

            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { result ->
                    val profileIntent = Intent(this, MainActivity::class.java)
                    startActivity(profileIntent)
                }
        }
    }

    private fun singUpPage() {
        val singUpIntent = Intent(this, SignUp::class.java)
        startActivity(singUpIntent)
    }

    private fun login() {
        var editTextLoginEmail = findViewById<EditText>(R.id.email)
        var editTextLoginPassword = findViewById<EditText>(R.id.password)

        val loginEmail: String = editTextLoginEmail.text.toString()
        val loginPassword: String = editTextLoginPassword.text.toString()

        if(loginEmail == "" || loginPassword == "") {
            return
        }

        auth.signInWithEmailAndPassword(loginEmail, loginPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "signInWithEmail:success")
                    val user = auth.currentUser
                    val profileIntent: Intent = Intent(this, MainActivity::class.java)
                    startActivity(profileIntent)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    // ...
                }

                // ...
            }
    }
}