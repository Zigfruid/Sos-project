package com.example.sos.ui.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.BuildConfig
import com.example.sos.R
import com.example.sos.core.extentions.*
import com.example.sos.databinding.FragmentMainBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class MainFragment: Fragment(R.layout.fragment_main) {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val adapter : MainAdapter by inject()
    private lateinit var navController: NavController
    private val viewModel: MainFragmentViewModel by viewModel()
    private var selectedLanguage = ""
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
        checkForPermissions()
        viewModel.getAllSelectedContacts()
        setObservers()
        checkGpsStatus()
        binding.fabMain.onClick {
          navController.navigate(MainFragmentDirections.actionMainFragmentToFragmentContacts())
        }

        adapter.setOnClickItemDelete { contact, position->
            viewModel.deleteSelectedContact(contact)
            adapter.deleteContact(position)
        }
        binding.btnSettings.onClick {
            val dialog = AlertDialog.Builder(requireContext())
            val points = arrayOf(getString(R.string.change_language), getString(R.string.information))
            dialog.setTitle(getString(R.string.settings))
            dialog.setItems(points){_,which->
                when(which){
                    0->{
                        dialog.setTitle(getString(R.string.change_language))
                        val languages = arrayOf(getString(R.string.russian_language),
                            getString(R.string.karakalpak_language),
                            getString(R.string.uzbek_language),
                            getString(R.string.english_language))
                        dialog.setSingleChoiceItems(languages,0) { _, i ->
                            selectedLanguage = languages[i]
                        }
                    }
                    1->{
                        dialog.setTitle(getString(R.string.information))
                        dialog.setMessage(getString(R.string.information_message))
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
                    ${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID.toString()}
                    """.trimIndent()
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                startActivity(Intent.createChooser(shareIntent, "choose one"))
            } catch (e: Exception) {
            }
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var isGranted = true
            permissions.entries.forEach {
                if (!it.value) isGranted = false
            }
            if (isGranted) {

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
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", activity?.packageName, null)
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
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                )
            )
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
                    adapter.models = it.data!!
                }
                ResourceState.ERROR->{
                    binding.progressBar.visibility(false)
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
// fun without refreshing activity change language

    fun setLocale(lang: String?) {
       val myLocale = Locale(lang)
        val res: Resources = resources
        val dm: DisplayMetrics = res.displayMetrics
        val conf: Configuration = res.configuration
        conf.locale = myLocale
        res.updateConfiguration(conf, dm)
        onConfigurationChanged(conf)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.locale === Locale.ENGLISH) {
            Toast.makeText(requireContext(), "English", Toast.LENGTH_SHORT).show()
        } else if (newConfig.locale === Locale.FRENCH) {
            Toast.makeText(requireContext(), "French", Toast.LENGTH_SHORT).show()
        }
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

}