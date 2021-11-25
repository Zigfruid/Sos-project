package com.example.sos.ui.contacts

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.R
import com.example.sos.core.extentions.dp
import com.example.sos.core.remote.Contact
import com.example.sos.core.extentions.onClick
import com.example.sos.databinding.FragmentContactsBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.example.sos.core.extentions.MarginItemDecoration
import com.example.sos.ui.dialog.DialogSuccess
import com.example.sos.ui.main.MainFragment
import java.util.*
import kotlin.collections.ArrayList

class ContactsFragment : ContactHelper(R.layout.fragment_contacts) {

    private var _binding:FragmentContactsBinding? = null
    private val binding get() = _binding!!
    private val adapter: ContactsAdapter by inject()
    private val viewModel: ContactsViewModel by viewModel()
    private lateinit var navController:NavController
    private val selectedContactList= mutableListOf<Contact>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController=Navigation.findNavController(view)
        binding.apply {
            rvContacts.adapter = adapter
            val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            rvContacts.addItemDecoration(MarginItemDecoration(10.dp))
            rvContacts.layoutManager = layoutManager
        }
        val list = getContactsIntoArrayList()
        val contactList : MutableList<Contact> = mutableListOf()
        for (i in 0 until list.size){
            contactList.add(Contact(i+1,list[i].getValue("Name").toString(),list[i].getValue("Number").toString(), false) )
        }
        binding.etSearchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                p0?.let {
                    if (it.isEmpty()) {
                        adapter.models = adapter.allModel
                    } else {
                        filter(it.toString())
                    }
                }
            }

        })
        contactList.sortBy { it.name }
        adapter.allModel = contactList
        adapter.setOnClickItem { contact, isSelected ->
            if (isSelected) {
                selectedContactList.add(contact)
            } else {
                selectedContactList.remove(contact)
            }
        }
        binding.btnBack.onClick {
            navController.popBackStack()
        }

        binding.btnSelectContacts.onClick {
            if (selectedContactList.isNotEmpty()){
                val mainFragment = MainFragment()
                viewModel.setSelectedContacts(selectedContactList)
                val dialog = DialogSuccess(requireContext())
                dialog.show()
                Handler(Looper.getMainLooper()).postDelayed({
                    dialog.dismiss()
                    navController.popBackStack()
                },3000)


                mainFragment.isSelected = true
            }else{
                Toast.makeText(requireContext(), getString(R.string.select_contact), Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun filter(text: String) {
        val filteredListName: ArrayList<Contact> = ArrayList()
        for (eachName in adapter.allModel) {
            if (eachName.name.lowercase(Locale.getDefault())
                    .contains(text.lowercase(Locale.getDefault()))
            ) {
                filteredListName.add(eachName)
            }
        }
        adapter.filterList(filteredListName)
    }
}