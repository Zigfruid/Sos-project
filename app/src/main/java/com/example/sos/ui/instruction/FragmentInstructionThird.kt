package com.example.sos.ui.instruction

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sos.R
import com.example.sos.core.extentions.onClick
import com.example.sos.databinding.FragmentInstructionsThirdBinding
import com.example.sos.ui.viewpager.ViewPagerFragmentDirections

class FragmentInstructionThird: Fragment(R.layout.fragment_instructions_third) {
    private var _binding:FragmentInstructionsThirdBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnStart.onClick {
            findNavController().navigate(ViewPagerFragmentDirections.actionViewPagerFragmentToMainFragment())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInstructionsThirdBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
}