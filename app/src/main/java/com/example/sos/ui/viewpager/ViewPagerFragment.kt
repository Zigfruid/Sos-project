package com.example.sos.ui.viewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.sos.R
import com.example.sos.databinding.FragmentViewpagerBinding
import com.example.sos.ui.instruction.FragmentInstructionFirst
import com.example.sos.ui.instruction.FragmentInstructionSecond
import com.example.sos.ui.instruction.FragmentInstructionThird

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
        val indicators = arrayOfNulls<ImageView>(adapter.itemCount)
        val layoutParams : LinearLayout.LayoutParams = LinearLayout.LayoutParams(50,50)
        layoutParams.setMargins(8,0,8,0)
        for (i in indicators.indices){
            indicators[i] = ImageView(requireContext())
            indicators[i].apply {
                this?.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.dot)
                )
                this?.layoutParams = layoutParams
            }
            binding.indicatorsContainer.addView(indicators[i])
        }
        currentDotPosition(0)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentDotPosition(position)
            }
        })
        return binding.root
    }

    private fun currentDotPosition(index:Int){
        val childCount = binding.indicatorsContainer.childCount
        for (i in 0 until childCount){
            val imageView = binding.indicatorsContainer[i] as ImageView
            if (i == index){
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.selected_dot)
                )
            }else{
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.dot)
                )
            }
        }
    }

}