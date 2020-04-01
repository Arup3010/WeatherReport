package com.technayak.weatherreport.utils

import WeatherData
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson


class SharedData(val context: Context) {

    private var mSharedPref: SharedPreferences? = null
    val WEATHER_DATA = "WEATHER_DATA"
    val LAT = "LAT"
    val LON = "LON"
    val TYPE = "TYPE"

     init {
        mSharedPref = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
    }


    fun setWeatherData(weatherData: WeatherData?) {
        val gson = Gson()
        val prefsEditor = mSharedPref!!.edit()
        prefsEditor.putString(WEATHER_DATA, gson.toJson(weatherData))
        prefsEditor.commit()
    }

    fun clearWeatherData() {
        val gson = Gson()
        val prefsEditor = mSharedPref!!.edit()
        prefsEditor.putString(WEATHER_DATA, "")
        prefsEditor.commit()
    }

    fun getWeatherData(): String? {
        return mSharedPref!!.getString(WEATHER_DATA, "")
    }

    fun setLatitude(lat: String?) {
        val gson = Gson()
        val prefsEditor = mSharedPref!!.edit()
        prefsEditor.putString(LAT, lat)
        prefsEditor.commit()
    }

    fun getLatitude(): String? {
        return mSharedPref!!.getString(LAT, "")
    }

    fun setLongitude(lon: String?) {
        val gson = Gson()
        val prefsEditor = mSharedPref!!.edit()
        prefsEditor.putString(LON, lon)
        prefsEditor.commit()
    }

    fun getLongitude(): String? {
        return mSharedPref!!.getString(LON, "")
    }

    fun setUnitType(type: String?) {
        val prefsEditor = mSharedPref!!.edit()
        prefsEditor.putString(TYPE, type)
        prefsEditor.commit()
    }

    fun getUnitType(): String? {
        return mSharedPref!!.getString(TYPE, "C")
    }
}