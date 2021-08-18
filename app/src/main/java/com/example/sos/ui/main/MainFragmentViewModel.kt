package com.example.sos.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sos.core.remote.Contact
import com.example.sos.core.remote.ContactDao
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import com.example.sos.core.extentions.Resource
import com.example.sos.core.model.SMSHelper

class MainFragmentViewModel(private val dao:ContactDao):ViewModel() {

    private var _contacts: MutableLiveData<Resource<MutableList<Contact>>> = MutableLiveData()
    val contacts: LiveData<Resource<MutableList<Contact>>> get() = _contacts

    private var _deleteContact: MutableLiveData<Resource<Contact>> = MutableLiveData()
    val deleteContact: LiveData<Resource<Contact>> get() = _deleteContact



    private val compositeDisposable = CompositeDisposable()
    fun getAllSelectedContacts(){
        _contacts.value = Resource.loading()
        compositeDisposable.add(
            dao.getContacts()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _contacts.value = Resource.success(it)
                },{
                    _contacts.value = Resource.error(it.localizedMessage)
                }
            )
        )
    }

    fun deleteSelectedContact(contact: Contact) {
        _deleteContact.value = Resource.loading()
        compositeDisposable.add(
            dao.deleteContact(contact.id)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                    _deleteContact.value = Resource.success(contact)
                    }
                    ,
                    {
                        _deleteContact.value = Resource.error(it.localizedMessage)
                    }
                )
        )
    }

    fun sendSms(numbers:MutableList<String>){
        SMSHelper.numbers = numbers
        SMSHelper.send()
    }
}
