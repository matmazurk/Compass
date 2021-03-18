package com.mat.compass.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.mat.compass.R
import com.mat.compass.data.CoordsDataStore
import com.mat.compass.databinding.FragmentCoordsInputBinding
import com.mat.compass.hasPermission
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class CoordsInputFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentCoordsInputBinding
    private val coordDataStore: CoordsDataStore by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCoordsInputBinding.inflate(inflater, container, false)
        val mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        MapsInitializer.initialize(requireActivity())
        mapView.getMapAsync(this)
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap?) {
        map ?: return
        if (requireActivity().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            map.isMyLocationEnabled = true
        }
        map.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMyLocationButtonEnabled = true
        }
        lifecycleScope.launch {
            val savedLat = coordDataStore.latFlow.first()
            val savedLng = coordDataStore.lngFlow.first()
            if (savedLat != null && savedLng != null) {
                val savedLatLng = LatLng(savedLat, savedLng)
                map.apply {
                    addMarker(
                        MarkerOptions()
                            .position(savedLatLng)
                    )
                    moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            savedLatLng,
                            coordDataStore.zoomFlow.first() ?: 0F
                        )
                    )
                }
            }
        }
        Snackbar
            .make(
                binding.mapView,
                getString(R.string.select_destination),
                Snackbar.LENGTH_SHORT
            )
            .show()
        map.setOnMapClickListener {
            val zoom = map.cameraPosition.zoom
            val lat = it.latitude
            val lng = it.longitude

            lifecycleScope.launch {
                with(coordDataStore) {
                    saveLatitude(lat)
                    saveLongitude(lng)
                    saveZoom(zoom)
                }
                Snackbar
                    .make(
                        binding.mapView,
                        getString(R.string.destination_saved),
                        Snackbar.LENGTH_SHORT
                    )
                    .show()
                findNavController().navigate(R.id.action_coordsInputFragment_to_compassFragment)
            }
        }
    }
}
