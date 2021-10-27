package by.filipau.parking.utils

import android.content.Context
import android.provider.Settings.Global.putFloat
import android.util.Log

//private fun saveParkingLocation() {
//    parkingLocation.latitude = mCurrentLocation?.latitude ?: 0.0
//    parkingLocation.longitude = mCurrentLocation?.longitude ?: 0.0
//
//    activity?.getSharedPreferences("data", Context.MODE_PRIVATE)
//        ?.edit()
//        ?.apply { putFloat("KEY_PARKING_LATITUDE", parkingLocation.latitude.toFloat()) }
//        ?.apply { putFloat("KEY_PARKING_LONGITUDE", parkingLocation.longitude.toFloat()) }
//        ?.apply()
//    Log.e("!@#", "Parking location is: $parkingLocation")
//}