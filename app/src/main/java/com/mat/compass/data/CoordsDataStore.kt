package com.mat.compass.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.createDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CoordsDataStore(context: Context) {
    private val coordsStore: DataStore<Preferences> = context.createDataStore(name = "coords")

    private val latPreferencesKey = doublePreferencesKey("lat")
    private val lonPreferencesKey = doublePreferencesKey("lon")
    private val zoomPreferencesKey = floatPreferencesKey("zoom")

    val latFlow: Flow<Double?> = coordsStore.data
        .map { preferences ->
            preferences[latPreferencesKey]
        }

    val lngFlow: Flow<Double?> = coordsStore.data
        .map { preferences ->
            preferences[lonPreferencesKey]
        }

    val zoomFlow: Flow<Float?> = coordsStore.data
        .map { preferences ->
            preferences[zoomPreferencesKey]
        }

    suspend fun saveLatitude(lat: Double) {
        coordsStore.edit { preferences ->
            preferences[latPreferencesKey] = lat
        }
    }

    suspend fun saveLongitude(lng: Double) {
        coordsStore.edit { preferences ->
            preferences[lonPreferencesKey] = lng
        }
    }

    suspend fun saveZoom(zoom: Float) {
        coordsStore.edit { preferences ->
            preferences[zoomPreferencesKey] = zoom
        }
    }
}
