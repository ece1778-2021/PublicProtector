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

class SignUp : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = Firebase.auth

        // Get button ID
        val signUpButton: Button = findViewById(R.id.signupfinal_button)

        // Listen for clicks
        signUpButton.setOnClickListener { signUp() }
    }

    private fun signUp() {
        var editTextEmail = findViewById(R.id.new_email) as EditText
        var editTextPassword = findViewById(R.id.new_password) as EditText
        var editTextConfirmPassword = findViewById(R.id.confirm_password) as EditText
        var editTextUsername = findViewById(R.id.new_username) as EditText
        var editTextNumber = findViewById(R.id.phone_number) as EditText

        val newEmailString: String = editTextEmail.text.toString()
        val newPasswordString: String = editTextPassword.text.toString()
        val newComfirmPasswordString: String = editTextConfirmPassword.text.toString()
        val newUsernameString: String = editTextUsername.text.toString()
        val newNumber: String = editTextNumber.text.toString()

        if(newEmailString == "" || newPasswordString == "" || newUsernameString == "" || newNumber == "") {
            Toast.makeText(this, "Some information is missing", Toast.LENGTH_SHORT).show()
            return
        }

        if(newPasswordString != newComfirmPasswordString){
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
        }
        else {
            var db = Firebase.firestore

            val userData = hashMapOf(
                "email" to newEmailString,
                "username" to newUsernameString,
                "phone" to newNumber,
                "timer" to "30",
                "phrase" to "start the timer"
            )

            auth.createUserWithEmailAndPassword(newEmailString, newPasswordString)
                .addOnCompleteListener(this) {task ->
                   if (task.isSuccessful) {

                       Log.d("TAG", "createUserWithEmail:success")
                       val user = auth.currentUser

                       val userID: String = user?.uid ?: "nevergettinghere"

                       db.collection("users").document(userID)
                           .set(userData)
                           .addOnSuccessListener { Log.d("TAG", "GREAT SUCCESS") }
                           .addOnFailureListener { Log.d("TAG", "FALIURE") }

                       Toast.makeText(baseContext, "Signing you up",
                           Toast.LENGTH_SHORT).show()
                       val profileIntent = Intent(this, MainActivity::class.java)
                       startActivity(profileIntent)
                   } else {

                       Log.w("TAG", "createUserWithEmail:failure", task.exception)
                       Toast.makeText(baseContext, "Authentication failed.",
                           Toast.LENGTH_SHORT).show()
                   }

                }
        }
    }
}