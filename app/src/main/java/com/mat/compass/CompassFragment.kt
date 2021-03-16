package com.mat.compass

import android.hardware.*
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mat.compass.databinding.FragmentCompassBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.*

class CompassFragment : Fragment() {

    private lateinit var binding: FragmentCompassBinding

    private lateinit var coordsDataStore: CoordsDataStore
    private lateinit var currentLocation: Location

    private lateinit var locationManager: LocationManager
    private var arrowAnimation: ViewPropertyAnimator? = null
    private var savedLon: Double? = null
    private var savedLat: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationManager = LocationManager(requireActivity())
        coordsDataStore = CoordsDataStore(requireActivity())
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompassBinding.inflate(inflater, container, false)
        locationManager.lastLocation.observe(viewLifecycleOwner) {
            currentLocation = it
        }
        locationManager.newLocation.observe(viewLifecycleOwner) {
            currentLocation = it
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val lat = coordsDataStore.latFlow.first()
        }
        locationManager.startLocationUpdates()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.bt.setOnClickListener {
            findNavController().navigate(R.id.action_compassFragment_to_coordsInputFragment)
        }
    }

    override fun onPause() {
        locationManager.stopLocationUpdates()
        super.onPause()
    }

    private fun rotateArrow(angle: Float) {
        val rads = angle * PI / 180
        val radius = 700F
        val translY = -1 * radius * cos(rads)
        val translX = radius * sin(rads)
        arrowAnimation = binding.arrow.animate().apply {
            translationY(translY.toFloat())
            translationX(translX.toFloat())
            duration = 50
            rotation(angle)
        }
    }

}