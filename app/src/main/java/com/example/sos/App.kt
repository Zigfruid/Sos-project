package com.example.sos

import android.app.Application
import com.example.sos.di.adapterModule
import com.example.sos.di.localModule
import com.example.sos.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val modules = listOf(adapterModule, localModule, viewModelModule)
        startKoin { // use AndroidLogger as Koin Logger - default Level.INFO
            androidLogger()

            // use the Android context given there
            androidContext(this@App)

            // load properties from assets/koin.properties file
            androidFileProperties()

            // module list
            modules(modules)
        }
    }
}