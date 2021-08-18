package com.example.sos.ui.contacts

import androidx.lifecycle.ViewModel
import com.example.sos.core.remote.Contact
import com.example.sos.core.remote.ContactDao
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class ContactsViewModel(private val dao: ContactDao): ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    fun setSelectedContacts(contacts:MutableList<Contact>){
        compositeDisposable.add(
            dao.insertContactToDB(contacts)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        )
    }

}