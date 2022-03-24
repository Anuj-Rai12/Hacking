package com.uptodd.uptoddapp.ui.home.homePage.adapter.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.uptodd.uptoddapp.databinding.HomeOptionsRecyclerviewItemBinding
import com.uptodd.uptoddapp.ui.home.homePage.adapter.models.OptionsItem

class HomeOptionsViewHolder(var binding:HomeOptionsRecyclerviewItemBinding) :
    RecyclerView.ViewHolder(binding.root){

        fun bind(model:OptionsItem){

            binding.ivIcon.setImageResource(model.icon)
            binding.tvTitle.text = model.title

        }


}