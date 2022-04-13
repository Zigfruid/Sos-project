package com.example.sos.utils

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import android.location.Location
import android.location.LocationListener

class LocationHelper(private val context: Context): LocationListener {
    companion object {
        const val TAG = "LocationHelper"
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10 // 10 meters
        private const val MIN_TIME_BW_UPDATES = (1000 * 60 * 1 // 1 minute
                ).toLong()
    }
    private var locationManager: LocationManager? = null
    private var isGPSEnabled = false
    var isNetworkEnabled = false
    var isGPSTrackingEnabled = false
    var latitude = 0.0
    var longitude = 0.0
    private var location: Location? = null
    private var provider_info: String? = null
    fun getLocation() {
        try {
            locationManager = context.getSystemService(Service.LOCATION_SERVICE) as LocationManager
            isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (isGPSEnabled || isNetworkEnabled) {
                isGPSTrackingEnabled = true
                provider_info = LocationManager.GPS_PROVIDER
                provider_info = LocationManager.NETWORK_PROVIDER

                if (provider_info!!.isNotEmpty()) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    locationManager!!.requestLocationUpdates(
                        provider_info!!,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
                        this
                    )
                    if (locationManager != null) {
                        location = locationManager!!.getLastKnownLocation(provider_info!!)
                        updateGPSCoordinates()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Impossible to connect to LocationManager", e)
        }

    }

    private fun updateGPSCoordinates() {
        if (location != null) {
            latitude = location!!.latitude
            longitude = location!!.longitude
        }
    }

    override fun onLocationChanged(p0: Location) {
        longitude = p0.longitude
        latitude = p0.latitude
    }
}