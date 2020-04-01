package com.technayak.weatherreport.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.technayak.weatherreport.broadcasts.DataReceiver

class AlarmUtil(val context: Context,val clearAlarm: Boolean) {

    init {
        val pendingIntent = Intent(context, DataReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (clearAlarm) {
            alarmManager.cancel(pendingIntent)
        }
        alarmManager?.set(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis()
                    + 1000  * 60 * 60 * 2,
            pendingIntent
        )
    }
}