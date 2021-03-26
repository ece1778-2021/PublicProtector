package com.example.peopleprotector

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class AmberMode : AppCompatActivity(), TextToSpeech.OnInitListener {
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(this.applicationContext)
    }
    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey =
            "key=" + "AAAAEQ8EzaI:APA91bGZxnnKNMJVTLjrUDmucUqeangvBYFNHBzey-f1ixWEiC7Ko0XPerBj79RjLo8sb2wWOoAwJGkNztAgT_1sERtEgXMnRXxRZosl4gMDvaSQ3U8ktuPxdAZ9Oen93vQ9DBdB_qXY"
    private val contentType = "application/json"
    public var MY_PERMISSIONS_REQUEST_LOCATION: Int = 99
    private var lat = ""
    private var lon = ""
    private var username = ""
    private lateinit var auth: FirebaseAuth
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var tts: TextToSpeech
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_amber_mode)
        tts = TextToSpeech(this, this)
        auth = Firebase.auth
        val user = auth.currentUser
        val userID: String = user?.uid ?: "WillNotGetHere"
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(baseContext, "nopermission", Toast.LENGTH_SHORT).show()
            return
        }
        var res7 = fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    lat = location?.latitude.toString()
                    lon = location?.longitude.toString()
                    Toast.makeText(baseContext, lat + lon, Toast.LENGTH_LONG).show()
                    var db = Firebase.firestore
                    val docRef = db.collection("users").document(userID)
                    var res8 = docRef.get()
                            .addOnSuccessListener { document ->
                                if(document != null) {
                                    val number: String? = document.data?.get("phone") as String?
                                    val email: String? = document.data?.get("email") as String?
                                    username = (document.data?.get("username") as String?).toString()
                                    sendMessage("/topics/protect")
                                }
                            }

                }


        var backButton: Button = findViewById(R.id.back)
        backButton.setOnClickListener {
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    private fun sendMessage(topic: String) {
        var db = Firebase.firestore
        val notification = JSONObject()
        val notificationBody = JSONObject()

        notificationBody.put("message", username + "!!" + lat + "!!" + lon)
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
    public fun checkLocationPermission(): Boolean {
        if(ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)){
                AlertDialog.Builder(this)
                        .setTitle("give me location")
                        .setMessage("give me location")
                        .setPositiveButton("plese", DialogInterface.OnClickListener { dialog, which ->
                            // Might need to change this
                            ActivityCompat.requestPermissions(this,
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_LOCATION)
                        })
                        .create()
                        .show()

            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_LOCATION)
            }
            return false
        } else {
            return true
        }
    }

    public override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //fusedLocationClient.removeLocationUpdates(fusedLocationClient.)
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set US English as language for tts/
            val result = tts!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language specified is not supported!")
            } else {
                tts.speak("Human rights standard and practice for the police. Everyone has the right to security of the person.", TextToSpeech.QUEUE_FLUSH, null, "")
            }

        } else {
            Log.e("TTS", "Initilization Failed!")
        }

    }
    public override fun onDestroy() {
        // Shutdown TTS
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }
}