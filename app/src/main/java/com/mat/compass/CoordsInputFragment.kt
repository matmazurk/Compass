package com.mat.compass

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.mat.compass.databinding.FragmentCoordsInputBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class CoordsInputFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentCoordsInputBinding
    private lateinit var coordDataStore: CoordsDataStore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCoordsInputBinding.inflate(inflater, container, false)
        val mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        MapsInitializer.initialize(requireActivity())
        mapView.getMapAsync(this)
        coordDataStore = CoordsDataStore(requireActivity())
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    }

    override fun onMapReady(map: GoogleMap?) {
        map ?: return
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true
        lifecycleScope.launch {
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        coordDataStore.latFlow.first() ?: 0.0,
                        coordDataStore.lonFlow.first() ?: 0.0
                    ), coordDataStore.zoomFlow.first() ?: 0F
                )
            )


        }
        Toast.makeText(requireActivity(), "select destination", Toast.LENGTH_SHORT).show()
        map.setOnMapClickListener {
            val zoom = map.cameraPosition.zoom
            val lat = it.latitude
            val lon = it.longitude

            lifecycleScope.launch {
                with(coordDataStore) {
                    saveLatitude(lat)
                    saveLongitude(lon)
                    saveZoom(zoom)
                }
                Log.i("map onclick", "$zoom $lat $lon")
                Toast.makeText(requireActivity(), "saving destination", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_coordsInputFragment_to_compassFragment)
            }

        }
    }
}