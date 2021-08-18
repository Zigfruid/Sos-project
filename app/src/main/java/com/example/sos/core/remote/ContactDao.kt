package com.example.sos.core.remote

import androidx.room.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertContactToDB(contact:MutableList<Contact>) : Completable

    @Query("SELECT * FROM contact")
    fun getContacts(): Maybe<MutableList<Contact>>

    @Query("DELETE FROM contact WHERE contact.id =:id")
    fun deleteContact(id: Int): Completable
}