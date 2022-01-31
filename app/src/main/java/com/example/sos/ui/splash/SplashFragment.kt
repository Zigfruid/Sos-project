package com.example.sos.ui.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sos.R
import com.example.sos.core.model.Settings
import com.example.sos.databinding.FragmentSplashBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashFragment: Fragment(R.layout.fragment_splash) {

    private val settings:Settings by inject()
    private val viewModel by viewModel<SplashViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.liveNext.observe(viewLifecycleOwner,{
            if (settings.checkLaunchStatic()){
                findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToMainFragment())
            }else{
                findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToViewPagerFragment())
            }
        })
    }
}