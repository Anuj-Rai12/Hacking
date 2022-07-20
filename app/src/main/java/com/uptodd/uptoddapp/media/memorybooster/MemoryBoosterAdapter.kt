package com.uptodd.uptoddapp.media.memorybooster

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.media.music.MusicFiles
import com.uptodd.uptoddapp.databinding.MemoryBoosterItemBinding
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.ScreenDpi


class SpeedBoosterAdapter(val clickListener: SpeedBoosterAdpaterInterface) :
    ListAdapter<MusicFiles, SpeedBoosterAdapter.SpeedBoosterViewHolder>(PoemDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpeedBoosterViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = MemoryBoosterItemBinding.inflate(inflater, parent, false)
        return SpeedBoosterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SpeedBoosterViewHolder, position: Int) {
        holder.bind(getItem(position),position)
    }

    inner class SpeedBoosterViewHolder(private val binding: MemoryBoosterItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MusicFiles,position: Int) {
            binding.speedBoosterItemTitle.text = item.name

          val dpi = ScreenDpi(binding.speedBoosterItemImage.context).getScreenDrawableType()
            Glide.with(binding.root)
                .load(AllUtil.getPoemImage(item,dpi))
                .placeholder(R.drawable.loading_animation)
                .apply(RequestOptions().override(600,600))
                .error(R.drawable.default_set_android_thumbnail)
                .into(binding.speedBoosterItemImage)

            binding.root.setOnClickListener {
                clickListener.onClickPoem(item,position)
            }
        }

    }
}

class PoemDiff : DiffUtil.ItemCallback<MusicFiles>() {
    override fun areItemsTheSame(oldItem: MusicFiles, newItem: MusicFiles): Boolean {
        return oldItem.id == newItem.id

    }

    override fun areContentsTheSame(oldItem: MusicFiles, newItem: MusicFiles): Boolean {
        return oldItem == newItem
    }

}

interface SpeedBoosterAdpaterInterface {
    fun onClickPoem(poem: MusicFiles,position: Int)
}
