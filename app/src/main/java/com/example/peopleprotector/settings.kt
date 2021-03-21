package com.example.peopleprotector

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class settings : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        auth = Firebase.auth
        var db = Firebase.firestore
        val user = auth.currentUser
        val userID: String = user?.uid ?: "WillNotGetHere"

        // Get the fields from firestore
        var number = ""
        var username = ""
        var email = ""
        var timer = ""
        var docRef = db.collection("users").document(userID)
        docRef.get()
            .addOnSuccessListener { document ->
                if(document != null) {
                    number = (document.data?.get("phone") as String?).toString()
                    username = (document.data?.get("username") as String?).toString()
                    email = (document.data?.get("email") as String?).toString()
                    timer = (document.data?.get("timer") as String?).toString()

                    findViewById<EditText>(R.id.timer).setText(timer)

                }
            }

        // Populate the recyclerview
        val list = mutableListOf<String>()
        val recyclerView: RecyclerView = findViewById(R.id.userrecycle)
        val numCols = 1
        recyclerView.layoutManager = GridLayoutManager(this, numCols)
        var adapter = CustomAdapter(list)
        recyclerView.adapter = adapter

        var res3 = db.collection("connections")
                .get()
                .addOnSuccessListener { result ->
                    val adapter: CustomAdapter = recyclerView.adapter as CustomAdapter
                    for(document in result){
                        val uid = document.data.get("uid") as String
                        val userToAdd = document.data.get("contact") as String

                        if(uid == userID) adapter.addItem(userToAdd)
                    }
                }

        val saveButton: Button = findViewById(R.id.save_button)
        val addButton: Button = findViewById(R.id.add_button)
        val deleteButton: Button = findViewById(R.id.delete_button)
        var timerText = findViewById<EditText>(R.id.timer).text.toString()
        saveButton.setOnClickListener {
            timerText = findViewById<EditText>(R.id.timer).text.toString()
            var userData = hashMapOf(
                "phone" to number,
                "username" to username,
                "email" to email,
                "timer" to timerText
            )

            var res = db.collection("users").document(userID)
                .set(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Saved your settings", Toast.LENGTH_SHORT).show()
                }
            while(!res.isComplete) {
            }

            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        addButton.setOnClickListener {
            var newUserText = findViewById<EditText>(R.id.user_to_do).text.toString()
            var userData = hashMapOf(
                "uid" to userID,
                "contact" to newUserText
            )

            var res6 = db.collection("connections").add(userData)
                .addOnSuccessListener {

                    val adapter: CustomAdapter = recyclerView.adapter as CustomAdapter
                    adapter.removeItem(newUserText)
                }
            while(!res6.isComplete){}
            var intent = Intent(this, settings::class.java)
            startActivity(intent)
        }

        deleteButton.setOnClickListener {
            var toDelete = "";
            var newUserText = findViewById<EditText>(R.id.user_to_do).text.toString()
            var res = db.collection("connections")
                .get()
                .addOnSuccessListener { result ->
                    for(document in result){
                        val uid = document.data.get("uid") as String
                        val userToRemove = document.data.get("contact") as String
                        if(uid == userID && userToRemove == newUserText) {
                            toDelete = document.id
                        }
                    }
                    Toast.makeText(this, toDelete, Toast.LENGTH_SHORT).show()
                    if(toDelete != "") {
                        db.collection("connections").document(toDelete).delete()
                        val adapter: CustomAdapter = recyclerView.adapter as CustomAdapter
                        adapter.removeItem(toDelete)
                    }
                }
            while(!res.isComplete) {}
            var intent = Intent(this, settings::class.java)
            startActivity(intent)
        }

    }
}