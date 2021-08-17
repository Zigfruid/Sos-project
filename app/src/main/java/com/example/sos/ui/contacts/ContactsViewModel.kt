package com.example.sos.ui.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sos.core.model.Contact
import com.example.sos.core.model.ContactDao
import io.reactivex.rxjava3.disposables.CompositeDisposable
import uz.texnopos.oneup.core.Resource

class ContactsViewModel(dao: ContactDao): ViewModel() {

    private var _contacts:MutableLiveData<Resource<List<Contact>>> = MutableLiveData()
    private val contacts:LiveData<Resource<List<Contact>>> get() = _contacts


    private val compositeDisposable = CompositeDisposable()

    fun getSelectedContacts(){
//    compositeDisposable.add(
//
//    )

    }

}