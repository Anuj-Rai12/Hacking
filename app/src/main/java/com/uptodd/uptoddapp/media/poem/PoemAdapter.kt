package com.uptodd.uptoddapp.media.poem

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uptodd.uptoddapp.database.media.music.MusicFiles
import com.uptodd.uptoddapp.databinding.PoemListItemBinding
import com.uptodd.uptoddapp.doctor.dashboard.DoctorDashboardFragment
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.ScreenDpi
import com.uptodd.uptoddapp.utils.setLogCat

class PoemAdapter(val clickListener: PoemAdapterInterface) :
    ListAdapter<MusicFiles, PoemAdapter.PoemViewHolder>(PoemDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PoemListItemBinding.inflate(inflater, parent, false)
        return PoemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PoemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PoemViewHolder(private val binding: PoemListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MusicFiles) {
            binding.poemItemTitle.text = item.name

            val dpi = ScreenDpi(binding.poemItemImage.context).getScreenDrawableType()
            val url=AllUtil.getPoemImage(item, dpi)
            setLogCat("URL_IMG", url)
            DoctorDashboardFragment.setImageWithGlideInImageView(
                binding.poemItemImage,
                url
            )
            /*Picasso.get()
                .load(AllUtil.getPoemImage(item, dpi))
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.ic_broken_image)
                .into(binding.poemItemImage)*/

            binding.root.setOnClickListener {
                clickListener.onClickPoem(item)
            }
            binding.root.setOnLongClickListener {

                clickListener.onLongClickPoem(item)
                true
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

interface PoemAdapterInterface {
    fun onClickPoem(poem: MusicFiles)
    fun onLongClickPoem(poem: MusicFiles)
}