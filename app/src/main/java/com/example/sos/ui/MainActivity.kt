package com.example.sos.ui

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.sos.R
import com.example.sos.service.LockService
import com.example.sos.service.ScreenReceiver
import org.koin.android.ext.android.inject
import java.util.*
import android.app.ActivityManager
import android.util.Log
import com.example.sos.core.broadcast.RestartService
import com.example.sos.service.Actions


class MainActivity : AppCompatActivity() {
    private val settings: com.example.sos.core.model.Settings by inject()
    lateinit var locale: Locale
    var isGranted = true

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setLocale()
        checkForPermissions()
        if (isGranted) {
            actionOnService(Actions.START)
        }else{
            showDialog()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        startBackgroundProcess()
        actionOnService(Actions.START)
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

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                if (!it.value) isGranted = false
            }
            if (isGranted) {
                startService(Intent(applicationContext, LockService::class.java))
            }else{
                showDialog()
            }
        }



    private fun startBackgroundProcess(){
        val intent = Intent(this,ScreenReceiver::class.java)
        intent.action = "BackgroundProcess"
        val pendingIntent = PendingIntent.getBroadcast(this,0,intent,0)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,0,10,pendingIntent)
        finish()
    }


    private fun showDialog() {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage(getString(R.string.permission_is_required))
            setTitle(getString(R.string.permission_required_title))
            setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }.create().show()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkForPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_NETWORK_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                )
            )
        }
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
}