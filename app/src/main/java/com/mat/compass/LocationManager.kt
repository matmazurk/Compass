package com.mat.compass

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import java.util.concurrent.TimeUnit

class LocationManager(private val context: Context) {

    val receivingLocationUpdates: LiveData<Boolean> get() = _receivingLocationUpdates
    val newLocation: LiveData<Location> get() = _newLocation

    private val _newLocation: MutableLiveData<Location> = MutableLiveData()
    private val _receivingLocationUpdates: MutableLiveData<Boolean> = MutableLiveData(false)
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = TimeUnit.SECONDS.toMillis(INTERVAL_DURATION_SECONDS)
        fastestInterval = TimeUnit.SECONDS.toMillis(FASTEST_INTERVAL_DURATION_SECONDS)
        maxWaitTime = TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME_SECONDS)
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private val locationCallback = object: LocationCallback() {
        override fun onLocationResult(location: LocationResult) {

            Log.i("new location", "")
            _newLocation.postValue(location.lastLocation)
        }
    }

    @Throws(SecurityException::class)
    @MainThread
    fun startLocationUpdates() {

//        if (!context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) return

        try {
            _receivingLocationUpdates.value = true
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (permissionRevoked: SecurityException) {
            _receivingLocationUpdates.value = false
            throw permissionRevoked
        }
    }

    @MainThread
    fun stopLocationUpdates() {
        _receivingLocationUpdates.value = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        private const val INTERVAL_DURATION_SECONDS = 1L
        private const val FASTEST_INTERVAL_DURATION_SECONDS = INTERVAL_DURATION_SECONDS / 2
        private const val MAX_WAIT_TIME_SECONDS = INTERVAL_DURATION_SECONDS * 2
    }
}