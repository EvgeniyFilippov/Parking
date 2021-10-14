package by.filipau.parking.ui

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import by.filipau.parking.R
import by.filipau.parking.databinding.FragmentStartBinding
import by.filipau.parking.workManager.AlarmReceiver
import by.filipau.parking.workManager.ParkingWorker
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*
import java.util.concurrent.TimeUnit

open class StartFragment : Fragment(), OnMapReadyCallback {

    private var binding: FragmentStartBinding? = null
    var mapFragment: SupportMapFragment?= null
    private var mCurrentLocation: Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var parkingLocation = Location(LocationManager.NETWORK_PROVIDER)
    private lateinit var map: GoogleMap
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var distance = 0
    private var alarmMgr: AlarmManager? = null
    private lateinit var alarmIntent: PendingIntent


    private val singlePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            when {
                granted -> {
//                    showUserLocationOnMap()
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
            interval = 30000
            fastestInterval = 20000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 50000
//            smallestDisplacement = 5F
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

                    Log.e("!@#", "User location: $locationResult. Current thread: ${Thread.currentThread().name}")
                }
            }
        }


        //WORKMANAGER
        val oneTimeWorkerRequest: WorkRequest =
            OneTimeWorkRequestBuilder<ParkingWorker>()
                .build()

        val periodicWorkRequest: WorkRequest =
            PeriodicWorkRequestBuilder<ParkingWorker>(15, TimeUnit.SECONDS)
                .build()

        context?.let {
            WorkManager
                .getInstance(it)
                .enqueue(oneTimeWorkerRequest)
        }

        context?.let {
            WorkManager
                .getInstance(it)
                .enqueue(periodicWorkRequest)
        }


        //ALARMMANAGER
        alarmMgr = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, 0, intent, 0)
        }

        alarmMgr?.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 10000,
            60000,
            alarmIntent
        )

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
    open fun showUserLocationOnMap() {

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                Log.e("!@#", "Last location is: $location. Current thread: ${Thread.currentThread().name}" )
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