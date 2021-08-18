package com.example.sos.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.R
import com.example.sos.core.dp
import com.example.sos.core.model.Contact
import com.example.sos.core.onClick
import com.example.sos.databinding.FragmentContactsBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import uz.texnopos.oneup.core.MarginItemDecoration

class FragmentContacts : GetContacts(R.layout.fragment_contacts) {

    private var _binding:FragmentContactsBinding? = null
    private val binding get() = _binding!!
    private val adapter: ContactsAdapter by inject()
    private val viewModel: ContactsViewModel by viewModel()
    private lateinit var navController:NavController

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
        adapter.models = contactList
        binding.btnSelectContacts.onClick {
            adapter.setOnClickItem {contact->
                viewModel.getSelectedContacts(contact)
            }
            navController.popBackStack()
        }
    }

}