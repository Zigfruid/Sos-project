package com.example.sos.service

import android.content.Intent

import android.content.BroadcastReceiver
import android.content.Context
import android.net.Uri
import android.os.CountDownTimer
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import com.example.sos.ui.main.MainFragmentViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class ScreenReceiver : BroadcastReceiver() {
    var isReadyToSend = false

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> {
                wasScreenOn++
                Log.e("LOB", "$wasScreenOn")
            }
            Intent.ACTION_SCREEN_ON -> {
                wasScreenOn++
                Log.e("LOB", "$wasScreenOn")
            }
        }
        if (wasScreenOn==1){
            timer.start()
            Log.e("Lob","timer started")
        }
        if (wasScreenOn==3){
            timer.cancel()
            wasScreenOn=0
            isReadyToSend = true
            Log.e("LOB", "таймер остановлен")
        }
    }

    companion object {
        var wasScreenOn = 0
    }

    private val timer = object : CountDownTimer(3000 , 1000){
        override fun onTick(p0: Long) {
        }
        override fun onFinish() {
            Log.e("LOB","time is over")
            wasScreenOn=0

        }
    }

}