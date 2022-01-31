package com.example.sos.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel:ViewModel() {

    private var mutableNext:MutableLiveData<Unit> = MutableLiveData()
    val liveNext: LiveData<Unit> get() = mutableNext

    init {
        viewModelScope.launch {
            delay(3000)
            mutableNext.value=Unit
        }
    }
}