package com.example.sos.core.broadcast

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.widget.Toast

class SmsSentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                Toast.makeText(context, "Sms sent", Toast.LENGTH_SHORT).show()
//                timer.start()
            }
            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                Toast.makeText(context, "Sms sent", Toast.LENGTH_SHORT).show()
//                timer.start()
            }
            SmsManager.RESULT_ERROR_NO_SERVICE -> {
                Toast.makeText(context, "Sms sent", Toast.LENGTH_SHORT).show()
//                timer.start()
            }
            SmsManager.RESULT_ERROR_NULL_PDU -> {
                Toast.makeText(context, "Sms sent", Toast.LENGTH_SHORT).show()
//                timer.start()
            }
            else -> {
            }
        }
    }

//    private val timer = object : CountDownTimer(1000, 1000) {
//        override fun onTick(p0: Long) {
//            Log.e("LOB", "Time : ${p0/1000}")
//        }
//
//        override fun onFinish() {
//            Log.e("LOB", "Time : ${SMSHelper.stopSendSms}")
//            if(!SMSHelper.stopSendSms){
//                SMSHelper.send()
//
//            }
//        }
//    }
}