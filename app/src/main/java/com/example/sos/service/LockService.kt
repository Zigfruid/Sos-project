package com.example.sos.service

import android.app.Service
import android.os.Binder

import android.content.BroadcastReceiver

import android.content.Intent

import android.content.IntentFilter

import android.os.IBinder
import com.example.sos.ui.main.MainFragmentViewModel


class LockService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_USER_PRESENT)
        val mReceiver: BroadcastReceiver = ScreenReceiver()
        registerReceiver(mReceiver, filter)
        return super.onStartCommand(intent, flags, startId)
    }

    inner class LocalBinder : Binder() {
        val service: LockService
            get() = this@LockService
    }
}