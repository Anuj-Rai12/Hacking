package com.uptodd.uptoddapp.support.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uptodd.uptoddapp.databinding.TicketMessageLayoutBinding

class TicketMessagesAdapter : ListAdapter<TicketMessage, TicketMessagesAdapter.ViewHolder>(
    TicketMessageDiffCallback()
){


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: TicketMessageLayoutBinding): RecyclerView.ViewHolder(binding.root){
//        private val ticketMessage: TextView = binding.ticketMessageItemText
//        private val ticketLayout: FrameLayout = binding.ticketMessageItemLayout

        fun bind(item: TicketMessage) {
            binding.ticketMessageLayoutBinding = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = TicketMessageLayoutBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }

    class TicketMessageDiffCallback : DiffUtil.ItemCallback<TicketMessage>() {
        override fun areItemsTheSame(oldItem: TicketMessage, newItem: TicketMessage): Boolean {
            return oldItem.time==newItem.time
        }

        override fun areContentsTheSame(oldItem: TicketMessage, newItem: TicketMessage): Boolean {
            return oldItem==newItem
        }
    }


}