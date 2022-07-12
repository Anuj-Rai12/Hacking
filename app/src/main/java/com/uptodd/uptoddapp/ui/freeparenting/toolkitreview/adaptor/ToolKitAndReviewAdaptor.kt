package com.uptodd.uptoddapp.ui.freeparenting.toolkitreview.adaptor

import android.content.Context
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
import com.uptodd.uptoddapp.databinding.VideoItemToolKitAndReviewLayoutBinding
import com.uptodd.uptoddapp.datamodel.toolkit.VideoForReview
import com.uptodd.uptoddapp.utils.getAdaptorViewHolderBg

typealias itemClickedVideo = (data: VideoForReview) -> Unit

class ToolKitAndReviewAdaptor(
    private val context: Context,
    private val itemClicked: itemClickedVideo
) :
    ListAdapter<VideoForReview, ToolKitAndReviewAdaptor.ToolAndReviewViewHolder>(diffUtil) {
    inner class ToolAndReviewViewHolder(private val binding: VideoItemToolKitAndReviewLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(data: VideoForReview, itemClicked: itemClickedVideo, position: Int) {
            binding.videoText.text = data.title
            binding.mainVideoLayout.setBackgroundResource(getAdaptorViewHolderBg[position % getAdaptorViewHolderBg.size])
            data.link.also { url ->
                Glide.with(context)
                    .load(Uri.parse(url))
                    .transform(CenterCrop(), RoundedCorners(20))
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.default_set_android_thumbnail)
                    .into(binding.videoThumbnail)
            }
            binding.root.setOnClickListener {
                itemClicked.invoke(data)
            }
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<VideoForReview>() {
            override fun areItemsTheSame(
                oldItem: VideoForReview,
                newItem: VideoForReview
            ) = oldItem.title == newItem.title

            override fun areContentsTheSame(
                oldItem: VideoForReview,
                newItem: VideoForReview
            ) = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolAndReviewViewHolder {
        val binding = VideoItemToolKitAndReviewLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ToolAndReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToolAndReviewViewHolder, position: Int) {
        val currItem = getItem(position)
        currItem?.let {
            holder.setData(it, itemClicked, position)
        }
    }

}