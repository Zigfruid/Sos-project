package com.example.sos.ui.splash

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sos.R
import com.example.sos.core.model.Settings
import org.koin.android.ext.android.inject

class SplashFragment: Fragment(R.layout.fragment_splash) {

    private val settings:Settings by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (settings.checkLaunchStatic()){
           Handler().postDelayed({
                    findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToMainFragment())
           },3000)
        }else{
            Handler().postDelayed({
                findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToViewPagerFragment())
            },3000)
        }
    }
}