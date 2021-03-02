package com.example.peopleprotector

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val intent = Intent(this, SpeechToText::class.java)
        startActivity(intent)
        //val timerStart: Button = findViewById(R.id.timerTrigger)


        //timerStart.setOnClickListener{
        //    move2Timer()
        //}
    }

    private fun move2Timer(){
        val intent: Intent = Intent(this, Timer::class.java)
        startActivity(intent)
    }

}
