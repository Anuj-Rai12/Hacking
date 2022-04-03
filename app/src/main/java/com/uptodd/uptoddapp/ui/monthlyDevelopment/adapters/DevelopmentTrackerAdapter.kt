package com.uptodd.uptoddapp.ui.monthlyDevelopment.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facebook.all.All
import com.uptodd.uptoddapp.database.expertCounselling.ExpertCounselling
import com.uptodd.uptoddapp.databinding.DevelopmentTrackerRecyclerViewItemBinding
import com.uptodd.uptoddapp.ui.monthlyDevelopment.DevelopmentTrackerViewHolder
import com.uptodd.uptoddapp.ui.monthlyDevelopment.models.AllResponse

class DevelopmentTrackerAdapter : RecyclerView.Adapter<DevelopmentTrackerViewHolder>() {

    var trackerList = ArrayList<AllResponse>()

    var listener:DevelopmentTrackerAdapter.DevelopmentTrackerListener?=null

    fun add(list: ArrayList<AllResponse>){
        trackerList.clear()
        trackerList.addAll(list)
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DevelopmentTrackerViewHolder {
        return DevelopmentTrackerViewHolder(
            DevelopmentTrackerRecyclerViewItemBinding.
        inflate(LayoutInflater.from(parent.context)))
    }

    override fun getItemCount(): Int {
       return trackerList.size
    }

    override fun onBindViewHolder(holder: DevelopmentTrackerViewHolder, position: Int) {
        holder.bind(trackerList[position],position)
        holder.itemView.setOnClickListener {
            listener?.onClick(trackerList[position])
        }
    }


    interface DevelopmentTrackerListener {
        fun onClick(allResponse: AllResponse)
    }
}
