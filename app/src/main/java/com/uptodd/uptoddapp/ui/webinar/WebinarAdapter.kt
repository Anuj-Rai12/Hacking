package com.uptodd.uptoddapp.ui.webinar

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.activitysample.ActivitySample
import com.uptodd.uptoddapp.databinding.ItemActSampleBinding
import com.uptodd.uptoddapp.utils.getAdaptorViewHolderBg

class WebinarAdapter(val clickListener: WebinarInterface) :
    RecyclerView.Adapter<WebinarAdapter.ActivityViewHolder>() {

    var list = listOf<ActivitySample>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: ItemActSampleBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.item_act_sample,
            parent,
            false
        )
        return ActivityViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(list[position])
    }

    inner class ActivityViewHolder(val view: ItemActSampleBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(activitySample: ActivitySample) {
            view.rootLayoutItemActSample.setBackgroundResource(getAdaptorViewHolderBg)
            val imageUrl = "https://img.youtube.com/vi/${activitySample.video}/mqdefault.jpg"
            Glide.with(view.root.context)
                .load(Uri.parse(imageUrl))
                .transform(CenterCrop(), RoundedCorners(20))
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.default_set_android_thumbnail)
                .into(view.videoThumbnail)

            view.videoTitle.text = activitySample.title

            view.root.setOnClickListener {
                clickListener.onClick(activitySample)
            }
        }

    }
}

interface WebinarInterface {
    fun onClick(act_sample: ActivitySample)
}
