package com.example.projetclw

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.projetclw.databinding.ActivityMain2Binding
import com.google.android.gms.location.*
import java.net.HttpURLConnection
import java.net.URL
import com.google.gson.Gson

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


        // prevent display from sleeping
        binding.root.keepScreenOn = true

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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BODY_SENSORS)) {
                AlertDialog.Builder(this)
                    .setTitle("Sensor permission")
                    .setMessage("Access to sensors is required to log the position.")
                    .setPositiveButton("OK") { _, _ -> requestSensorPermission() }
                    .create().show();
            } else {
                requestSensorPermission();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WAKE_LOCK)) {
                    AlertDialog.Builder(this)
                        .setTitle("Wake lock permission")
                        .setMessage("Access to wake lock is required to log the position.")
                        .setPositiveButton("OK") { _, _ -> requestWakeLockPermission() }
                        .create().show();
                } else {
                    requestWakeLockPermission();
                }
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

    private fun requestSensorPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BODY_SENSORS), LOCATION_PERMISSION_REQUEST);
    }

    private fun requestWakeLockPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WAKE_LOCK), LOCATION_PERMISSION_REQUEST);
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

    data class GpsLocation(val lat: Double, val lng: Double)
    data class ClswData<T>(
        val sensor: String,
        val value: T,
        val clsw: Boolean = true
    )

    private fun startServiceWithPermissions() {
        val haveAccessToLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        val haveBackgroundLocationAccess = Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q
                ||  ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if(haveAccessToLocation && haveBackgroundLocationAccess && !isMyServiceRunning(GpsSensorService::class.java)) {
            // create a background service that fetches location every 5 second
            val service = Intent(this, GpsSensorService::class.java)
            startService(service)
            /*
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

                        val data =
                            ClswData("gps", GpsLocation(loc.latitude, loc.longitude))
                        sendSensorData(data)
                    }
                    super.onLocationResult(p0)
                }
            }

            val client = LocationServices.getFusedLocationProviderClient(applicationContext);
            client.lastLocation.addOnSuccessListener { res -> Log.d("LOCATION", res?.toString() ?: "NULL"); };
            client.getCurrentLocation(CurrentLocationRequest.Builder().build(), null).addOnSuccessListener { res -> Log.d("LOCATION", res?.toString() ?: "NULL") }
            client.requestLocationUpdates(request, callback, Looper.getMainLooper());*/
        }
/*
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val temp = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        // every 5 second
        sensorManager.registerListener(object : SensorEventListener {
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
                // TODO
            }

            override fun onSensorChanged(p0: SensorEvent?) {
                if(p0 != null) {
                    val data = ClswData("temperature", p0.values[0])
                    sendSensorData(data)
                }
            }
        }, temp, 30 * 1000 * 1000)

        val pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        sensorManager.registerListener(object : SensorEventListener {
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
                // TODO
            }

            override fun onSensorChanged(p0: SensorEvent?) {
                if(p0 != null) {
                    val data = ClswData("pressure", p0.values[0])
                    sendSensorData(data)
                }
            }
        }, pressure, 30 * 1000 * 1000)

        val bpm = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        sensorManager.registerListener(object : SensorEventListener {
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
                // TODO
            }

            override fun onSensorChanged(p0: SensorEvent?) {
                if(p0 != null) {
                    val data = ClswData("bpm", p0.values[0])
                    sendSensorData(data)
                }
            }
        }, bpm, 1000 * 1000)*/
    }

    private fun <T> sendSensorData(data: ClswData<T>) {
        Thread {
            val json = Gson().toJson(data)
            Log.d("JSON", json)
            val url = URL("https://domino.zdimension.fr/web/clsw/data.php")
            try {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; utf-8")
                connection.doOutput = true
                val os = connection.outputStream
                val input: ByteArray = json.toByteArray()
                os.write(input, 0, input.size)
                val responseCode = connection.responseCode
                Log.d("RESPONSE", responseCode.toString())
            } catch (e: Exception) {
                Log.e("ERROR", e.toString())
            }
        }.start()
    }

}