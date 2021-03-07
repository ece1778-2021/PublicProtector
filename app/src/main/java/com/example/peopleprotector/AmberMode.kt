package com.example.peopleprotector

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class AmberMode : AppCompatActivity() {
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(this.applicationContext)
    }
    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey =
            "key=" + "AAAAEQ8EzaI:APA91bGZxnnKNMJVTLjrUDmucUqeangvBYFNHBzey-f1ixWEiC7Ko0XPerBj79RjLo8sb2wWOoAwJGkNztAgT_1sERtEgXMnRXxRZosl4gMDvaSQ3U8ktuPxdAZ9Oen93vQ9DBdB_qXY"
    private val contentType = "application/json"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_amber_mode)
        sendMessage()
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
                    Toast.makeText(this@AmberMode, "Request error", Toast.LENGTH_LONG).show()
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
}