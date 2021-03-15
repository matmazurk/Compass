package com.mat.compass

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.*
import com.mat.compass.databinding.FragmentCoordsInputBinding

class CoordsInputFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentCoordsInputBinding
    private lateinit var mapView: MapView

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.bt2.setOnClickListener {
            findNavController().navigate(R.id.action_coordsInputFragment_to_compassFragment)
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        Log.i("map", "ready")
    }
}