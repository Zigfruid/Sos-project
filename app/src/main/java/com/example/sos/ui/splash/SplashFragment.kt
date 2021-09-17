package com.example.sos.ui.splash

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.sos.R

class SplashFragment: Fragment(R.layout.fragment_splash) {

    lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        Handler().postDelayed({
            navController.navigate(SplashFragmentDirections.actionSplashFragmentToViewPagerFragment())
        }, 3000)
    }
}