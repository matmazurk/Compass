package com.mat.compass

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.mat.compass.databinding.FragmentCompassBinding
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


class CompassFragment : Fragment() {

    private val viewModel: CompassViewModel by viewModel()
    private lateinit var binding: FragmentCompassBinding
    private lateinit var gpsSwitchStateReceiver: BroadcastReceiver
    private var gpsEnabled = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompassBinding.inflate(inflater, container, false)
        observeAzimuthUpdates()
        observeDistanceChanges()
        observeEnableGpsButton()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        gpsEnabled = requireActivity().isGpsEnabled()
        setupViews()
        viewModel.loadDestination()
        viewModel.startLocationUpdates()
        viewModel.startMeasuring(requireActivity().windowManager)
        registerGpsSwitchStateReceiver()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.bt.setOnClickListener {
            findNavController().navigate(R.id.action_compassFragment_to_coordsInputFragment)
        }
    }

    override fun onPause() {
        viewModel.stopLocationUpdates()
        viewModel.stopMeasuring()
        unregisterGpsSwitchStateReceiver()
        super.onPause()
    }

    private fun observeAzimuthUpdates() {
        viewModel.azimuth.observe(viewLifecycleOwner) { normalizedBearing ->
            binding.ivCompass.animate().rotation(-1 * normalizedBearing + 45F)
            if (viewModel.destination != null && viewModel.distance.value != null && gpsEnabled) {
                binding.arrow.visibility = View.VISIBLE
                rotateArrow(viewModel.destinationPointerAngle - normalizedBearing)
            }
        }
    }

    private fun observeDistanceChanges() {
        viewModel.distance.observe(viewLifecycleOwner) { distance ->
            binding.mtv.text =
                if (distance != null){
                    getString(R.string.distance_meters, distance)
                } else {
                    getString(R.string.distance_unknown)
                }
        }
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

    private fun setupViews() {
        val activity = requireActivity()
        if (activity.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
                activity.isGpsEnabled()) {
            binding.layoutDestination.visibility = View.VISIBLE
            binding.btEnableGps.visibility = View.GONE
        } else {
            binding.layoutDestination.visibility = View.GONE
            binding.btEnableGps.visibility = View.VISIBLE
            binding.arrow.visibility = View.GONE
        }
    }

    private fun observeEnableGpsButton() {
        binding.btEnableGps.setOnClickListener {
            Dexter.withContext(requireActivity())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        val locationReq = LocationRequest.create().apply {
                            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        }
                        val builder = LocationSettingsRequest.Builder().apply {
                            addLocationRequest(locationReq)
                        }
                        val result = LocationServices.getSettingsClient(requireActivity())
                            .checkLocationSettings(
                                builder.build()
                            )
                        result.addOnFailureListener { exception ->
                            if (exception is ResolvableApiException) {
                                exception.startResolutionForResult(requireActivity(), 321)
                            }
                        }
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        if (response.isPermanentlyDenied) {
                            Snackbar
                                .make(
                                    binding.layout,
                                    getString(R.string.change_location_permission),
                                    Snackbar.LENGTH_LONG
                                )
                                .setAction("OK") {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
                                    intent.data = uri
                                    startActivity(intent)
                                }
                                .show()
                        } else {
                            Snackbar
                                .make(
                                    binding.layout,
                                    getString(R.string.permission_rejection_text),
                                    Snackbar.LENGTH_LONG
                                )
                                .show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        request: PermissionRequest?,
                        token: PermissionToken?
                    ) {
                        token?.continuePermissionRequest()
                    }
                }).check()
        }
    }

    private fun registerGpsSwitchStateReceiver() {
        gpsSwitchStateReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                gpsEnabled = context.isGpsEnabled()
                setupViews()
            }
        }
        val filter = IntentFilter(android.location.LocationManager.PROVIDERS_CHANGED_ACTION)
        filter.addAction(Intent.ACTION_PROVIDER_CHANGED)
        requireActivity().registerReceiver(gpsSwitchStateReceiver, filter)
    }

    private fun unregisterGpsSwitchStateReceiver() {
        requireActivity().unregisterReceiver(gpsSwitchStateReceiver)
    }
}
