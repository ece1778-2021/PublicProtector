package com.example.peopleprotector

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MoveToMap : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_move_to_map)
        var info = intent.getStringExtra("package")
        var items = info?.split("!!")
        var name = items?.get(0)
        var lat = items?.get(1)
        var lon = items?.get(2)

        var message = "User with name " + name + " is in trouble at latitude " + lat +" and longitude " + lon

        var alertMessageView = findViewById<TextView>(R.id.alertMessage)
        var mapButton: Button = findViewById(R.id.mapbutton)
        alertMessageView.text = message


        mapButton.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:0,0?q=" + lat + "," + lon + "(ALERT)")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }

    }
}