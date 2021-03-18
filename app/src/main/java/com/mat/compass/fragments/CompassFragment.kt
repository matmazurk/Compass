package com.mat.compass.fragments

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
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
import com.mat.compass.CompassViewModel
import com.mat.compass.R
import com.mat.compass.databinding.FragmentCompassBinding
import com.mat.compass.hasPermission
import com.mat.compass.isGpsEnabled
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class CompassFragment : Fragment() {

    private val viewModel: CompassViewModel by viewModel()
    private lateinit var binding: FragmentCompassBinding
    private lateinit var gpsSwitchStateReceiver: BroadcastReceiver
    private var gpsEnabled = false
    private var compassHeight: Int = 0

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
                if (distance != null) {
                    getString(R.string.distance_meters, distance)
                } else {
                    getString(R.string.distance_unknown)
                }
        }
    }

    private fun rotateArrow(angle: Float) {
        val rads = angle * PI / 180
        val radius = compassHeight / 2 + 120
        val translY = -1 * radius * cos(rads)
        val translX = radius * sin(rads)
        binding.arrow.animate().apply {
            translationY(translY.toFloat())
            translationX(translX.toFloat())
            rotation(angle)
        }
    }

    /*
        function calculates compass view size for given screen size and direction pointer size
        it hides and shows views according to location availability
     */
    private fun setupViews() {
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels - getStatusBarHeight()
        val arrowWidth = resources.getDimension(R.dimen.destination_pointer_size)
        val rest = if (screenWidth > screenHeight) {
            (screenHeight - 2 * arrowWidth).toInt()
        } else {
            (screenWidth - 2 * arrowWidth).toInt()
        }
        compassHeight = rest - 150
        binding.ivCompass.layoutParams.apply {
            height = compassHeight
            width = compassHeight
        }
        val activity = requireActivity()
        if (activity.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            activity.isGpsEnabled()
        ) {
            binding.layoutDestination.visibility = View.VISIBLE
            binding.btEnableGps.visibility = View.GONE
        } else {
            binding.layoutDestination.visibility = View.GONE
            binding.btEnableGps.visibility = View.VISIBLE
            binding.arrow.visibility = View.INVISIBLE
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
                        // when permission.ACCESS_FINE_LOCATION granted, but location is disabled
                        // show location dialog
                        result.addOnFailureListener { exception ->
                            if (exception is ResolvableApiException) {
                                exception.startResolutionForResult(requireActivity(), 321)
                            }
                        }
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        if (response.isPermanentlyDenied) {
                            // show snackbar allowing app setting navigation
                            Snackbar
                                .make(
                                    binding.layout,
                                    getString(R.string.change_location_permission),
                                    Snackbar.LENGTH_LONG
                                )
                                .setAction(getString(R.string.ok)) {
                                    val intent =
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    val uri: Uri = Uri.fromParts(
                                        "package",
                                        requireActivity().packageName,
                                        null
                                    )
                                    intent.data = uri
                                    startActivity(intent)
                                }
                                .show()
                        } else {
                            // show snackbar with rationale
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
        gpsSwitchStateReceiver = object : BroadcastReceiver() {
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

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}