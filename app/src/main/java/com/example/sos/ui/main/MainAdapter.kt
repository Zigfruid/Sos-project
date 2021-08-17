package com.example.sos.ui.main

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.R
import com.example.sos.core.inflate
import com.example.sos.core.model.Model
import com.example.sos.databinding.ItemContactBinding

class MainAdapter : RecyclerView.Adapter<MainAdapter.MainViewHolder>() {

    var models = listOf<Model>()
    set(value) {
        field = value
        notifyDataSetChanged()
    }


    inner class MainViewHolder(private val binding: ItemContactBinding): RecyclerView.ViewHolder(binding.root){
        fun populateModel(model: Model){
            binding.tvName.text = model.name
            binding.tvNumber.text = model.number
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val itemView = parent.inflate(R.layout.item_contact)
        val binding =ItemContactBinding.bind(itemView)
        return MainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.populateModel(models[position])
    }

    override fun getItemCount(): Int = models.size
}