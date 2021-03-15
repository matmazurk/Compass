package com.mat.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.mat.compass.databinding.FragmentCompassBinding
import kotlin.math.atan

class CompassFragment : Fragment(), SensorEventListener {

    private lateinit var binding: FragmentCompassBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompassBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, sensor, 100000)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.bt.setOnClickListener {
            findNavController().navigate(R.id.action_compassFragment_to_coordsInputFragment)
        }
    }

    override fun onPause() {
        sensorManager.unregisterListener(this)
        super.onPause()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        Log.i("event", event?.values?.get(2).toString() + "   ${event?.accuracy}")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i("accuracy", "changed")
    }

}