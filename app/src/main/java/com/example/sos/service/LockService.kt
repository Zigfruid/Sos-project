package com.example.sos.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.sos.R
import com.example.sos.core.broadcast.ScreenReceiver
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

import android.os.Bundle
import android.os.VibrationEffect

import android.os.Build

import android.os.Vibrator
import android.util.Log
import com.example.sos.core.broadcast.StartReceiver
import com.example.sos.core.model.Settings
import com.example.sos.utils.LocationHelper


class LockService: Service(), LocationListener{

    override fun onLocationChanged(location: Location) {}
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    private val dao: ContactDao by inject()
    private var wakeLock: PowerManager.WakeLock? = null
    private val mReceiver = ScreenReceiver()
    private var locationManager: LocationManager? = null
    private val CHANNEL_ID = "channel_id_example_01"
    private val notificationId = 101
    private lateinit var mNotifyManager: NotificationManager
    private lateinit var mBuilder: NotificationCompat.Builder
    private lateinit var notification: Notification
    private var isServiceStarted = false
    private val settings:Settings by inject()
    private val location: LocationHelper by inject()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        setServiceState(this, ServiceState.STOPPED)
        val broadcastIntent = Intent()
        broadcastIntent.setClass(this, StartReceiver::class.java)
        this.sendBroadcast(broadcastIntent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_USER_PRESENT)
        registerReceiver(mReceiver, filter)
        startService()
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        notificationChannel()
        location.getLocation()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startService() {
        if (isServiceStarted) return
        isServiceStarted = true
        setServiceState(this, ServiceState.STARTED)
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LockService::lock").apply {
                    acquire(1000L /*10 sec*/)
                }
            }
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                launch(Dispatchers.IO) {
                    getContactFromDb()
                }
                delay(1000L)
            }
        }
    }

    private val compositeDisposable = CompositeDisposable()
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getContactFromDb() {
            compositeDisposable.add(
                dao.getContacts()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            context = this
                            if (location.latitude!=0.0 && location.longitude!=0.0) {
                                locationManager =
                                    getSystemService(LOCATION_SERVICE) as LocationManager
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
                                SMSHelper.text = when(settings.getLanguage()){
                                    "uz" -> getString(
                                        R.string.sms_text_uz,
                                        location.latitude.toString(),
                                        location.longitude.toString()
                                    )
                                    "ru" -> getString(
                                        R.string.sms_text_rus,
                                        location.latitude.toString(),
                                        location.longitude.toString()
                                    )
                                    "kaa" -> getString(
                                        R.string.sms_text_kaa,
                                        location.latitude.toString(),
                                        location.longitude.toString()
                                    )
                                    else -> getString(
                                        R.string.sms_text_english,
                                        location.latitude.toString(),
                                        location.longitude.toString()
                                    )
                                }
                                if (mReceiver.isReadyToSend) {
                                    SMSHelper.numbers.clear()
                                    it.forEach { contact ->
                                        SMSHelper.numbers.add(contact.number)
                                    }
                                    val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
                                    v.vibrate(VibrationEffect.createOneShot(
                                        500,
                                        VibrationEffect.DEFAULT_AMPLITUDE
                                    ))
                                    SMSHelper.send()
                                    mReceiver.isReadyToSend = false
                                }
                            }else{
                                location.getLocation()
                            }
                        },
                        {
                            Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                        }

                    )
            )
        }

    @RequiresApi(Build.VERSION_CODES.O)
    fun notificationChannel() {
        if (Build.VERSION.SDK >= Build.VERSION_CODES.O.toString()) {
            val name = "agi"
            val descriptor = settings.status
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptor
            }
            mNotifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotifyManager.createNotificationChannel(channel)

            mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText(settings.status)
                .setSmallIcon(R.drawable.icon_512)
                .setOngoing(true)
            notification = mBuilder.build()
            mNotifyManager.notify(notificationId, mBuilder.build())
            startForeground(notificationId, notification)
        } else {
            notifications()
        }
    }

    private fun notifications() {
        if (Build.VERSION.SDK >= Build.VERSION_CODES.O.toString()) {
            mBuilder.setContentTitle(getString(R.string.app_name))
                .setContentText(settings.status)
            mNotifyManager.notify(notificationId, mBuilder.build())
            startForeground(notificationId, notification)
        } else {
            mNotifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val builder: Notification.Builder = Notification.Builder(this)
            builder.setContentTitle(getString(R.string.app_name))
                .setContentText(settings.status)
            notification = builder.build()
            startForeground(notificationId, notification)
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        val restartServiceIntent = Intent(applicationContext, LockService::class.java).also {
            it.setPackage(packageName)
        }
        val restartServicePendingIntent: PendingIntent = PendingIntent
            .getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT)
        applicationContext.getSystemService(Context.ALARM_SERVICE)
        val alarmService: AlarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent)

    }
}
