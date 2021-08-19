package com.example.sos.service

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sos.core.extentions.Resource
import com.example.sos.core.model.SMSHelper
import com.example.sos.core.model.SMSHelper.context
import com.example.sos.core.remote.ContactDao
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class LockService : Service(){

    private val dao: ContactDao by inject()
    private var wakeLock: PowerManager.WakeLock? = null
    private val mReceiver= ScreenReceiver()
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_USER_PRESENT)
        registerReceiver(mReceiver, filter)
        return START_STICKY

    }

    private fun startService() {
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LockService::lock").apply {
                    acquire(5 * 60 * 1000L /*5 minutes*/)
                }
            }

        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                launch(Dispatchers.IO) {
                    getContactFromDb()
                }
                delay(3000L)
            }
        }
    }
    private val compositeDisposable = CompositeDisposable()
    private fun getContactFromDb() {
        val gpsTracker = GPSTracker(this)
        if (gpsTracker.canGetLocation()) {
            val stringLatitude: String = java.lang.String.valueOf(gpsTracker.latitude)
            val stringLongitude: String = java.lang.String.valueOf(gpsTracker.longitude)
            compositeDisposable.add(
                dao.getContacts()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            it.forEach { contact ->
                                SMSHelper.numbers.add(contact.number)
                            }
                            context = this
                            SMSHelper.text = "Вы мой экстренный контакт. Мне нужна помощь." +
                                    "Вот мое примерное местоположение:https://www.google.com/maps/dir/$stringLatitude,$stringLongitude"
                            if (mReceiver.isReadyToSend) {
//                            Handler().postDelayed({
//                                SMSHelper.send()
//                            }, 3000)
                                SMSHelper.send()
                            }
                        },
                        {
                            Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                        }

                    )
            )
        }
    }
}