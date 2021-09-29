package com.example.sos.ui.intro.instructions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.sos.R
import com.example.sos.core.extentions.onClick
import com.example.sos.databinding.FragmentInstructionsFirstBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.android.ext.android.bind

class FragmentInstructionFirst: Fragment(R.layout.fragment_instructions_first) {
    private var _binding:FragmentInstructionsFirstBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInstructionsFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)

        binding.button.onClick {
            viewPager?.currentItem = 1
        }
    }
}