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


class LockService: Service(), LocationListener{

    override fun onLocationChanged(location: Location) {}
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    private val dao: ContactDao by inject()
    private var wakeLock: PowerManager.WakeLock? = null
    private val mReceiver= ScreenReceiver()
    private var locationManager: LocationManager? = null
    private val CHANNEL_ID = "channel_id_example_01"
    private val notificationId = 101
    private lateinit var mNotifyManager: NotificationManager
    private lateinit var mBuilder: NotificationCompat.Builder
    private lateinit var notification: Notification
    private var isServiceStarted = false

    private var isGPSEnabled = false
    var isNetworkEnabled = false
    var isGPSTrackingEnabled = false
    var location: Location? = null
    private var latitude = 0.0
    private var longitude = 0.0

    companion object {
        private val TAG = LockService::class.java.name
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10 // 10 meters
        private const val MIN_TIME_BW_UPDATES = (1000 * 60 * 1 // 1 minute
                ).toLong()
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
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
        getLocation()
    }

    private var isConnectedGPS:(connect: Boolean) -> Unit = {_ ->}
    fun setGPSOff(isConnectedGPS:(connect: Boolean)->Unit){
        this.isConnectedGPS=isConnectedGPS
    }
    private var provider_info: String? = null

    private fun getLocation() {
        try {
            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
            isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (isGPSEnabled) {
                isGPSTrackingEnabled = true
                provider_info = LocationManager.GPS_PROVIDER
            } else if (isNetworkEnabled) {
                isGPSTrackingEnabled = true
                 provider_info = LocationManager.NETWORK_PROVIDER
            }

            if (provider_info!!.isNotEmpty()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                locationManager!!.requestLocationUpdates(
                    provider_info!!,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
                    this
                )
                if (locationManager != null) {
                    location = locationManager!!.getLastKnownLocation(provider_info!!)
                    updateGPSCoordinates()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Impossible to connect to LocationManager", e)
        }
    }

    private fun updateGPSCoordinates() {
        if (location != null) {
            latitude = location!!.latitude
            longitude = location!!.longitude
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startService() {
        if (isServiceStarted) return
        Toast.makeText(this, "service is started", Toast.LENGTH_SHORT).show()
        isServiceStarted = true
        setServiceState(this, ServiceState.STARTED)
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
    @RequiresApi(Build.VERSION_CODES.O)
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
                            if (isGPSTrackingEnabled) {
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
                                SMSHelper.text = getString(R.string.sms_text, latitude.toString(), longitude.toString())
                                if (mReceiver.isReadyToSend) {
                                    val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        v.vibrate(
                                            VibrationEffect.createOneShot(
                                                500,
                                                VibrationEffect.DEFAULT_AMPLITUDE
                                            )
                                        )
                                    } else {
                                        v.vibrate(500)
                                    }
                                    SMSHelper.send()
                                    mReceiver.isReadyToSend = false
                                }
                            }else{
                                isConnectedGPS.invoke(false)
                                Toast.makeText(this, getString(R.string.warning), Toast.LENGTH_SHORT).show()
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
            val name = "SOS"
            val descriptor = getString(R.string.background_work)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptor
            }
            mNotifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotifyManager.createNotificationChannel(channel)

            mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            mBuilder.setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.background_work))
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
                .setContentText(getString(R.string.background_work))
            mNotifyManager.notify(notificationId, mBuilder.build())
            startForeground(notificationId, notification)
        } else {
            mNotifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val builder: Notification.Builder = Notification.Builder(this)
            builder.setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.background_work))
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
