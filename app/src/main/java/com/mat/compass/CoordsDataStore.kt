package com.mat.compass

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.createDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take

class CoordsDataStore(private val context: Context) {
    private val coordStore: DataStore<Preferences> = context.createDataStore(name = "coords")

    private val latPreferencesKey = doublePreferencesKey("lat")
    private val lonPreferencesKey = doublePreferencesKey("lon")
    private val zoomPreferencesKey = floatPreferencesKey("zoom")

    val latFlow: Flow<Double?> = coordStore.data
        .map { preferences ->
            preferences[latPreferencesKey]
        }
        .take(1)

    val lonFlow: Flow<Double?> = coordStore.data
        .map { preferences ->
            preferences[lonPreferencesKey]
        }
        .take(1)

    val zoomFlow: Flow<Float?> = coordStore.data
        .map { preferences ->
            preferences[zoomPreferencesKey]
        }
        .take(1)

    suspend fun saveLatitude(lat: Double) {
        coordStore.edit { preferences ->
            preferences[latPreferencesKey] = lat
        }
    }

    suspend fun saveLongitude(lon: Double) {
        coordStore.edit { preferences ->
            preferences[lonPreferencesKey] = lon
        }
    }

    suspend fun saveZoom(zoom: Float) {
        coordStore.edit { preferences ->
            preferences[zoomPreferencesKey] = zoom
        }
    }

}