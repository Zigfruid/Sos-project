package com.example.sos.core.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertContactToDB(contact:Contact) : Completable

    @Query("SELECT * FROM contact")
    fun getContacts(): Maybe<List<Contact>>
}