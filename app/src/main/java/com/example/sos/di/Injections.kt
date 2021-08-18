package com.example.sos.di

import androidx.room.Room
import com.example.sos.core.model.ContactsBD
import com.example.sos.ui.contacts.ContactsAdapter
import com.example.sos.ui.contacts.ContactsViewModel
import com.example.sos.ui.main.MainAdapter
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.experimental.dsl.viewModel
import org.koin.core.component.getScopeId
import org.koin.dsl.module

val localModule = module {
    single {
        Room.databaseBuilder(androidContext(), ContactsBD::class.java, "base.db")
            .build()
    }
    single {
        get<ContactsBD>().daoContactList()
    }

}

val adapterModule = module {
    single { MainAdapter() }
    single { ContactsAdapter() }
}

val viewModelModule = module {
    viewModel { ContactsViewModel(get()) }
}