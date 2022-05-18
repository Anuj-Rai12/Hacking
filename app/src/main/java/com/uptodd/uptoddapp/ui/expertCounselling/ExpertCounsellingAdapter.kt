package com.uptodd.uptoddapp.ui.expertCounselling

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uptodd.uptoddapp.database.expertCounselling.ExpertCounselling
import com.uptodd.uptoddapp.databinding.ExpertCounsellingRecylerviewItemBinding

class ExpertCounsellingAdapter(val clickListener: ExpertCounsellingInterface) :
    RecyclerView.Adapter<ExpertCounsellingAdapter.ExpertViewHolder>() {

    var list = listOf<ExpertCounselling>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpertViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ExpertViewHolder(ExpertCounsellingRecylerviewItemBinding.inflate(
            inflater
        ))
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ExpertViewHolder, position: Int) {
        holder.bind(list[position])
    }

    inner class ExpertViewHolder(val binding: ExpertCounsellingRecylerviewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(expCon: ExpertCounselling) {

            binding.title.text = expCon.name
            binding.date.text = expCon.sessionDate
            binding.root.setOnClickListener {
                clickListener.onClick(expCon)
            }
            if (!expCon.date.isNullOrEmpty()) {
                binding.date.text = expCon.date
            } else
                binding.dateLayout.visibility = View.GONE

            if (!expCon.status.isNullOrEmpty()) {
                binding.status.text = expCon.status
            } else
                binding.date.visibility = View.GONE

            if (expCon.status.equals("Completed")) {
                binding.status.setTextColor(Color.GREEN)
            } else if (expCon.status.equals("Missed")){
                binding.status.setTextColor(Color.RED)
            } else{
                binding.status.setTextColor(Color.BLUE)
                binding.status.text = "Upcoming"
            }
        }

    }
}

interface ExpertCounsellingInterface {
    fun onClick(exp_con: ExpertCounselling)
}
