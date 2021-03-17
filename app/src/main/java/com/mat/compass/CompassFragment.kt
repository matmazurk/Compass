package com.mat.compass

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mat.compass.databinding.FragmentCompassBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel

class CompassFragment : Fragment() {

    private val viewModel: CompassViewModel by viewModel()
    private lateinit var binding: FragmentCompassBinding
    private lateinit var coordsDataStore: CoordsDataStore
    private lateinit var savedLocation: Location
    private var previousCompassBearing = -1f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        coordsDataStore = CoordsDataStore(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompassBinding.inflate(inflater, container, false)
        observeAzimuth()
        observeLocation()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.startLocationUpdates()
        viewModel.startMeasuring(requireActivity().windowManager)
        lifecycleScope.launch {
            val lat = coordsDataStore.latFlow.first()
            val lon = coordsDataStore.lonFlow.first()
            if (lat != null && lon != null) {
                savedLocation = Location("").apply {
                    latitude = lat
                    longitude = lon
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.bt.setOnClickListener {
            findNavController().navigate(R.id.action_compassFragment_to_coordsInputFragment)
        }
    }

    override fun onPause() {
        viewModel.stopLocationUpdates()
        viewModel.stopMeasuring()
        super.onPause()
    }

    private fun observeAzimuth() {
        viewModel.azimuth.observe(viewLifecycleOwner) { targetCompassBearing ->
            if (previousCompassBearing < 0) {
                previousCompassBearing = targetCompassBearing.toFloat()
            }
            val normalizedBearing: Float = shortestRotation(
                targetCompassBearing.toFloat(),
                previousCompassBearing
            )
            previousCompassBearing = targetCompassBearing.toFloat()
            binding.ivCompass.animate().rotation(-1 * normalizedBearing + 45F)
        }
    }

    private fun observeLocation() {
        viewModel.newLocation.observe(viewLifecycleOwner) { newLocation ->
            if (this::savedLocation.isInitialized) {
                val distance = newLocation.distanceTo(savedLocation).toInt()
                binding.mtv.text = getString(R.string.distance_meters, distance)
            } else {
                binding.mtv.text = getString(R.string.distance_unknown)
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