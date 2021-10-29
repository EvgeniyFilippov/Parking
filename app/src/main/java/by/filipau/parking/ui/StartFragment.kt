package by.filipau.parking.ui

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.hardware.*
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import by.filipau.parking.R
import by.filipau.parking.databinding.FragmentStartBinding

import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import timber.log.Timber
import androidx.core.content.ContextCompat.getSystemService
import android.view.animation.Animation

import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import by.filipau.parking.utils.showAlertDialogWithMessage
import kotlin.math.roundToInt


open class StartFragment : Fragment(), OnMapReadyCallback, SensorEventListener {

    private var binding: FragmentStartBinding? = null
    var mapFragment: SupportMapFragment?= null
    private var mCurrentLocation: Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var parkingLocation = Location(LocationManager.NETWORK_PROVIDER)
    private lateinit var map: GoogleMap
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var distance = 0
    private var image: AppCompatImageView? = null
    private lateinit var lm: LocationManager
    private var gps_enabled = false
    private var network_enabled = false


    private var mSensorManager: SensorManager? = null
    private var currentDegree = 0f
    private var geoField: GeomagneticField  = GeomagneticField(
        parkingLocation.latitude.toFloat(), parkingLocation.longitude.toFloat(),
        parkingLocation.altitude.toFloat(), System.currentTimeMillis() )

    private val singlePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            when {
                granted -> {
//                    showUserLocationOnMap()
                    Timber.d("Location permission granted")
                }
                !shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {
                    Timber.d("Request permission rationale")
                }
                else -> {

                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        mSensorManager = activity?.getSystemService(SENSOR_SERVICE) as SensorManager?

        locationRequest = LocationRequest.create().apply {
            interval = 3000
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 3000
            smallestDisplacement = 0f
        }

        lm = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        //location callback
      locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    Timber.d("locationCallback." + Thread.currentThread().name)
                    mCurrentLocation = location
                    Timber.d("Accuracy: ${location.accuracy}. Provider: ${location.provider}")

                    binding?.locationView?.text = getString(
                        R.string.current_location_message,
                        location?.latitude.toString(),
                        location?.longitude.toString()
                    )
                    distance = location.distanceTo(parkingLocation).toInt()
                    binding?.distanceView?.text = distance.toString()

                    Timber.e("User location: " + locationResult + ". Current thread: " + Thread.currentThread().name)
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
            activity?.showAlertDialogWithMessage(getString(R.string.location_alert_text_body))
            Timber.d("Should show rationale")

        } else {
            singlePermission.launch(ACCESS_FINE_LOCATION)
            Timber.d("Launch permission")

        }

        binding?.btnSaveLocation?.setOnClickListener {
            map.clear()
            saveParkingLocation()
            map.addParkingMarker(parkingLocation.latitude,parkingLocation.longitude)
        }

        binding?.btnResetLocation?.setOnClickListener {
            map.clear()
        }



        image = binding?.compassView

    }


//    @SuppressLint("MissingPermission")
//    open fun showUserLocationOnMap() {
//
//        fusedLocationClient.lastLocation
//            .addOnSuccessListener { location : Location? ->
//                Timber.e("Last location is: $location. Current thread: ${Thread.currentThread().name}")
//                binding?.locationView?.text = getString(
//                    R.string.current_location_message,
//                    location?.latitude.toString(),
//                    location?.longitude.toString()
//                )
//                mCurrentLocation = location
//            }
//
//    }

    //get location
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        Timber.e("startLocationUpdates method")
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        readParkingLocation()
        map = googleMap
//        map.isMyLocationEnabled = true
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
        Timber.e("Parking location is: $parkingLocation")
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
        val parkingPoint = LatLng(lat, lon)
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
        mSensorManager?.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        requestingLocationUpdates()
        startLocationUpdates()
        mSensorManager?.registerListener(this, mSensorManager!!.getDefaultSensor(Sensor.TYPE_ORIENTATION),
            SensorManager.SENSOR_DELAY_GAME);
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        var degree = event?.values?.let { it[0].roundToInt().toFloat() }
        degree = degree?.plus(geoField.declination)

        val bearing: Float? = mCurrentLocation?.bearingTo(parkingLocation)
        if (bearing != null) {
            degree = (bearing - degree!!) * -1
        }
        degree = degree?.let { normalizeDegree(it) }

//        tvHeading.setText("Heading: " + java.lang.Float.toString(degree) + " degrees")

        // create a rotation animation (reverse turn degree degrees)
        val ra = RotateAnimation(
            currentDegree,
            -degree!!,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )

        // how long the animation will take place

        // how long the animation will take place
        ra.duration = 210

        // set the animation after the end of the reservation status

        // set the animation after the end of the reservation status
        ra.fillAfter = true

        // Start the animation

        // Start the animation
        image?.startAnimation(ra)
        currentDegree = -degree
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //not use

    }

    private fun normalizeDegree(value: Float): Float {
        return if (value in 0.0f..180.0f) {
            value
        } else {
            180 + (180 + value)
        }
    }

    private fun requestingLocationUpdates() {
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(e: Exception) {
            Timber.d("Error check gps")
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(e: Exception) {
            Timber.d("Error check network")
        }

        if(!gps_enabled && !network_enabled) {
            Timber.d("Gps off")
        }
    }

}