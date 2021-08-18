package com.example.sos.ui.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sos.core.model.Contact
import com.example.sos.core.model.ContactDao
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import uz.texnopos.oneup.core.Resource

class ContactsViewModel(private val dao: ContactDao): ViewModel() {

    private var _contacts:MutableLiveData<Resource<List<Contact>>> = MutableLiveData()
    private val contacts:LiveData<Resource<List<Contact>>> get() = _contacts


    private val compositeDisposable = CompositeDisposable()

    fun getSelectedContacts(contacts:Contact){
        compositeDisposable.add(
            dao.insertContactToDB(contacts)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        )
    }

}