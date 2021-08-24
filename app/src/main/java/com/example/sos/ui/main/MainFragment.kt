package com.example.sos.ui.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.BuildConfig
import com.example.sos.R
import com.example.sos.core.extentions.*
import com.example.sos.core.model.SMSHelper
import com.example.sos.core.model.Settings
import com.example.sos.databinding.FragmentMainBinding
import com.example.sos.ui.MainActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainFragment: Fragment(R.layout.fragment_main) {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val adapter : MainAdapter by inject()
    private lateinit var navController: NavController
    private val viewModel: MainFragmentViewModel by viewModel()
    private var selectedLanguage = ""
    private val settings: Settings by inject()
    var isSelected = false

    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = 10000
        fastestInterval = 5000 / 2
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(layoutInflater, container , false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        binding.rv1.adapter = adapter
        val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.rv1.addItemDecoration(MarginItemDecoration(8.dp))
        binding.rv1.layoutManager = layoutManager
        viewModel.getAllSelectedContacts()
        setObservers()
        checkGpsStatus()
        binding.fabMain.onClick {
          navController.navigate(MainFragmentDirections.actionMainFragmentToFragmentContacts())
        }

        adapter.setOnClickItemDelete { contact, position->
            viewModel.deleteSelectedContact(contact)
            adapter.deleteContact(position)
            SMSHelper.numbers.remove(contact.number)
            setObservers()
        }
        binding.btnSettings.onClick {
            val dialog = AlertDialog.Builder(requireContext())
            val points = arrayOf(getString(R.string.change_language), getString(R.string.information))
            dialog.setTitle(getString(R.string.settings))
            dialog.setItems(points){_,which->
                when(which){
                    0->{
                        val dialogLanguages = AlertDialog.Builder(requireContext())
                        dialogLanguages.setTitle(getString(R.string.change_language))
                        val languages = arrayOf(getString(R.string.russian_language),
                            getString(R.string.karakalpak_language),
                            getString(R.string.uzbek_language),
                            getString(R.string.english_language))
                        dialogLanguages.setSingleChoiceItems(languages,settings.getPosition()) { _, i ->
                            selectedLanguage = languages[i]
                            settings.setPosition(i)

                        }
                        dialogLanguages.setPositiveButton("Ok"){ _, _->
                            when(selectedLanguage){
                                getString(R.string.russian_language) ->{
                                    settings.setLanguage("ru")
                                    setLocale()
                                    settings.setFirstLanguageSelected()
                                }
                                getString(R.string.karakalpak_language) ->{
                                    settings.setLanguage("kaa")
                                    setLocale()
                                    settings.setFirstLanguageSelected()
                                }
                                getString(R.string.uzbek_language) ->{
                                    settings.setLanguage("uz")
                                    setLocale()
                                    settings.setFirstLanguageSelected()
                                }
                                getString(R.string.english_language) ->{
                                    settings.setLanguage("en")
                                    setLocale()
                                    settings.setFirstLanguageSelected()
                                }
                            }
                        }
                        dialogLanguages.show()
                    }
                    1->{
                        val dialogInfo = AlertDialog.Builder(requireContext())
                        dialogInfo.setTitle(getString(R.string.information))
                        dialogInfo.setMessage(getString(R.string.information_message))
                        dialogInfo.setPositiveButton("OK"){d, _->
                            d.dismiss()
                        }
                        dialogInfo.show()
                    }
                }
            }
            dialog.show()
        }

        binding.btnShare.onClick {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "SOS")
                var shareMessage = "\nLet me recommend you this application\n\n"
                shareMessage =
                    """
                    ${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}
                    """.trimIndent()
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                startActivity(Intent.createChooser(shareIntent, "choose one"))
            } catch (e: Exception) {
            }
        }
    }

    private fun setObservers(){
        viewModel.contacts.observe(viewLifecycleOwner, {
            when(it.status){
                ResourceState.LOADING->{
                    binding.progressBar.visibility(true)
                }
                ResourceState.SUCCESS->{
                    binding.progressBar.visibility(false)
                    if (it.data!!.isNotEmpty()){
                        binding.tvDescription.visibility = View.GONE
                    }else{
                        binding.tvDescription.visibility = View.VISIBLE
                    }
                    adapter.models = it.data
                    isSelected = true
                }
                ResourceState.ERROR->{
                    binding.progressBar.visibility(false)
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    private fun checkGpsStatus() {
        val settingsClient: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val locationManager =
            activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationSettingsRequest: LocationSettingsRequest = builder.build()
        builder.setAlwaysShow(true)

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            settingsClient
                .checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(context as Activity) {

                }
                .addOnFailureListener(requireActivity()) { e ->
                    when ((e as ApiException).statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                            try {
                                val rae = e as ResolvableApiException
                                rae.startResolutionForResult(requireActivity(), 101)
                            } catch (sie: IntentSender.SendIntentException) {
                            }
                    }
                }
        }
    }

    private fun setLocale() {
        val refresh = Intent(
            requireContext(),
            MainActivity::class.java
        )
        refresh.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        requireActivity().intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        requireActivity().intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(refresh)
    }

}