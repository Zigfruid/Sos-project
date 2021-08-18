package com.example.sos.ui.main

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.R
import com.example.sos.core.extentions.inflate
import com.example.sos.core.remote.Contact
import com.example.sos.core.extentions.onClick
import com.example.sos.databinding.ItemContactBinding

class MainAdapter : RecyclerView.Adapter<MainAdapter.MainViewHolder>() {

    var models = mutableListOf<Contact>()
    set(value) {
        field = value
        notifyDataSetChanged()
    }


    private var onClickItemDelete:(list:Contact, position:Int) -> Unit = { _: Contact, _: Int -> }
    fun setOnClickItemDelete(onClickItemDelete:(list:Contact, position:Int)->Unit){
        this.onClickItemDelete=onClickItemDelete
    }

    fun deleteContact(position: Int){
        models.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, models.size)
    }

    inner class MainViewHolder(private val binding: ItemContactBinding): RecyclerView.ViewHolder(binding.root){
        fun populateModel(model: Contact, position: Int){
            binding.tvName.text = model.name
            binding.tvNumber.text = model.number
            binding.btnDeleteContact.onClick {
                onClickItemDelete.invoke(model, position)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val itemView = parent.inflate(R.layout.item_contact)
        val binding =ItemContactBinding.bind(itemView)
        return MainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.populateModel(models[position], position)
    }

    override fun getItemCount(): Int = models.size
}