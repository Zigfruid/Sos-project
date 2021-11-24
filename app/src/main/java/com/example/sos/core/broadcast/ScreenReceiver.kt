package com.example.sos.core.broadcast

import android.content.Intent

import android.content.BroadcastReceiver
import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import com.example.sos.core.model.SMSHelper


class ScreenReceiver : BroadcastReceiver() {
    var isReadyToSend = false
    var wasScreenOn = 0

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
        if (wasScreenOn ==1){
            timer.start()
            Log.e("Lob","timer started")
        }
        if (wasScreenOn ==4){
            timer.cancel()
            wasScreenOn =0
            SMSHelper.stopSendSms=false
            isReadyToSend = true
            Log.e("LOB", "таймер остановлен")
        }
    }

    private val timer = object : CountDownTimer(3500 , 1000){
        override fun onTick(p0: Long) {
        }
        override fun onFinish() {
            Log.e("LOB","time is over")
            wasScreenOn =0
        }
    }
}