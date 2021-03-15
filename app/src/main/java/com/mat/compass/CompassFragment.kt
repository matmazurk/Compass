package com.mat.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import androidx.navigation.fragment.findNavController
import com.mat.compass.databinding.FragmentCompassBinding
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class CompassFragment : Fragment(), SensorEventListener {


    private lateinit var binding: FragmentCompassBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private lateinit var locationManager: LocationManager
    private var arrowAnimation: ViewPropertyAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        locationManager = LocationManager(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompassBinding.inflate(inflater, container, false)
        locationManager.newLocation.observe(viewLifecycleOwner) {
            binding.mtv.text = "distance: ${it.distanceTo(Location(android.location.LocationManager.GPS_PROVIDER).apply { 
                latitude = 23.5678
                longitude = 34.456
            })} m"
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, sensor, 1000000)
        locationManager.startLocationUpdates()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.bt.setOnClickListener {
            findNavController().navigate(R.id.action_compassFragment_to_coordsInputFragment)
        }
    }

    override fun onPause() {
        sensorManager.unregisterListener(this)
        locationManager.stopLocationUpdates()
        super.onPause()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val rotation = event?.values?.get(2) ?: 0F
        binding.ivCompass.animate().setDuration(50).rotation(rotation * 180 + 45)
        rotateArrow(rotation * 180)
//        Log.i("event", event?.values?.get(2).toString())
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i("accuracy", "changed")
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