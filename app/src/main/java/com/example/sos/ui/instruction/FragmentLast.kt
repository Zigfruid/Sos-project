package com.example.sos.ui.instruction

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sos.R
import com.example.sos.ui.viewpager.ViewPagerFragmentDirections

class FragmentLast: Fragment(R.layout.fragment_last) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findNavController().navigate(ViewPagerFragmentDirections.actionViewPagerFragmentToMainFragment())
    }
}