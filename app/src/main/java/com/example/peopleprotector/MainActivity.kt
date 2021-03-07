package com.example.peopleprotector

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(this.applicationContext)
    }
    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey =
            "key=" + "AAAAEQ8EzaI:APA91bGZxnnKNMJVTLjrUDmucUqeangvBYFNHBzey-f1ixWEiC7Ko0XPerBj79RjLo8sb2wWOoAwJGkNztAgT_1sERtEgXMnRXxRZosl4gMDvaSQ3U8ktuPxdAZ9Oen93vQ9DBdB_qXY"
    private val contentType = "application/json"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //val intent = Intent(this, SpeechToText::class.java)
        //startActivity(intent)
        val timerStart: Button = findViewById(R.id.timerTrigger)
        val notifStart: Button = findViewById(R.id.notificationTrigger)


        timerStart.setOnClickListener{
            val intent = Intent(this, SpeechToText::class.java)
            startActivity(intent)
            //move2Timer()
            //sendMessage()
        }
        notifStart.setOnClickListener {
            sendMessage()
        }

        var res = FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            //val msg = getString(R.string.msg_token_fmt, token)
            val msg = token;
            if (msg != null) {
                Log.d(TAG, msg)
            }
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })

        Firebase.messaging.subscribeToTopic("protect")
                .addOnCompleteListener { task ->
                    var msg = "You did it"
                    if (!task.isSuccessful) {
                        msg = "You didn't do it"
                    }
                    Log.d(TAG, msg)
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                }
    }

    private fun move2Timer(){
        val intent: Intent = Intent(this, Timer::class.java)
        startActivity(intent)
    }

    private fun sendMessage() {
        val topic = "/topics/protect"
        val notification = JSONObject()
        val notificationBody = JSONObject()

        notificationBody.put("title", "Alert!")
        notificationBody.put("message", "There is somebody in trouble near you!")
        notification.put("to", topic)
        notification.put("data", notificationBody)
        sendNotification(notification)
    }

    private fun sendNotification(notification: JSONObject) {
        Log.e("TAG", "sendNotification")
        val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
                Response.Listener<JSONObject> { response ->
                    Log.i("TAG", "onResponse: $response")
                    //msg.setText("")
                },
                Response.ErrorListener {
                    Toast.makeText(this@MainActivity, "Request error", Toast.LENGTH_LONG).show()
                    Log.i("TAG", "onErrorResponse: Didn't work")
                }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = serverKey
                params["Content-Type"] = contentType
                return params
            }
        }
        requestQueue.add(jsonObjectRequest)
    }

    companion object {

        private const val TAG = "MainActivity"
    }
}
