package com.technayak.weatherreport.screens

import WeatherData
import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.technayak.weatherreport.R
import com.technayak.weatherreport.services.LocationService
import com.technayak.weatherreport.utils.DateConverter
import com.technayak.weatherreport.utils.SharedData
import com.technayak.weatherreport.viewmodels.WeatherViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.math.RoundingMode
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    // Use the 'by viewModels()' Kotlin property delegate
    // from the activity-ktx artifact
    //private val model: WeatherViewModel by viewModels()
    private val dateConverter = DateConverter()

    //private val model: WeatherViewModel by viewModels()

    private lateinit var model: WeatherViewModel

    inner class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            model.updateData()
        }
    }

    inner class LocationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            handleLocationUpdate()
        }
    }

    private fun handleLocationUpdate() {
        stopService(locationServiceIntent)
        preferedData.clearWeatherData()
        model.fetchWeatherData()
    }

    private lateinit var preferedData: SharedData
    private lateinit var dataReceiver: Receiver
    private lateinit var locationReceiver: LocationReceiver

    val df = DecimalFormat("#.##")

    val LOCATION_ON_REQUEST = 1001
    private val LOCATION_PERMISSION_REQUEST = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferedData = SharedData(applicationContext)
        dataReceiver = Receiver()
        locationReceiver = LocationReceiver()

        //val preferedData = SharedData(applicationContext)
        if (preferedData.getLatitude().equals("")) {
            model = WeatherViewModel(application, true)
            createLocationRequest()
        } else {
            model = WeatherViewModel(application, false)
        }

        setObservers()
        setListners()
    }

    private fun setListners() {
        tvCelsius.setOnClickListener {
            model.setUnitType("C")
        }

        tvFahrenheit.setOnClickListener {
            model.setUnitType("F")
        }

        tvUsePreciseLocation.setOnClickListener {
            createLocationRequest()
        }
    }

    private fun setObservers() {
        model.getWeatherData().observe(this, Observer<WeatherData> { weatherData ->
            val unitType = if (preferedData.getUnitType().equals("c", true)) " \u2103" else " \u2109"
            val windType = if (preferedData.getUnitType().equals("c", true)) " Mt/S" else " Mi/H"

            supportActionBar!!.title = weatherData.name + " / " + weatherData.sys.country
            tvWeatherDescription.text = weatherData.weather[0].description.toUpperCase()
            tvTemparature.text = df.format(weatherData.main.temp).toString() + unitType
            tvMinMaxTemparature.text = df.format(weatherData.main.temp_max)
                .toString() + unitType + " / " + df.format(weatherData.main.temp_min)
                .toString() + unitType
            tvHumidity.text = weatherData.main.humidity.toString() + "%"
            tvWind.text = df.format(weatherData.wind.speed).toString() + windType
            tvUpdatedOn.text = "Recorded on: " + dateConverter.convertDate(weatherData.dt.toLong())

            Glide.with(this)
                .load("http://openweathermap.org/img/w/" + weatherData.weather[0].icon + ".png")
                .into(ivWeatherCondition)
        })

        model.getResponseType().observe(this, Observer<Boolean> { isBadResponse ->
            if (isBadResponse) {
                Toast.makeText(
                    this@MainActivity,
                    "There is a problem in fetching Data",
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        model.getOnline().observe(this, Observer<Boolean> { isOnline ->
            if (!isOnline) {
                Toast.makeText(
                    this@MainActivity,
                    "Please check your internet connection",
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        model.getUnitType().observe(this, Observer<String> { unitType ->
            if (unitType.equals("C", true)) {
                tvCelsius.setTextColor(resources.getColor(R.color.colorTextBlue))
                tvCelsius.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);

                tvFahrenheit.setTextColor(resources.getColor(R.color.colorWhite))
                tvFahrenheit.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
            } else {
                tvFahrenheit.setTextColor(resources.getColor(R.color.colorTextBlue))
                tvFahrenheit.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);

                tvCelsius.setTextColor(resources.getColor(R.color.colorWhite))
                tvCelsius.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
            }
        })

    }

    override fun onResume() {
        super.onResume()
        registerReceiver(
            dataReceiver,
            IntentFilter("ON_NEW_WEATHER_DATA")
        );

        registerReceiver(
            locationReceiver,
            IntentFilter("ON_NEW_LOCATION")
        );
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(dataReceiver)
        unregisterReceiver(locationReceiver)
    }


    fun createLocationRequest() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest).setAlwaysShow(true)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST
                )
            } else {
                startLocationService()
            }
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this@MainActivity, LOCATION_ON_REQUEST)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(locationServiceIntent)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LOCATION_ON_REQUEST && resultCode == Activity.RESULT_OK) {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST
                )
                return
            } else {
                startLocationService()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationService()
        }
    }

    private lateinit var locationServiceIntent: Intent
    private fun startLocationService() {
        locationServiceIntent = Intent(this, LocationService::class.java)
        startService(locationServiceIntent)
    }
}
