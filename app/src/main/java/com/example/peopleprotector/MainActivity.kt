package com.example.peopleprotector

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
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
    private lateinit var auth: FirebaseAuth
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lat = ""
    private var lon = ""
    private var username = ""
    public var MY_PERMISSIONS_REQUEST_LOCATION: Int = 99
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = Firebase.auth
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        var db = Firebase.firestore
        val user = auth.currentUser
        val userID: String = user?.uid ?: "WillNotGetHere"
        //val intent = Intent(this, SpeechToText::class.java)
        //startActivity(intent)
        val timerStart: Button = findViewById(R.id.timerTrigger)
        val notifStart: Button = findViewById(R.id.notificationTrigger)
        val settingsButton: ImageButton = findViewById(R.id.settingsButton)
        val logoutButton: Button = findViewById(R.id.logout_button)
        //val gmmIntentUri = Uri.parse("geo:0,0?q=37.7749,-122.4194(ALERT)")
        //val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        //mapIntent.setPackage("com.google.android.apps.maps")
        //startActivity(mapIntent)
        Toast.makeText(this, "Getting ready...", Toast.LENGTH_LONG).show()
        checkLocationPermission()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //Toast.makeText(baseContext, "Location Permission Denied", Toast.LENGTH_SHORT).show()
            return
        }
        fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    lat = location?.latitude.toString()
                    lon = location?.longitude.toString()
                    //Toast.makeText(baseContext, lat + lon, Toast.LENGTH_LONG).show()
                }


        //notifyContactsButton.setOnClickListener {
        //    // Get the names of the contacts and then notify them with their subscriptions
        //    db.collection("connections")
        //            .get().addOnSuccessListener { result ->
        //                for(document in result) {
        //                    val uid = document.data.get("uid") as String
        //                    val contact = document.data.get("contact") as String

        //                    if(uid == userID) {
        //                        val topic = "/topics/" + contact
        //                        sendMessage(topic)
        //                    }
        //                }
        //            }

        //}

        timerStart.setOnClickListener{
            val intent = Intent(this, SpeechToText::class.java)
            startActivity(intent)
        }

        notifStart.setOnClickListener {
            val intent = Intent(this, ConfirmRedMode::class.java)
            startActivity(intent)
        }

        settingsButton.setOnClickListener{
            val intent = Intent(this, settings::class.java)
            startActivity(intent)
        }
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, StartPage::class.java)
            startActivity(intent)
        }


        var res = FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Andrei, this is the token that the Firebase produces

            // Log and toast
            //val msg = getString(R.string.msg_token_fmt, token)
            val msg = token;
            if (msg != null) {
                Log.d(TAG, msg)
            }
            // Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })
        Firebase.messaging.subscribeToTopic("protect")
                .addOnCompleteListener { task ->
                    var msg = "Messaging Enabled"
                    if (!task.isSuccessful) {
                        msg = "Messaging Disabled"
                    }
                    Log.d(TAG, msg)
                    //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                }


        // Do the firebase stuff
        val docRef = db.collection("users").document(userID)
        docRef.get()
            .addOnSuccessListener { document ->
                if(document != null) {
                    val number: String? = document.data?.get("phone") as String?
                    val email: String? = document.data?.get("email") as String?
                    username = (document.data?.get("username") as String?).toString()

                    // Decide what to do here
                    // Subscribe to my own email

                    if (username != null) {
                        Firebase.messaging.subscribeToTopic(username)
                            .addOnCompleteListener { task ->
                                var msg = "Firebase Connection Successful"
                                if (!task.isSuccessful) {
                                    msg = "Firebase Connection Failed"
                                }
                                Log.d(TAG, msg)
                                //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                            }
                    }

                }
            }

        //Unsibscribe from users that aren't me
        val docRef2 = db.collection("users")
                .get()
                .addOnSuccessListener { result ->
                    for(document in result) {
                        val name = document.data.get("username") as String
                        if(name != username) {
                            Firebase.messaging.unsubscribeFromTopic(name)
                                    .addOnCompleteListener {task ->

                                        var msg = "you have unsubbed"
                                        if (!task.isSuccessful) {
                                            msg = "You have not unsubbec"
                                        }
                                        Log.d(TAG, msg)
                                        //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                                    }
                        }
                    }
                }
    }

    private fun move2Timer(){
        val intent: Intent = Intent(this, Timer::class.java)
        startActivity(intent)
    }

    private fun goToSettings() {
        val intent = Intent(this, settings::class.java)
        startActivity(intent)
    }


    private fun sendEmail() {
        /*
        val username = "ece1778publicprotector@gmail.com"
        val password = "publicprotector"

        var props: Properties = Properties()
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "587");
        props.put("mail.smtp.starttls.enabled", "true")
        props.put("mail.smtp.starttls.required", "true")
        props.put("mail.smtp.socketFactory.class",
            "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        val session = Session.getInstance(props,
                object : javax.mail.Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(username, password)
                    }
                })
        try {
            val message = MimeMessage(session)
            message.setFrom(InternetAddress(username))
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("amertens114@gmail.com"))
            message.subject = "Cool"
            message.setText("Test")

            //var messageBodyPart = MimeBodyPart()
            Transport.send(message, username, password)
        }
        catch (e: MessagingException) {
            e.printStackTrace()
        }*/

        /*var emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, "amertens114@gmail.com");
        emailIntent.putExtra(Intent.EXTRA_CC, "");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Your subject");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Email message goes here");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."))
            finish()
            Log.i("Finished sending email", "")
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                this@MainActivity,
                "There is no email client installed.",
                Toast.LENGTH_SHORT
            ).show()
        }*/

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
                    //Toast.makeText(this@MainActivity, "Request error", Toast.LENGTH_LONG).show()
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
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //fusedLocationClient.removeLocationUpdates(fusedLocationClient.)
                }
            }
        }
    }

    companion object {

        private const val TAG = "MainActivity"
    }

}
