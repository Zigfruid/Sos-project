package com.example.sos.di

import androidx.room.Room
import com.example.sos.core.model.Settings
import com.example.sos.core.remote.ContactsBD
import com.example.sos.ui.contacts.ContactsAdapter
import com.example.sos.ui.contacts.ContactsViewModel
import com.example.sos.ui.main.MainAdapter
import com.example.sos.ui.main.MainFragmentViewModel
import com.example.sos.ui.splash.SplashViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val localModule = module {
    single {
        Room.databaseBuilder(androidContext(), ContactsBD::class.java, "base.db")
            .build()
    }
    single {
        get<ContactsBD>().daoContactList()
    }
    single { Settings(androidApplication().applicationContext) }

}

val adapterModule = module {
    single { MainAdapter() }
    single { ContactsAdapter() }
}

val viewModelModule = module {
    viewModel { ContactsViewModel(get()) }
    viewModel { MainFragmentViewModel(get()) }
    viewModel { SplashViewModel() }
}