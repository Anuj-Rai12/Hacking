package com.uptodd.uptoddapp.ui.webinars.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.makeramen.roundedimageview.RoundedImageView
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.activitysample.ActivitySample

class SuggestedVideoAdapter(val clickListener: SuggestedVideoInterface) : RecyclerView.Adapter<SuggestedVideoAdapter.ViewHolder>() {

    var list = listOf<ActivitySample>()
    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view){
        private val videoThumbnail: RoundedImageView = view.findViewById(R.id.sugVideoImg)
        private val videoTitle: TextView = view.findViewById(R.id.sugVideoTxt)

        fun bind(activitySample: ActivitySample) {

            val imageUrl = "https://img.youtube.com/vi/${activitySample.video}/mqdefault.jpg"
            Glide.with(view.context)
                .load(Uri.parse(imageUrl))
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.default_set_android_thumbnail)
                .into(videoThumbnail)

            videoTitle.text = activitySample.title

            view.setOnClickListener {
                clickListener.onClick(activitySample)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.suggested_video_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size
}

interface SuggestedVideoInterface{
    fun onClick(act_sample: ActivitySample)
}