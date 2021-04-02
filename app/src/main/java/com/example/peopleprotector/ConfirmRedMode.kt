package com.example.peopleprotector

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ConfirmRedMode : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_red_mode)
        val confirmRedMode: Button = findViewById(R.id.confirm_red_mode)
        val cancelRedMode: Button = findViewById(R.id.cancel_red_mode)


        confirmRedMode.setOnClickListener {
            var intent = Intent(this, AmberMode::class.java)
            startActivity(intent)
        }

        cancelRedMode.setOnClickListener {
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}