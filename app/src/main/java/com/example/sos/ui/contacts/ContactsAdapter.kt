package com.example.sos.ui.contacts

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.R
import com.example.sos.core.extentions.inflate
import com.example.sos.core.remote.Contact
import com.example.sos.core.extentions.onClick
import com.example.sos.databinding.ItemSelectContactBinding

class ContactsAdapter: RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>() {

    var models = listOf<Contact>()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    private var onClickItem:(list:Contact, isSelected: Boolean) -> Unit = {_, _ ->}
    fun setOnClickItem(onClickItem:(list:Contact, isSelected: Boolean)->Unit){
        this.onClickItem=onClickItem
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
            binding.checkbox.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    onClickItem.invoke(contact, true)
                } else {
                    onClickItem.invoke(contact, false)
                }
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