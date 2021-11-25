package com.example.sos.ui.main

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.sos.service.Actions
import com.example.sos.service.LockService
import com.google.android.gms.common.api.ApiException


class MainFragment: Fragment(R.layout.fragment_main) {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val adapter : MainAdapter by inject()
    private lateinit var navController: NavController
    private val viewModel: MainFragmentViewModel by viewModel()
    private var selectedLanguage = ""
    private val settings: Settings by inject()
    var isSelected = false
    var isGranted = true

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
        settings.setFirstLaunched()
        setObservers()
        if (isGranted){
            checkGpsStatus()
            checkForPermissions()
            actionOnService(Actions.START)
        }else{
            showDialog()
        }
        binding.fabMain.onClick {
          navController.navigate(MainFragmentDirections.actionMainFragmentToFragmentContacts())
        }
        adapter.setOnClickItemDelete { contact, position->
            viewModel.deleteSelectedContact(contact)
            adapter.deleteContact(position)
            SMSHelper.numbers.remove(contact.number)
            setObservers()
            val intent = Intent(requireActivity(), LockService::class.java)
            requireActivity().stopService(intent)
            Handler(Looper.getMainLooper()).postDelayed({
              actionOnService(Actions.START)
            },1000)
        }
        binding.btnSettings.onClick {
            val dialog = AlertDialog.Builder(requireContext())
            val points = arrayOf(getString(R.string.change_language),getString(R.string.information),getString(
                            R.string.stop_sent_sms))
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
                    2->{
                        val dialogStopSentSms = AlertDialog.Builder(requireContext())
                        dialogStopSentSms.setMessage(getString(R.string.sms_stop_dialog))
                        dialogStopSentSms.setPositiveButton(getString(R.string.yes)){ d, _->
                            SMSHelper.stopSendSms=true
                            d.dismiss()
                        }
                        dialogStopSentSms.setNegativeButton(getString(R.string.no)){ d, _->
                            d.dismiss()
                        }
                        dialogStopSentSms.show()
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setLocale() {
        val intent = Intent(requireActivity(), LockService::class.java)
        requireActivity().stopService(intent)
            actionOnService(Actions.START)
        val refresh = Intent(
            requireContext(),
            MainActivity::class.java)
        refresh.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        requireActivity().intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        requireActivity().intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(refresh)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun checkGpsStatus() {
        val settingsClient: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationSettingsRequest: LocationSettingsRequest = builder.build()
        builder.setAlwaysShow(true)
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            settingsClient
                .checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(requireActivity()) {
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

    @RequiresApi(Build.VERSION_CODES.O)
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


    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                if (!it.value) isGranted = false
            }
            if (isGranted) {
                requireContext().startService(Intent(requireContext(), LockService::class.java))
            }else{
                showDialog()
            }
        }
    private fun showDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.apply {
            setMessage(getString(R.string.permission_is_required))
            setTitle(getString(R.string.permission_required_title))
            setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }.create().show()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkForPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_NETWORK_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setTitle(getString(R.string.permission))
            dialog.setMessage(getString(R.string.information_description))
            dialog.setCancelable(false)
            dialog.setPositiveButton(getString(R.string.ok)){d, _->
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                    )
                )
                d.dismiss()
            }
            dialog.show()
        }
    }
}