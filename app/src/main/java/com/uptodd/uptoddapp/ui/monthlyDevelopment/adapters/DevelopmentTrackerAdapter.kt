package com.uptodd.uptoddapp.ui.monthlyDevelopment.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uptodd.uptoddapp.databinding.DevelopmentTrackerRecyclerViewItemBinding
import com.uptodd.uptoddapp.ui.monthlyDevelopment.DevelopmentTrackerViewHolder

class DevelopmentTrackerAdapter : RecyclerView.Adapter<DevelopmentTrackerViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DevelopmentTrackerViewHolder {
        return DevelopmentTrackerViewHolder(
            DevelopmentTrackerRecyclerViewItemBinding.
        inflate(LayoutInflater.from(parent.context)))
    }

    override fun getItemCount(): Int {
       return 0
    }

    override fun onBindViewHolder(holder: DevelopmentTrackerViewHolder, position: Int) {

    }
}