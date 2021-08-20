package com.example.sos.core.broadcast

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.telephony.SmsManager
import android.widget.Toast
import com.example.sos.core.model.SMSHelper

class SmsSentReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                timer.start()
                Toast.makeText(context, "Sms ketti", Toast.LENGTH_SHORT).show()
            }
            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                //            SMSHelper.send()
                Toast.makeText(context, "Sms ketti", Toast.LENGTH_SHORT).show()
            }
            SmsManager.RESULT_ERROR_NO_SERVICE -> {
                //              SMSHelper.send()
                Toast.makeText(context, "Sms ketti", Toast.LENGTH_SHORT).show()
            }
            SmsManager.RESULT_ERROR_NULL_PDU -> {
//                SMSHelper.send()
                Toast.makeText(context, "Sms ketti", Toast.LENGTH_SHORT).show()
            }
            else -> {
            }
        }
    }

    private val timer = object : CountDownTimer(15000, 1000) {
        override fun onTick(p0: Long) {
        }

        override fun onFinish() {
            SMSHelper.send()
        }
    }
}