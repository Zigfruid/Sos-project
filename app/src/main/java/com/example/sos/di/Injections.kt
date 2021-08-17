package com.example.sos.di

import com.example.sos.ui.contacts.ContactsAdapter
import com.example.sos.ui.main.MainAdapter
import org.koin.dsl.module

val adapterModule = module {
    single { MainAdapter() }
    single { ContactsAdapter() }
}