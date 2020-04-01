package com.technayak.weatherreport.services

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import androidx.annotation.Nullable
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.technayak.weatherreport.utils.SharedData

class LocationService : Service() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val request = LocationRequest()
        request.interval = 10000
        request.fastestInterval = 5000
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        /*val mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){

                    println("currentLatitute :" + location!!.latitude + "  " + location.longitude )
                    val preferedData = SharedData(applicationContext)
                    preferedData.setLatitude(location.latitude.toString())
                    preferedData.setLongitude(location.longitude.toString())


                    var data = Intent()
                    data.action = "ON_NEW_LOCATION"
                    applicationContext.sendBroadcast(data)

                    stopSelf()
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(request, mLocationCallback, null)*/


        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.

                // Update UI with location data
                try {
                    println("currentLatitute :" + location!!.latitude + "  " + location.longitude)
                    val preferedData = SharedData(applicationContext)
                    preferedData.setLatitude(location.latitude.toString())
                    preferedData.setLongitude(location.longitude.toString())
                } catch (e: Exception) {
                    println(e.printStackTrace())
                }


                var data = Intent()
                data.action = "ON_NEW_LOCATION"
                applicationContext.sendBroadcast(data)

                stopSelf()
            }

        return START_NOT_STICKY
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        stopSelf()
        super.onDestroy()
    }
}