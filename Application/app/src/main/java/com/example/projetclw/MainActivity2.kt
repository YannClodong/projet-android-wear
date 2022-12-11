package com.example.projetclw

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.projetclw.databinding.ActivityMain2Binding
import com.google.android.gms.location.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection
import kotlin.collections.Map.Entry


class MainActivity2 : Activity() {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 99;
        private const val BACKGROUND_LOCATION_PERMISSION_REQUEST = 66;
    }
    private lateinit var binding: ActivityMain2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startService.setOnClickListener { _ -> start() }
    }


    private fun start() {
        requestPermissions();
        startServiceWithPermissions();
    }

    private fun requestPermissions() {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this)
                    .setTitle("Location permission")
                    .setMessage("Access to fine location is required to log the position.")
                    .setPositiveButton("OK") { _, _ -> requestFinePositionPermission() }
                    .create().show();
            } else {
                requestFinePositionPermission();
            }
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                AlertDialog.Builder(this)
                    .setTitle("Location permission")
                    .setMessage("Access to background location is required to log the position.")
                    .setPositiveButton("OK") { _, _ -> requestBackgroundLocationAccess() }
                    .create().show();
            } else {
                requestBackgroundLocationAccess();
            }
        }

    }

    private fun requestFinePositionPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST);
    }

    private fun requestBackgroundLocationAccess() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                BACKGROUND_LOCATION_PERMISSION_REQUEST
            );
        } else {
            requestFinePositionPermission();
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        startServiceWithPermissions();
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun startServiceWithPermissions() {
        val haveAccessToLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        val haveBackgroundLocationAccess = Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q
                ||  ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if(haveAccessToLocation && haveBackgroundLocationAccess && !isMyServiceRunning(GpsSensorService::class.java)) {
            // Start the service
            val request = LocationRequest.create()
                .setInterval(5000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            val callback = object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    val loc = p0.locations.lastOrNull();
                    if(loc != null) {
                        Log.d("LOCATION", loc.toString());

                        Log.d("CONFIG", "MainActivity2 l.127: Changer le nom de domaine de la requête.");
                        // performPostCall("http://10.0.0.22:3000/test", loc);
                    }
                    super.onLocationResult(p0)
                }
            }

            val client = LocationServices.getFusedLocationProviderClient(applicationContext);
            client.lastLocation.addOnSuccessListener { res -> Log.d("LOCATION", res?.toString() ?: "NULL"); };
            client.getCurrentLocation(CurrentLocationRequest.Builder().build(), null).addOnSuccessListener { res -> Log.d("LOCATION", res?.toString() ?: "NULL") }
            client.requestLocationUpdates(request, callback, Looper.getMainLooper());
        }
    }

    fun performPostCall(
        requestURL: String?,
        location: Location
    ): String? {
        val url: URL
        var response: String? = ""
        try {
            url = URL(requestURL)
            val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
            conn.setReadTimeout(15000)
            conn.setConnectTimeout(15000)
            conn.setRequestMethod("GET")
            conn.setDoInput(true)
            conn.setDoOutput(true)
            val os: OutputStream = conn.getOutputStream()
            val writer = BufferedWriter(
                OutputStreamWriter(os, "UTF-8")
            )
            writer.write("{ \"latitude\": \"${location.latitude}\", \"longitude\": \"${location.longitude}\" }")
            writer.flush()
            writer.close()
            os.close()
            val responseCode: Int = conn.getResponseCode()
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                var line: String?
                val br = BufferedReader(InputStreamReader(conn.getInputStream()))
                while (br.readLine().also { line = it } != null) {
                    response += line
                }
            } else {
                response = ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return response
    }
}