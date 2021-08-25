package com.example.sos.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.sos.R
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
    private val CHANNEL_ID = "channel_id_example_01"
    private val notificationId = 101
    private lateinit var mNotifyManager: NotificationManager
    private lateinit var mBuilder: NotificationCompat.Builder
    private lateinit var notification: Notification


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


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
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(this, RestartService::class.java)
        this.sendBroadcast(broadcastIntent)

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

    @RequiresApi(Build.VERSION_CODES.O)
    fun notificationChannel() {
        if (Build.VERSION.SDK >= Build.VERSION_CODES.O.toString()) {
            val name = "SOS"
            val descriptor = "Работает в фоновом режиме"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptor
            }
            mNotifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotifyManager.createNotificationChannel(channel)

            mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            mBuilder.setContentTitle(getString(R.string.app_name))
                .setContentText("Приложение работает в фоновом режиме")
                .setSmallIcon(android.R.drawable.radiobutton_on_background)
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
                .setContentText("Приложение работает в фоновом режиме")
            mNotifyManager.notify(notificationId, mBuilder.build())
            startForeground(notificationId, notification)
        } else {
            mNotifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val builder: Notification.Builder = Notification.Builder(this)
            builder.setContentTitle(getString(R.string.app_name))
                .setContentText("Приложение работает в фоновом режиме")
            notification = builder.build()
            startForeground(notificationId, notification)
        }
    }
}
