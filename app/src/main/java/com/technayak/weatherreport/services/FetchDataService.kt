package com.technayak.weatherreport.services

import WeatherData
import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.technayak.weatherreport.utils.AlarmUtil
import com.technayak.weatherreport.screens.MainActivity
import com.technayak.weatherreport.R
import com.technayak.weatherreport.utils.SharedData
import com.technayak.weatherreport.utils.Urls


class FetchDataService : Service() {

    val CHANNEL_ID: String = "ForegroundServiceChannel"

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //val input = intent.getStringExtra("inputExtra")
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WeatherReport")
            .setContentText("Updating Data")
            .setSmallIcon(R.drawable.ic_humidity)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)
        //do heavy work on a background thread

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, Urls(applicationContext).getWeatherUrl(), null,
            Response.Listener { response ->
                //textView.text = "Response: %s".format(response.toString())
                println("myResponse: " + response.toString())
                val weatherResponse: WeatherData =
                    Gson().fromJson(response.toString(), WeatherData::class.java)
                val preferedData = SharedData(applicationContext)
                preferedData.setWeatherData(weatherResponse)

                AlarmUtil(
                    applicationContext,
                    true
                )

                var data = Intent()
                data.action = "ON_NEW_WEATHER_DATA"
                applicationContext.sendBroadcast(data)

                stopSelf()
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
        Volley.newRequestQueue(applicationContext).add(jsonObjectRequest)
        //stopSelf();
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}