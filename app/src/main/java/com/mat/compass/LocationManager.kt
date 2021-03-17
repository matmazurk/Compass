package com.mat.compass

import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import java.util.concurrent.TimeUnit

class LocationManager(context: Context) {

    val newLocation: LiveData<Location> get() = _newLocation

    private val _newLocation: MutableLiveData<Location> = MutableLiveData()
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
            Log.i("new location", "${location.lastLocation.latitude} ${location.lastLocation.longitude}")
            _newLocation.postValue(location.lastLocation)
        }
    }

    @Throws(SecurityException::class)
    @MainThread
    fun startLocationUpdates() {

//        if (!context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) return
        fusedLocationClient.lastLocation.addOnSuccessListener {
            _newLocation.postValue(it)
        }
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (permissionRevoked: SecurityException) {
            throw permissionRevoked
        }
    }

    @MainThread
    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        private const val INTERVAL_DURATION_SECONDS = 1L
        private const val FASTEST_INTERVAL_DURATION_SECONDS = INTERVAL_DURATION_SECONDS / 2
        private const val MAX_WAIT_TIME_SECONDS = INTERVAL_DURATION_SECONDS * 2
    }
}