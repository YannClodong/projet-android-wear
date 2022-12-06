package com.example.projetclw

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import java.net.HttpURLConnection
import java.net.URL

class GpsSensorService : Service(), SensorEventListener {
    override fun onBind(p0: Intent?): IBinder? {
        return null;
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createLocationRequest();

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val temp = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        /*// every 5 second
        sensorManager.registerListener(this, temp, 30 * 1000 * 1000)

        val pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        sensorManager.registerListener(this, pressure, 30 * 1000 * 1000)*/

        val bpm = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        sensorManager.registerListener(this, bpm, 1000 * 1000)

        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun createLocationRequest() {
        Log.d("DEBUG", "Creating location request.");
        val request = LocationRequest.create()
            .setInterval(1000)
            .setFastestInterval(1000);

        val client = LocationServices.getFusedLocationProviderClient(this);
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                val loc = result.lastLocation;
                if(loc != null) onNewLocation(loc);
            }
        }
            
        client.requestLocationUpdates(request, callback, Looper.getMainLooper());
    }

    private fun onNewLocation(loc: Location) {
        Log.d("LOCATION", loc.toString());

        val data =
            MainActivity2.ClswData("gps", MainActivity2.GpsLocation(loc.latitude, loc.longitude))
        sendSensorData(data)
    }

    private fun <T> sendSensorData(data: MainActivity2.ClswData<T>) {
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

    override fun onSensorChanged(p0: SensorEvent?) {
        val kind = when (p0?.sensor?.type) {
            Sensor.TYPE_AMBIENT_TEMPERATURE -> "temperature"
            Sensor.TYPE_PRESSURE -> "pressure"
            Sensor.TYPE_HEART_RATE -> "bpm"
            else -> return
        }
        val data = MainActivity2.ClswData(kind, p0.values[0])
        sendSensorData(data)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }
}