package com.technayak.weatherreport.utils

import android.content.Context

class Urls(val context: Context) {

    fun getWeatherUrl(): String {
        val preferedData = SharedData(context)
        val unitType = if (preferedData.getUnitType().equals("C", true)) "metric" else "imperial"

        if (!preferedData.getLatitude().equals("")) {
            return "http://api.openweathermap.org/data/2.5/weather?lat=" + preferedData.getLatitude() + "&lon=" + preferedData.getLongitude() + "&units=" + unitType + "&appid=5ad7218f2e11df834b0eaf3a33a39d2a"
        } else {
            return "http://api.openweathermap.org/data/2.5/weather?lat=22.4257&lon=87.3199&units=metric&appid=5ad7218f2e11df834b0eaf3a33a39d2a"
        }
    }
}