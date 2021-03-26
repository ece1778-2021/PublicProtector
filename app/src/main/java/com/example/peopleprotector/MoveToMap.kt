package com.example.peopleprotector

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MoveToMap : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    public var MY_PERMISSIONS_REQUEST_LOCATION: Int = 99
    private var myLat = ""
    private var myLon = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_move_to_map)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        var info = intent.getStringExtra("package")
        var items = info?.split("!!")
        var name = items?.get(0)
        var lat = items?.get(1)
        var lon = items?.get(2)
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
        fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    myLat = location?.latitude.toString()
                    myLon = location?.longitude.toString()
                    Toast.makeText(baseContext, myLat + myLon, Toast.LENGTH_LONG).show()
                    if(myLat != "null" && lat != "null" && myLat != "null" && myLon != "null") {
                        var flat = lat!!.toFloat()
                        var flon = lon!!.toFloat()
                        var fmyLat = myLat!!.toFloat()
                        var fmyLon = myLon!!.toFloat()
                        var dist = getDistFromLatLon(flat, flon, fmyLat, fmyLon).toString()
                        var message = "User with name " + name + " is in trouble " + dist + " km away from you"
                        var alertMessageView = findViewById<TextView>(R.id.alertMessage)
                        alertMessageView.text = message
                    }
                    else {
                        var message = "User with name " + name + " is in trouble but we cannot find how far away they are"
                        var alertMessageView = findViewById<TextView>(R.id.alertMessage)
                        alertMessageView.text = message
                    }
                }

        var mapButton: Button = findViewById(R.id.mapbutton)


        mapButton.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:0,0?q=" + lat + "," + lon + "(ALERT)")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }

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

    // Found on stack overflow
    private fun getDistFromLatLon(lat1:Float, lon1: Float, lat2: Float, lon2: Float): Double {
        var R = 6371
        var dLat = deg2rad(lat2 - lat1)
        var dLon = deg2rad(lon2-lon1)
        var a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2)
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        var d = R * c; // Distance in km
        return d
    }
    private fun deg2rad(deg: Float): Double {
        return deg * (Math.PI/180)
    }

}