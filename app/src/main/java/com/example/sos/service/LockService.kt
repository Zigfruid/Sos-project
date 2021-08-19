package com.example.sos.service

import android.app.Service
import android.os.Binder

import android.content.BroadcastReceiver
import android.content.Context

import android.content.Intent

import android.content.IntentFilter
import android.os.Handler

import android.os.IBinder
import android.os.PowerManager
import android.widget.Toast
import com.example.sos.core.model.SMSHelper
import com.example.sos.core.remote.ContactDao
import com.example.sos.ui.main.MainFragmentViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class LockService : Service() {

    private val dao: ContactDao by inject()
    private var wakeLock: PowerManager.WakeLock? = null
    private val mReceiver = ScreenReceiver()

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
        return super.onStartCommand(intent, flags, startId)
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
        compositeDisposable.add(
            dao.getContacts()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        it.forEach {contact->
                            SMSHelper.numbers.add(contact.number)
                        }
                        SMSHelper.context = this
                        if (mReceiver.isReadyToSend){
                            Handler().postDelayed({
                                SMSHelper.send()
                            }, 300000)
                        }
                    }
                    ,
                    {
                        Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                )
        )
    }

}