package com.example.sos.core.model

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import com.example.sos.core.broadcast.SmsSentReceiver

@SuppressLint("StaticFieldLeak")
object SMSHelper {
    var cnt = 0
    var context: Context? = null
    var numbers: MutableList<String> = mutableListOf()
    var text:String=""


    fun send() {
        val sentPendingIntents = ArrayList<PendingIntent>()
        val deliveredPendingIntents = ArrayList<PendingIntent>()
        val sentPI = PendingIntent.getBroadcast(context, 0,
            Intent(context, SmsSentReceiver::class.java), 0)
        try {
            val sms = SmsManager.getDefault()
            sentPendingIntents.add(sentPI)
            val mSMSMessage: ArrayList<String> = sms.
            divideMessage(text)
            sms.sendMultipartTextMessage(numbers[cnt++], null, mSMSMessage,
                sentPendingIntents, deliveredPendingIntents)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



}