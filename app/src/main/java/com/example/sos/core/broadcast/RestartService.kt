package com.example.sos.core.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.example.sos.service.LockService
import android.content.ComponentName

import android.os.IBinder

import android.content.ServiceConnection

class RestartService:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context!!.startForegroundService(Intent(context, LockService::class.java))
        } else {
            context!!.startService(Intent(context, LockService::class.java))
        }

    }
}