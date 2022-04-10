package com.uptodd.uptoddapp.ui.monthlyDevelopment

import androidx.recyclerview.widget.RecyclerView
import com.uptodd.uptoddapp.databinding.DevelopmentTrackerRecyclerViewItemBinding
import com.uptodd.uptoddapp.ui.monthlyDevelopment.models.AllResponse
import java.text.SimpleDateFormat
import java.util.*

class DevelopmentTrackerViewHolder(var binding:DevelopmentTrackerRecyclerViewItemBinding) :
    RecyclerView.ViewHolder(binding.root){

    fun bind(model:AllResponse,position:Int){
        binding.monthItemTitle.text ="Month - ${model.month} Form"
        val date: Date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(model.updated_at)
        val format=SimpleDateFormat("MMM dd,yyyy")
        binding.date.text="${format.format(date)}"

    }


}