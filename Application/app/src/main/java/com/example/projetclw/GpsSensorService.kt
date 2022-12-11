package com.example.projetclw

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class GpsSensorService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null;
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createLocationRequest();
        return super.onStartCommand(intent, flags, startId)
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

    private fun onNewLocation(location: Location) {
        Log.d("DEBUG", "New location received.")
    }
}