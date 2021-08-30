package com.example.sos.core.broadcast

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import com.example.sos.core.model.SMSHelper

class SmsSentReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                timer.start()
            }
            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                timer.start()
            }
            SmsManager.RESULT_ERROR_NO_SERVICE -> {
                timer.start()
            }
            SmsManager.RESULT_ERROR_NULL_PDU -> {
                timer.start()
            }
            else -> {
            }
        }
    }

    private val timer = object : CountDownTimer(180000, 1000) {
        override fun onTick(p0: Long) {
            Log.e("LOB", "Time : ${p0/1000}")
        }

        override fun onFinish() {
            Log.e("LOB", "Time : ${SMSHelper.stopSendSms}")
            if(!SMSHelper.stopSendSms){
                SMSHelper.send()
                
            }
        }
    }
}