package com.uptodd.uptoddapp.ui.remides.adaptor

import android.net.Uri
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.ItemActPodcastBinding
import com.uptodd.uptoddapp.ui.remides.model.Disease
import com.uptodd.uptoddapp.utils.getAdaptorViewHolderBg
import com.uptodd.uptoddapp.utils.hide

class RemediesAdaptor : ListAdapter<Disease, RemediesAdaptor.RemediesPlanViewHolder>(diffUtil) {

    var listener: RemediesInterface? = null

    inner class RemediesPlanViewHolder(private val binding: ItemActPodcastBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(data: Disease,position: Int) {
            binding.rootLayoutItemActPodcast.setBackgroundResource(getAdaptorViewHolderBg[position % getAdaptorViewHolderBg.size])
            binding.root.setOnClickListener {
                listener?.getRemedies(data)
            }
            binding.greyThumbnail.hide()

            val imageUrl = "https://img.youtube.com/vi/${data.link}/mqdefault.jpg"
            Glide.with(binding.root.context)
                .load(Uri.parse(imageUrl))
                .transform(CenterCrop(), RoundedCorners(20))
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.default_set_android_thumbnail)
                .into(binding.itemActPodcastVideoThumbnail)

            binding.itemActPodcastVideoTitle.text = data.name

        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Disease>() {
            override fun areItemsTheSame(
                oldItem: Disease,
                newItem: Disease
            ) = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: Disease,
                newItem: Disease
            ) = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RemediesPlanViewHolder {
        val binding =
            ItemActPodcastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RemediesPlanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RemediesPlanViewHolder, position: Int) {
        val currItem = getItem(position)
        currItem?.let {
            holder.setData(it,position)
        }
    }

}

interface RemediesInterface {
    fun getRemedies(response: Disease)
}