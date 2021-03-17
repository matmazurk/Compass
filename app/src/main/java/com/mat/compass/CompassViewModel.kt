package com.mat.compass

import android.view.WindowManager
import androidx.lifecycle.ViewModel

class CompassViewModel (
    private val repository: Repository
) : ViewModel() {

    val azimuth = repository.azimuth
    val newLocation = repository.newLocation

    fun startMeasuring(windowManager: WindowManager) = repository.startMeasuring(windowManager)

    fun stopMeasuring() = repository.stopMeasuring()

    fun startLocationUpdates() = repository.startLocationUpdates()

    fun stopLocationUpdates() = repository.stopLocationUpdates()
}