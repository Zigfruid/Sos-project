package com.example.sos.ui.instruction

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.sos.R
import com.example.sos.core.model.Settings
import com.example.sos.databinding.FragmentInstructionsFirstBinding
import com.example.sos.service.Actions
import com.example.sos.service.LockService
import com.example.sos.ui.MainActivity
import org.koin.android.ext.android.inject

class FragmentInstructionFirst : Fragment(R.layout.fragment_instructions_first) {
    private var _binding: FragmentInstructionsFirstBinding? = null
    private val binding get() = _binding!!
    private val settings: Settings by inject()
    private var selectedLanguage = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInstructionsFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!settings.isChangeLanguage){
            val dialogLanguages = AlertDialog.Builder(requireContext())
            dialogLanguages.setTitle(getString(R.string.change_language))
            val languages = arrayOf(
                getString(R.string.russian_language),
                getString(R.string.karakalpak_language),
                getString(R.string.uzbek_language),
                getString(R.string.english_language)
            )
            dialogLanguages.setSingleChoiceItems(languages, settings.getPosition()) { _, i ->
                selectedLanguage = languages[i]
                settings.setPosition(i)

            }
            dialogLanguages.setPositiveButton("Ok") { _, _ ->
                when (selectedLanguage) {
                    getString(R.string.russian_language) -> {
                        settings.setLanguage("ru")
                        setLocale()
                        settings.setFirstLanguageSelected()
                        settings.isChangeLanguage=true
                    }
                    getString(R.string.karakalpak_language) -> {
                        settings.setLanguage("kaa")
                        setLocale()
                        settings.setFirstLanguageSelected()
                        settings.isChangeLanguage=true
                    }
                    getString(R.string.uzbek_language) -> {
                        settings.setLanguage("uz")
                        setLocale()
                        settings.setFirstLanguageSelected()
                        settings.isChangeLanguage=true
                    }
                    getString(R.string.english_language) -> {
                        settings.setLanguage("en")
                        setLocale()
                        settings.setFirstLanguageSelected()
                        settings.isChangeLanguage=true
                    }
                }
            }
            dialogLanguages.show()
        }
    }

    private fun setLocale() {
        val intent = Intent(requireActivity(), LockService::class.java)
        requireActivity().stopService(intent)
        actionOnService(Actions.START)
        val refresh = Intent(
            requireContext(),
            MainActivity::class.java
        )
        refresh.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        requireActivity().intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        requireActivity().intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(refresh)
    }

    private fun actionOnService(actions: Actions) {
        Intent(requireContext(), LockService::class.java).also { int ->
            int.action = actions.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(requireContext(), int)
                return
            }
            requireActivity().startService(int)
        }
    }
}