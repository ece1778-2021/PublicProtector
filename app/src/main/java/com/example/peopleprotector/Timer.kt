package com.example.peopleprotector

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Timer : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        val redModeStart: Button = findViewById(R.id.redModeButton)
        val cancelButton: Button = findViewById(R.id.cancelButton)
        val textTimer: TextView = findViewById(R.id.timer)
        val time = 30000L

        val timer = object : CountDownTimer(time, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                var timeLeft = millisUntilFinished/1000
                textTimer.text = ("${timeLeft}")
            }

            override fun onFinish() {
                // call here other methods from activity
                move2RedMode()
            }
        }
        timer.start()


        cancelButton.setOnClickListener{
            timer.cancel()
            onBackPressed()
        }

        redModeStart.setOnClickListener{
            timer.cancel()
            move2RedMode()
        }

    }

    private fun move2RedMode(){
        val red: Intent = Intent(this, AmberMode::class.java)
        startActivity(red)
    }
}