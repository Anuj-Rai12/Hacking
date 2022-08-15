package com.uptodd.uptoddapp.ui.freeparenting.daily_book.adaptor


import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.DailyContentSuggestionItemLayoutBinding
import com.uptodd.uptoddapp.datamodel.videocontent.Content
import com.uptodd.uptoddapp.ui.freeparenting.content.repo.VideoContentRepository
import com.uptodd.uptoddapp.utils.OnBottomClick
import com.uptodd.uptoddapp.utils.showImage


class DailyContentAdaptor(private val bg:Int) :
    ListAdapter<Content, DailyContentAdaptor.DailyItemContentViewHolder>(diffUtil) {

    var itemClickListener: OnBottomClick? = null

    inner class DailyItemContentViewHolder(private val binding: DailyContentSuggestionItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(data: Content) {
            binding.videoThumbnail.apply {
                val type = VideoContentRepository.Companion.ItemType.VIDEO
                if (type.name == data.type) {
                    context.showImage(data.url, this, false)
                } else {
                    setImageResource(R.drawable.music_icon)
                    setBackgroundResource(bg)
                }
            }
            binding.videoTitle.text = data.name
//            binding.videoTitle.setTextViewMovingAnimation()
            binding.rootLayout.setOnClickListener {
                itemClickListener?.onClickListener(data)
            }
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Content>() {
            override fun areItemsTheSame(
                oldItem: Content,
                newItem: Content
            ) = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: Content,
                newItem: Content
            ) = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyItemContentViewHolder {
        val binding = DailyContentSuggestionItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DailyItemContentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyItemContentViewHolder, position: Int) {
        val currItem = getItem(position)
        currItem?.let {
            holder.setData(it)
        }
    }

}