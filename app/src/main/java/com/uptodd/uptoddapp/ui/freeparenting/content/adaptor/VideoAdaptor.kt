package com.uptodd.uptoddapp.ui.freeparenting.content.adaptor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.DemoVideoItemLayoutBinding
import com.uptodd.uptoddapp.datamodel.videocontent.Content
import com.uptodd.uptoddapp.ui.freeparenting.content.tabs.FreeDemoVideoModuleFragments

typealias ItemVideoList = (data: Content) -> Unit

class VideoAdaptor(private val itemClicked: ItemVideoList) :
    ListAdapter<Content, VideoAdaptor.VideoItemViewHolder>(diffUtil) {
    inner class VideoItemViewHolder(private val binding: DemoVideoItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun makeData(data: Content, itemClicked: ItemVideoList, position: Int) {
            binding.serialNoTxt.text = position.toString()
            binding.title.text = data.name
            when (FreeDemoVideoModuleFragments.Companion.VideoContentTabsEnm.valueOf(data.type)) {
                FreeDemoVideoModuleFragments.Companion.VideoContentTabsEnm.VIDEO -> {
                    binding.title.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_baseline_play_circle_24,
                        0
                    )
                }
                FreeDemoVideoModuleFragments.Companion.VideoContentTabsEnm.MUSIC -> {
                    binding.title.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_baseline_music,
                        0
                    )
                }
            }
            binding.root.setOnClickListener {
                itemClicked.invoke(data)
            }
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Content>() {
            override fun areItemsTheSame(oldItem: Content, newItem: Content): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Content, newItem: Content): Boolean {
                return oldItem == newItem
            }

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