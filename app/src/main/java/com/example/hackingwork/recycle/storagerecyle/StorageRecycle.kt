package com.example.hackingwork.recycle.storagerecyle

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hackingwork.databinding.VideoItemBinding
import com.example.hackingwork.utils.Video
import javax.inject.Inject

class StorageRecycle @Inject constructor() : ListAdapter<Video, StorageViewHolder>(diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StorageViewHolder {
        val binding = VideoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StorageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StorageViewHolder, position: Int) {
        val currentItem = getItem(position)
        currentItem?.let { video ->
            holder.bindIt(video)
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Video>() {
            override fun areItemsTheSame(oldItem: Video, newItem: Video) =
                oldItem.uri == newItem.uri

            override fun areContentsTheSame(oldItem: Video, newItem: Video) = oldItem == newItem
        }
    }
}

class StorageViewHolder(private val binding: VideoItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("SetTextI18n")
    fun bindIt(video: Video) {
        binding.textView.text = "${video.title}\n${video.uri}"
        video.assignment?.let {
            binding.textView.append("\n\n${it.title}\n${it.uri}")
        }
    }
}