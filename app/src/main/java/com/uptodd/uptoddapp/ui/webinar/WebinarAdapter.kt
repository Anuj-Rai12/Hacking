package com.uptodd.uptoddapp.ui.webinar

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

class WebinarAdapter(val clickListener: WebinarInterface) :
    RecyclerView.Adapter<WebinarAdapter.ActivityViewHolder>() {

    var list = listOf<ActivitySample>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_act_sample, parent, false)
        return ActivityViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(list[position])
    }

    inner class ActivityViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val videoThumbnail: RoundedImageView =
            view.findViewById<RoundedImageView>(R.id.videoThumbnail)
        private val videoTitle: TextView = view.findViewById<TextView>(R.id.videoTitle)

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
}

interface WebinarInterface {
    fun onClick(act_sample: ActivitySample)
}
