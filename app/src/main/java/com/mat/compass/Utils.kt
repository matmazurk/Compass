package com.mat.compass

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.app.ActivityCompat
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/*
    returns counterclockwise angle between meridian passing through this location and line passing through
    this and other locations
 */
fun Location.angleBetween(other: Location): Double {
    val dLon = Math.toRadians(this.longitude - other.longitude)
    val thisLatDegrees = Math.toRadians(this.latitude)
    val otherLatDegrees = Math.toRadians(other.latitude)

    val y = sin(dLon) * cos(otherLatDegrees)
    val x = cos(thisLatDegrees) * sin(otherLatDegrees) -
        sin(thisLatDegrees) * cos(otherLatDegrees) * cos(dLon)

    val brng = Math.toDegrees(atan2(y, x))
    return 360 - ((brng + 360) % 360)
}

fun Context.hasPermission(permission: String): Boolean {

    if (permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION &&
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
    ) {
        return true
    }

    return ActivityCompat.checkSelfPermission(this, permission) ==
        PackageManager.PERMISSION_GRANTED
}

fun Context.isGpsEnabled(): Boolean {
    val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}
