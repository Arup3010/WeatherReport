package com.technayak.weatherreport.viewmodels

import WeatherData
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.technayak.weatherreport.networkutils.NetworkHelper
import com.technayak.weatherreport.utils.AlarmUtil
import com.technayak.weatherreport.utils.SharedData
import com.technayak.weatherreport.utils.Urls


class WeatherViewModel(application: Application, isFreshCall: Boolean) :
    AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val preferedData = SharedData(context)


    var weatherData = MutableLiveData<WeatherData>()
    var isBadResponse = MutableLiveData<Boolean>()
    var isOnline = MutableLiveData<Boolean>()
    var unitType = MutableLiveData<String>()

    init {
        if (!preferedData.getWeatherData().equals("")) {
            val gson = Gson()
            weatherData.value =
                gson.fromJson(preferedData.getWeatherData().toString(), WeatherData::class.java)

            isOnline.value = true
        } else {
            if (!isFreshCall) {
                val networkHelper = NetworkHelper(context)
                if (networkHelper.isOnline()) {
                    isOnline.value = true
                    fetchWeatherData()
                } else {
                    isOnline.value = false
                }

            }
        }

        unitType.value = preferedData.getUnitType()
    }

    fun getUnitType(): LiveData<String> {
        if (unitType.value.equals("")) {
            unitType.value = preferedData.getUnitType()
        }
        return unitType
    }

    fun setUnitType(unit: String) {
        preferedData.setUnitType(unit)
        unitType.value = unit

        val networkHelper = NetworkHelper(context)
        if (networkHelper.isOnline()) {
            isOnline.value = true
            fetchWeatherData()
        } else {
            isOnline.value = false
        }
    }

    fun getWeatherData(): LiveData<WeatherData> {
        return weatherData
    }

    fun getResponseType(): LiveData<Boolean> {
        return isBadResponse
    }

    fun getOnline(): LiveData<Boolean> {
        return isOnline
    }

    fun updateData() {
        if (!preferedData.getWeatherData().equals("")) {
            val gson = Gson()
            weatherData.value =
                gson.fromJson(preferedData.getWeatherData().toString(), WeatherData::class.java)
            unitType.value = preferedData.getUnitType()
        }
    }

    fun fetchWeatherData() {

        // Request a string response from the provided URL.
        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.GET, Urls(context).getWeatherUrl(), null,
                Response.Listener { response ->
                    try {
                        println("myResponse: " + response.toString())
                        val weatherResponse: WeatherData =
                            Gson().fromJson(response.toString(), WeatherData::class.java)
                        weatherData.value = weatherResponse
                        preferedData.setWeatherData(weatherResponse)
                        unitType.value = preferedData.getUnitType()
                        isBadResponse.value = false
                    } catch (e: Exception) {
                        isBadResponse.value = true
                    }

                    AlarmUtil(context, true)
                },
                Response.ErrorListener { error ->
                    // TODO: Handle error
                    println("myError: " + error.printStackTrace())
                }
            )

// Add the request to the RequestQueue.
        jsonObjectRequest.setRetryPolicy(
            DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
        )
        Volley.newRequestQueue(context).add(jsonObjectRequest)
    }
}