package com.mat.compass

import android.view.WindowManager
import androidx.lifecycle.LiveData

class Repository(
    private val locationManager: LocationManager,
    private val azimuthProvider: AzimuthProvider,
) {
    val azimuth: LiveData<Float> = azimuthProvider.azimuth
    val newLocation = locationManager.newLocation

    fun startMeasuring(windowManager: WindowManager) = azimuthProvider.startMeasuring(windowManager)

    fun stopMeasuring() = azimuthProvider.stopMeasuring()

    fun startLocationUpdates() = locationManager.startLocationUpdates()

    fun stopLocationUpdates() = locationManager.stopLocationUpdates()
}