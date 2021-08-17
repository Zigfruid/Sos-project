package com.example.sos.ui.contacts

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.R
import com.example.sos.core.inflate
import com.example.sos.core.model.Contact
import com.example.sos.core.model.Model
import com.example.sos.core.onClick
import com.example.sos.databinding.ItemSelectContactBinding

class ContactsAdapter: RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>() {

    var models = listOf<Contact>()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    
    inner class ContactsViewHolder(private val binding: ItemSelectContactBinding): RecyclerView.ViewHolder(binding.root){
        fun populateModel(contact: Contact){
            binding.apply {
                tvName.text = contact.name
                tvNumber.text = contact.number
                checkbox.isChecked = contact.isSelected
            }
            binding.root.onClick {
                contact.isSelected=!contact.isSelected
                binding.checkbox.isChecked = contact.isSelected
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        val itemView = parent.inflate(R.layout.item_select_contact)
        val binding = ItemSelectContactBinding.bind(itemView)
        return ContactsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        holder.populateModel(models[position])
    }

    override fun getItemCount(): Int = models.size

    
}