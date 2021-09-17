package com.example.sos.ui.intro.instructions

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.sos.R
import com.example.sos.core.extentions.onClick
import com.example.sos.databinding.FragmentInstructionsThirdBinding
import com.example.sos.ui.MainActivity

class FragmentInstructionThird: Fragment(R.layout.fragment_instructions_third) {
    private var _binding:FragmentInstructionsThirdBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.button.onClick {
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInstructionsThirdBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
}