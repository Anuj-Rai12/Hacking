package com.uptodd.uptoddapp.ui.freeparenting.daily_book.adaptor


import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uptodd.uptoddapp.databinding.DailyContentSuggestionItemLayoutBinding
import com.uptodd.uptoddapp.datamodel.videocontent.delete.DailyCheckData
import com.uptodd.uptoddapp.utils.OnBottomClick
import com.uptodd.uptoddapp.utils.showImage


class DailyContentAdaptor :
    ListAdapter<DailyCheckData, DailyContentAdaptor.DailyItemContentViewHolder>(diffUtil) {

    var itemClickListener: OnBottomClick? = null

    inner class DailyItemContentViewHolder(private val binding: DailyContentSuggestionItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(data: DailyCheckData) {
            binding.videoThumbnail.apply {
                context.showImage(data.url, this,false)
            }
            binding.videoTitle.text = data.title
            binding.rootLayout.setOnClickListener {
                itemClickListener?.onClickListener(data)
            }
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<DailyCheckData>() {
            override fun areItemsTheSame(
                oldItem: DailyCheckData,
                newItem: DailyCheckData
            ) = oldItem.title == newItem.title

            override fun areContentsTheSame(
                oldItem: DailyCheckData,
                newItem: DailyCheckData
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