package com.mat.compass

import android.location.Location
import android.os.Bundle
import android.util.Log
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class CompassFragment : Fragment() {

    private val viewModel: CompassViewModel by viewModel()
    private lateinit var binding: FragmentCompassBinding
    private lateinit var coordsDataStore: CoordsDataStore
    private lateinit var destination: Location
    private lateinit var currentLocation: Location
    private var previousCompassBearing = -1f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        coordsDataStore = CoordsDataStore(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompassBinding.inflate(inflater, container, false)
        observeAzimuthUpdates()
        observeLocationUpdates()
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
                destination = Location("").apply {
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

    private fun observeAzimuthUpdates() {
        viewModel.azimuth.observe(viewLifecycleOwner) { targetCompassBearing ->
            if (previousCompassBearing < 0) {
                previousCompassBearing = targetCompassBearing
            }
            val normalizedBearing: Float = shortestRotation(
                targetCompassBearing,
                previousCompassBearing
            )
            previousCompassBearing = targetCompassBearing
            binding.ivCompass.animate().rotation(-1 * normalizedBearing + 45F)
            if (this::destination.isInitialized && this::currentLocation.isInitialized) {
                val angle = currentLocation.angleBetween(destination).toFloat()
                rotateArrow(angle - normalizedBearing)
            }
        }
    }

    private fun observeLocationUpdates() {
        viewModel.newLocation.observe(viewLifecycleOwner) { newLocation ->
            if (this::destination.isInitialized) {
                currentLocation = newLocation
                val distance = currentLocation.distanceTo(destination).toInt()
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

    private fun rotateArrow(angle: Float) {
        val rads = angle * PI / 180
        val radius = 700F
        val translY = -1 * radius * cos(rads)
        val translX = radius * sin(rads)
        binding.arrow.animate().apply {
            translationY(translY.toFloat())
            translationX(translX.toFloat())
            rotation(angle)
        }
    }

}