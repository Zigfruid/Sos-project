package com.example.sos.ui.intro.viewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.sos.R
import com.example.sos.databinding.FragmentViewpagerBinding
import com.example.sos.ui.intro.instructions.FragmentInstructionFirst
import com.example.sos.ui.intro.instructions.FragmentInstructionSecond
import com.example.sos.ui.intro.instructions.FragmentInstructionThird
import com.google.android.material.tabs.TabLayoutMediator

class ViewPagerFragment: Fragment() {
    private var _binding: FragmentViewpagerBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewpagerBinding.inflate(layoutInflater, container,false)
        val fragmentList = arrayListOf(
            FragmentInstructionFirst(),
            FragmentInstructionSecond(),
            FragmentInstructionThird()
        )

        val adapter = ViewPagerAdapter(fragmentList, requireActivity().supportFragmentManager, lifecycle)
        binding.viewPager.adapter = adapter
        return binding.root
    }
}