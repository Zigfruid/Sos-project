package com.example.sos.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.IBinder
import android.widget.Toast


class MyService : Service() {

    var handler: Handler? = null
    var runnable: Runnable? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val filter = IntentFilter()
        filter.addAction("android.media.VOLUME_CHANGED_ACTION")
        registerReceiver(broadcastReceiver, filter)
        return START_STICKY;
    }
    override fun onCreate() {
        Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show()
        handler = Handler()
         runnable = Runnable {
            Toast.makeText(this, "Service is still running", Toast.LENGTH_LONG).show()
             runnable?.let { handler!!.postDelayed(it, 5000) }
        }
        handler!!.postDelayed(runnable!!, 5000)

    }


    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }


    var volumePrev = 0
    var cnt = 0
    val Screenoff = "android.intent.action.SCREEN_OFF"
    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
//            if ("android.media.VOLUME_CHANGED_ACTION" == intent!!.action) {
//                val volume = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", 0)
//                Log.i("absVol", "volume = $volume")
//                if (volumePrev < volume) {
//                    Log.i("absUp", "You have pressed volume up button")
//                } else {
//                    Log.i("absDown", "You have pressed volume down button")
//                }
//                volumePrev = volume
//            }
        }
    }
}