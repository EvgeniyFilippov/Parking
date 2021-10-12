package by.filipau.parking.ui

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import by.filipau.parking.R
import by.filipau.parking.databinding.FragmentStartBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class StartFragment : Fragment(), OnMapReadyCallback {

    private var binding: FragmentStartBinding? = null
    var mapFragment: SupportMapFragment?= null
    private var mCurrentLocation: Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var parkingLocation = Location(LocationManager.NETWORK_PROVIDER)
    private lateinit var map: GoogleMap
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var distance = 0

    @SuppressLint("MissingPermission")
    val singlePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            when {
                granted -> {
                    showUserLocationOnMap()
                }
                !shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {

                }
                else -> {

                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        locationRequest = LocationRequest.create().apply {
            interval = 3000
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 5000
        }

      locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    mCurrentLocation = location
                    binding?.locationView?.text = getString(R.string.current_location_message,
                        location?.latitude.toString(),
                        location?.longitude.toString())
                    distance = location.distanceTo(parkingLocation).toInt()
                    binding?.distanceView?.text = distance.toString()
                    Log.e("!@#", locationResult.toString())
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStartBinding.inflate(inflater, container, false)
        mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment?
        mapFragment?.getMapAsync(this)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {


        } else {
            singlePermission.launch(ACCESS_FINE_LOCATION)

        }

        binding?.btnSaveLocation?.setOnClickListener {
            map.clear()
            saveParkingLocation()
            map.addParkingMarker(parkingLocation.latitude,parkingLocation.longitude)
        }

        binding?.btnResetLocation?.setOnClickListener {
            map.clear()
        }

    }


    @SuppressLint("MissingPermission")
    private fun showUserLocationOnMap() {

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                Log.e("!@#", "Location is: $location" )
                binding?.locationView?.text = getString(R.string.current_location_message,
                    location?.latitude.toString(),
                    location?.longitude.toString())
                mCurrentLocation = location
            }

    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }

    override fun onMapReady(googleMap: GoogleMap) {
        readParkingLocation()
        map = googleMap
        if (parkingLocation.latitude != 0.0 && parkingLocation.longitude != 0.0) {
            googleMap.addParkingMarker(parkingLocation.latitude, parkingLocation.longitude)
        } else {
            map.clear()
        }

    }

    private fun saveParkingLocation() {
        parkingLocation.latitude = mCurrentLocation?.latitude ?: 0.0
        parkingLocation.longitude = mCurrentLocation?.longitude ?: 0.0

        activity?.getSharedPreferences("data", Context.MODE_PRIVATE)
            ?.edit()
            ?.apply { putFloat("KEY_PARKING_LATITUDE", parkingLocation.latitude.toFloat()) }
            ?.apply { putFloat("KEY_PARKING_LONGITUDE", parkingLocation.longitude.toFloat()) }
            ?.apply()
        Log.e("!@#", "Parking location is: $parkingLocation")
    }

    private fun readParkingLocation() {
        val sharedPreference =
            activity?.getSharedPreferences("data", Context.MODE_PRIVATE)
        val latitude = sharedPreference?.getFloat("KEY_PARKING_LATITUDE", 0.0f)
        val longitude = sharedPreference?.getFloat("KEY_PARKING_LONGITUDE", 0.0f)

        if (latitude != null && longitude != null) {
            parkingLocation.latitude = latitude.toDouble()
            parkingLocation.longitude = longitude.toDouble()
        }

    }

    fun GoogleMap.addParkingMarker(lat: Double, lon: Double) {
        val parkingPoint = LatLng(lat, lat)
        this.addMarker(
            MarkerOptions()
                .position(parkingPoint)
                .title("Your car is here")
//                    .draggable(true)
        )
        val yourLocation = CameraUpdateFactory.newLatLngZoom(parkingPoint, this.maxZoomLevel)
        map.animateCamera(yourLocation)
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

}