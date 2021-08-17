package com.example.sos.core.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Contact::class], version = 1, exportSchema = false)
abstract class ContactsBD:RoomDatabase(){
    abstract fun daoContactList(): ContactDao
}