package com.example.sos.service

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.sos.core.broadcast.RestartService
import com.example.sos.core.model.SMSHelper
import com.example.sos.core.model.SMSHelper.context
import com.example.sos.core.remote.ContactDao
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.*


open class LockService: Service(),LocationListener{

    private val dao: ContactDao by inject()
    private var wakeLock: PowerManager.WakeLock? = null
    private val mReceiver= ScreenReceiver()
    var long=""
    var lat=""
    private var locationManager: LocationManager? = null


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_USER_PRESENT)
        registerReceiver(mReceiver, filter)
        startService()
        startTimer()
        return START_STICKY

    }

    private fun startService() {
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LockService::lock").apply {
                    acquire(5 * 60 * 1000L /*5 minutes*/)
                }
            }
        Toast.makeText(this, "service is started", Toast.LENGTH_SHORT).show()
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                launch(Dispatchers.IO) {
                    getContactFromDb()
                }
                delay(3000L)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val broadcastIntent = Intent()
        stoptimertask()
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(this, RestartService::class.java)
        this.sendBroadcast(broadcastIntent)

    }

    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    open fun startTimer() {
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
               // Log.i("Count", "=========  " + counter++)
            }
        }
        timer!!.schedule(timerTask, 1000, 1000) //
    }

    open fun stoptimertask() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }


    private val compositeDisposable = CompositeDisposable()
    private fun getContactFromDb() {
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
                            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
                            if (ActivityCompat.checkSelfPermission(
                                    this,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                    this,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                return@subscribe
                            }
                            locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
                                locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, this)
                            SMSHelper.text = "Вы мой экстренный контакт. Мне нужна помощь." +
                                    "Вот мое примерное местоположение:https://www.google.com/maps/dir/$lat,$long"
                            if (mReceiver.isReadyToSend) {
                                SMSHelper.send()
                                mReceiver.isReadyToSend = false
                            }
                        },
                        {
                            Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                        }

                    )
            )
        }

    override fun onLocationChanged(location: Location) {
        try {
            lat = location.latitude.toString()
            long = location.longitude.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}