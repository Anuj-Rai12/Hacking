package com.uptodd.uptoddapp.ui.freeparenting.content.adaptor

import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.DemoVideoItemLayoutBinding
import com.uptodd.uptoddapp.datamodel.videocontent.ModuleList

typealias ItemVideoList = (data: ModuleList) -> Unit

class VideoAdaptor(private val itemClicked: ItemVideoList) :
    ListAdapter<ModuleList, VideoAdaptor.VideoItemViewHolder>(diffUtil) {
    inner class VideoItemViewHolder(private val binding: DemoVideoItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun makeData(data: ModuleList, itemClicked: ItemVideoList, position: Int) {
            binding.serialNoTxt.text = position.toString()
            binding.title.text = data.title
            binding.root.setOnClickListener {
                itemClicked.invoke(data)
            }
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ModuleList>() {
            override fun areItemsTheSame(
                oldItem: ModuleList,
                newItem: ModuleList
            ) = oldItem.title == newItem.title

            override fun areContentsTheSame(
                oldItem: ModuleList,
                newItem: ModuleList
            ) = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: DemoVideoItemLayoutBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.demo_video_item_layout,
            parent,
            false
        )
        return VideoItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoItemViewHolder, position: Int) {
        val currItem = getItem(position)
        currItem?.let {
            holder.makeData(it, itemClicked, position + 1)
        }
    }

}