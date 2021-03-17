package com.mat.compass

import android.location.Location
import android.view.WindowManager
import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class CompassViewModel (
    private val repository: Repository,
    private val coordsDataStore: CoordsDataStore,
) : ViewModel() {

    val azimuth: LiveData<Float> get() = _azimuth
    var destination: Location? = null
        private set
    val distance: LiveData<Int?> get() = _distance
    var destinationPointerAngle: Float = 0f
        private set

    private var previousCompassBearing = -1f
    private lateinit var currentLocation: Location
    private val _distance = Transformations.map(repository.newLocation) { newLocation ->
        newLocation ?: return@map null
        currentLocation = newLocation
        destinationPointerAngle = currentLocation.angleBetween(destination ?: return@map null).toFloat()
        currentLocation.distanceTo(destination).toInt()
    }
    private val _azimuth = Transformations.map(repository.azimuth) { targetCompassBearing ->
        if (previousCompassBearing < 0) {
            previousCompassBearing = targetCompassBearing
        }
        val normalizedBearing: Float = shortestRotation(
                targetCompassBearing,
                previousCompassBearing
        )
        previousCompassBearing = targetCompassBearing
        normalizedBearing
    }

    fun startMeasuring(windowManager: WindowManager) = repository.startMeasuring(windowManager)

    fun stopMeasuring() = repository.stopMeasuring()

    fun startLocationUpdates() = repository.startLocationUpdates()

    fun stopLocationUpdates() = repository.stopLocationUpdates()

    fun loadDestination() =
        viewModelScope.launch {
            val lat = coordsDataStore.latFlow.first()
            val lon = coordsDataStore.lonFlow.first()
            if (lat != null && lon != null) {
                destination = Location("").apply {
                    latitude = lat
                    longitude = lon
                }
            }
        }

    private fun shortestRotation(heading: Float, previousHeading: Float): Float {
        var rotation = heading
        val diff = (previousHeading - heading).toDouble()
        if (diff > 180.0f) {
            rotation += 360.0f
        } else if (diff < -180.0f) {
            rotation -= 360f
        }
        return rotation
    }
}