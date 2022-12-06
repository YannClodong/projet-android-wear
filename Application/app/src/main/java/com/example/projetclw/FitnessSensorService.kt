package com.example.projetclw

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.SensorsClient
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataSourcesRequest
import com.google.android.gms.fitness.request.OnDataPointListener
import com.google.android.gms.fitness.request.SensorRequest
import java.util.concurrent.TimeUnit


class FitnessSensorService : Service() {
    private val binder: SensorsServiceBinder = SensorsServiceBinder();
    private lateinit var sensors: SensorsClient;
    private var listener: OnDataPointListener? = null;

    override fun onBind(intent: Intent): IBinder {
        val fitnessOptions = FitnessOptions.builder().addDataType(DataType.TYPE_STEP_COUNT_DELTA).build()
        sensors = Fitness.getSensorsClient(this.applicationContext, GoogleSignIn.getAccountForExtension(this.applicationContext, fitnessOptions));


        sensors.findDataSources(DataSourcesRequest.Builder()
            .setDataTypes(DataType.TYPE_MOVE_MINUTES)
            .setDataSourceTypes(DataSource.TYPE_RAW)
            .build()).addOnSuccessListener { dataSources -> dataSources.forEach {
                if(it.dataType == DataType.TYPE_MOVE_MINUTES) {
                    listener = subscribeToDataSource(it);
                }
        } }.addOnFailureListener { e -> Log.e("SENSOR_ERROR", "Fail to request data source.") }

        return binder;
    }


    inner class SensorsServiceBinder : Binder() {
        fun getService(): FitnessSensorService {
            return this@FitnessSensorService;
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if(listener != null)
            sensors.remove(listener!!);
        return super.onUnbind(intent)
    }

    private fun subscribeToDataSource(dataSource: DataSource): OnDataPointListener {
        val listener = OnDataPointListener { dataPoint -> for(field in dataPoint.dataType.fields) {
            val value = dataPoint.getValue(field);
            Log.i("TEST_SENSOR", "Detected DataPoint field: ${field.name}")
            Log.i("TEST_SENSOR", "Detected DataPoint value: $value")
        } }

        sensors.add(SensorRequest.Builder()
            .setDataType(DataType.TYPE_MOVE_MINUTES)
            .setDataSource(dataSource)
            .setSamplingRate(10, TimeUnit.SECONDS)
            .build(), listener);

        return listener;
    }
}