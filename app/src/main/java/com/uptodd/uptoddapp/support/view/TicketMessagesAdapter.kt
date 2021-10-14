package com.uptodd.uptoddapp.support.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.TicketMessageLayoutBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences

class TicketMessagesAdapter : ListAdapter<TicketMessage, TicketMessagesAdapter.ViewHolder>(
    TicketMessageDiffCallback()
){

    var isExpert=false

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item,isExpert)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: TicketMessageLayoutBinding): RecyclerView.ViewHolder(binding.root){
//        private val ticketMessage: TextView = binding.ticketMessageItemText
//        private val ticketLayout: FrameLayout = binding.ticketMessageItemLayout

        fun bind(item: TicketMessage,isExpert:Boolean=false) {
            binding.ticketMessageLayoutBinding = item
            binding.executePendingBindings()
            if(item.isSenderValue)
            {
                var url=UptoddSharedPreferences.getInstance(binding.root.context).getProfileUrl();
                if (url == "null" || url == "") {
                }
                else {
                    url="https://www.uptodd.com/uploads/$url"
                    Glide.with(binding.root.context).load(url).into(binding.profileImageSend)
                }
                binding.profileImageSend.visibility=View.VISIBLE
                binding.profileImageBorder.visibility=View.GONE
            }
            else {
                if(item.sender.isNullOrEmpty()) {
                    if (isExpert)
                        binding.senderName.text = "Expert"
                    else
                        binding.senderName.text = "Support"
                }
                else
                    binding.senderName.text=item.sender

                binding.profileImageSend.visibility=View.GONE
                binding.profileImageBorder.visibility=View.VISIBLE
                binding.profileImageBorder.setImageResource(R.drawable.app_icon)
            }
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