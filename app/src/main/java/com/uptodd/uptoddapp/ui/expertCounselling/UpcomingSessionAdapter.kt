package com.uptodd.uptoddapp.ui.expertCounselling

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uptodd.uptoddapp.database.expertCounselling.ExpertCounselling
import com.uptodd.uptoddapp.database.expertCounselling.UpComingSessionModel
import com.uptodd.uptoddapp.databinding.UpcomingSessionRecyclerviewItemBinding

class UpcomingSessionAdapter(val clickListener: UpcomingSessionInterface) :
    RecyclerView.Adapter<UpcomingSessionAdapter.ExpertViewHolder>() {

    var list = listOf<ExpertCounselling>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpertViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ExpertViewHolder(
            UpcomingSessionRecyclerviewItemBinding.inflate(
            inflater
        ))
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ExpertViewHolder, position: Int) {
        holder.bind(list[position])
    }

    inner class ExpertViewHolder(val binding: UpcomingSessionRecyclerviewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(upComingSession: ExpertCounselling) {

            binding.title.text=upComingSession.name
            binding.date.text=upComingSession.date
            binding.status.text=upComingSession.duration
            binding.root.setOnClickListener {
                clickListener.onClick(upComingSession)
            }
        }

    }
}

interface UpcomingSessionInterface {
    fun onClick(upcomingSession:ExpertCounselling)
}
