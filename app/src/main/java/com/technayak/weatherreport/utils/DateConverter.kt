package com.technayak.weatherreport.utils

import java.text.SimpleDateFormat
import java.util.*

class DateConverter {

    fun convertDate(unixDate:Long):String{
        val date = Date(unixDate * 1000L)
        val jdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z")
        jdf.setTimeZone(TimeZone.getTimeZone("UTC"))
        val value: Date = jdf.parse(jdf.format(date))


        val dateFormatter = SimpleDateFormat("EEE, MMM dd, yyyy hh:mm a") //this format changeable
        dateFormatter.timeZone = TimeZone.getDefault()
        return dateFormatter.format(value)
    }
}