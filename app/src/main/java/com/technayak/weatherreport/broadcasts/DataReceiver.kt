package com.technayak.weatherreport.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.technayak.weatherreport.networkutils.NetworkHelper
import com.technayak.weatherreport.services.FetchDataService


class DataReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val intent1 = Intent(context, FetchDataService::class.java)
        //Start service:

        val networkHelper = NetworkHelper(context)
        //Start service:
        if (networkHelper.isWifiConnected()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent1)
            } else {
                context.startService(intent1)
            }
        }

        Toast.makeText(context, "Working", Toast.LENGTH_LONG).show()
    }
}