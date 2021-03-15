package com.mat.compass

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.*
import com.mat.compass.databinding.FragmentCoordsInputBinding

class CoordsInputFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentCoordsInputBinding

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
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.bt2.setOnClickListener {
            findNavController().navigate(R.id.action_coordsInputFragment_to_compassFragment)
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        map ?: return
        Toast.makeText(requireActivity(), "select destination", Toast.LENGTH_SHORT).show()
        map.setOnMapClickListener {
            val zoom = map.cameraPosition.zoom
            val lat = it.latitude
            val lon = it.longitude
            Log.i("map onclick", "$zoom $lat $lon")
            Toast.makeText(requireActivity(), "saving destination", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_coordsInputFragment_to_compassFragment)
        }
    }
}