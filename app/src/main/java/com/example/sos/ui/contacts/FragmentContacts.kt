package com.example.sos.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.R
import com.example.sos.core.dp
import com.example.sos.databinding.FragmentContactsBinding
import org.koin.android.ext.android.inject
import uz.texnopos.oneup.core.MarginItemDecoration

class FragmentContacts : Fragment(R.layout.fragment_contacts) {

    private var _binding:FragmentContactsBinding? = null
    private val binding get() = _binding!!
    private val adapter: ContactsAdapter by inject()


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
        binding.apply {
            rvContacts.adapter = adapter
            val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            rvContacts.addItemDecoration(MarginItemDecoration(8.dp))
            rvContacts.layoutManager = layoutManager
        }
    }
}