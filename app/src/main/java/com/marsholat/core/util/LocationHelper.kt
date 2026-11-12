package com.marsholat.core.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import com.marsholat.domain.model.LocationData
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.ZoneId
import java.util.TimeZone
import kotlin.coroutines.resume

class LocationHelper(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationData? {
        val mgr = locationManager ?: return null
        if (!hasLocationPermission()) return null

        return suspendCancellableCoroutine { continuation ->
            var resumed = false

            // Check last known location first for instantaneous offline response
            val lastGps = runCatching { mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER) }.getOrNull()
            val lastNetwork = runCatching { mgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }.getOrNull()
            val lastPassive = runCatching { mgr.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) }.getOrNull()

            val bestLast = listOfNotNull(lastGps, lastNetwork, lastPassive).maxByOrNull { it.time }

            if (bestLast != null && System.currentTimeMillis() - bestLast.time < 30 * 60 * 1000) {
                // Return fresh location within 30 minutes
                val data = createLocationData(bestLast.latitude, bestLast.longitude)
                resumed = true
                continuation.resume(data)
                return@suspendCancellableCoroutine
            }

            // Otherwise, request single location update
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (!resumed) {
                        resumed = true
                        mgr.removeUpdates(this)
                        continuation.resume(createLocationData(location.latitude, location.longitude))
                    }
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {
                    if (!resumed) {
                        resumed = true
                        mgr.removeUpdates(this)
                        val fallback = bestLast?.let { createLocationData(it.latitude, it.longitude) }
                        continuation.resume(fallback)
                    }
                }
            }

            val isGpsEnabled = mgr.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            try {
                if (isGpsEnabled) {
                    mgr.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, Looper.getMainLooper())
                } else if (isNetworkEnabled) {
                    mgr.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, listener, Looper.getMainLooper())
                } else {
                    continuation.resume(bestLast?.let { createLocationData(it.latitude, it.longitude) })
                    resumed = true
                }
            } catch (e: Exception) {
                if (!resumed) {
                    resumed = true
                    continuation.resume(bestLast?.let { createLocationData(it.latitude, it.longitude) })
                }
            }

            continuation.invokeOnCancellation {
                runCatching { mgr.removeUpdates(listener) }
            }
        }
    }

    private fun createLocationData(latitude: Double, longitude: Double): LocationData {
        val nearest = CityDatabase.findNearestCity(latitude, longitude)
        val defaultTz = TimeZone.getDefault().id.ifEmpty { nearest.timeZoneId }
        return LocationData(
            latitude = latitude,
            longitude = longitude,
            cityName = nearest.cityName,
            region = nearest.region,
            timeZoneId = defaultTz
        )
    }
}
