package com.example.sos.ui.instruction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.sos.R
import com.example.sos.core.extentions.onClick
import com.example.sos.databinding.FragmentInstructionsSecondBinding
import com.example.sos.ui.viewpager.ViewPagerFragmentDirections

class FragmentInstructionSecond: Fragment(R.layout.fragment_instructions_second) {
    private var _binding:FragmentInstructionsSecondBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInstructionsSecondBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}