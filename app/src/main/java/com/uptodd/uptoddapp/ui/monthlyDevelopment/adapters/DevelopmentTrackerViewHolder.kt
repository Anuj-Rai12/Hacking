package com.uptodd.uptoddapp.ui.monthlyDevelopment

import androidx.recyclerview.widget.RecyclerView
import com.uptodd.uptoddapp.databinding.DevelopmentTrackerRecyclerViewItemBinding
import com.uptodd.uptoddapp.databinding.HomeOptionsRecyclerviewItemBinding
import com.uptodd.uptoddapp.ui.home.homePage.adapter.models.OptionsItem
import com.uptodd.uptoddapp.ui.monthlyDevelopment.models.AllResponse

class DevelopmentTrackerViewHolder(var binding:DevelopmentTrackerRecyclerViewItemBinding) :
    RecyclerView.ViewHolder(binding.root){

    fun bind(model:AllResponse,position:Int){
        binding.monthItemTitle.text =" ${model.month} Form"
    }


}