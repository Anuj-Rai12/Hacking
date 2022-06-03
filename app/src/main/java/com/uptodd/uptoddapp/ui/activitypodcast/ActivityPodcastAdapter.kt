package com.uptodd.uptoddapp.ui.activitypodcast

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.makeramen.roundedimageview.RoundedImageView
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.activitypodcast.ActivityPodcast
import com.uptodd.uptoddapp.database.activitysample.ActivitySample
import com.uptodd.uptoddapp.databinding.ItemActPodcastBinding
import com.uptodd.uptoddapp.utils.getAdaptorViewHolderBg

class ActivityPodcastAdapter(val clickListener: ActivityPodcastInterface) :
    RecyclerView.Adapter<ActivityPodcastAdapter.ActivityViewHolder>() {

    var list = listOf<ActivityPodcast>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = DataBindingUtil.inflate<ItemActPodcastBinding>(
            inflater,
            R.layout.item_act_podcast,
            parent,
            false
        )
        return ActivityViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(list[position])
    }

    inner class ActivityViewHolder(val view: ItemActPodcastBinding) :
        RecyclerView.ViewHolder(view.root) {

        fun bind(activitySample: ActivityPodcast) {
            view.rootLayoutItemActPodcast.setBackgroundResource(getAdaptorViewHolderBg)
            val imageUrl = "https://img.youtube.com/vi/${activitySample.video}/mqdefault.jpg"
            Glide.with(view.root.context)
                .load(Uri.parse(imageUrl))
                .transform(CenterCrop(), RoundedCorners(20))
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.default_set_android_thumbnail)
                .into(view.itemActPodcastVideoThumbnail)

            view.itemActPodcastVideoTitle.text = activitySample.title

            view.root.setOnClickListener {
                clickListener.onClick(activitySample)
            }
        }

    }
}

interface ActivityPodcastInterface {
    fun onClick(act_sample: ActivityPodcast)
}
