package com.example.sos.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.sos.R
import com.example.sos.core.broadcast.ScreenReceiver
import com.example.sos.service.Actions
import com.example.sos.service.LockService
import org.koin.android.ext.android.inject
import java.util.*


class MainActivity : AppCompatActivity() {
    private val settings: com.example.sos.core.model.Settings by inject()
    private lateinit var locale: Locale

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setLocale()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        startBackgroundProcess()
        actionOnService(Actions.START)
    }

    private fun startBackgroundProcess(){
        val intent = Intent(this, ScreenReceiver::class.java)
        intent.action = "BackgroundProcess"
        val pendingIntent = PendingIntent.getBroadcast(this,0,intent,0)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,0,10,pendingIntent)
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun actionOnService(actions: Actions) {
        Intent(this, LockService::class.java).also { int ->
            int.action = actions.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this, int)
                return
            }
            startService(int)
        }
    }

    private fun setLocale() {
        val localeName = settings.getLanguage()
        locale = Locale(localeName)
        val res = resources
        val dm = res.displayMetrics
        val conf = res.configuration
        conf.locale = locale
        res.updateConfiguration(conf, dm)
    }
}