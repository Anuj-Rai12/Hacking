package com.uptodd.uptoddapp.ui.freeparenting.content.adaptor

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.DemoModuleItemLayoutBinding
import com.uptodd.uptoddapp.datamodel.videocontent.ModuleList
import com.uptodd.uptoddapp.datamodel.videocontent.VideoContent
import com.uptodd.uptoddapp.utils.hide
import com.uptodd.uptoddapp.utils.show

typealias moduleItemClicked = (data: ModuleList) -> Unit

class ModuleAdaptor(private val itemClicked: moduleItemClicked) :
    ListAdapter<VideoContent, ModuleAdaptor.ModuleItemListViewHolder>(diffUtil) {
    inner class ModuleItemListViewHolder(private val binding: DemoModuleItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private lateinit var adaptorVideo: VideoAdaptor
        private var isModuleOpenFlag = false

        @SuppressLint("SetTextI18n")
        fun makeData(data: VideoContent, itemClicked: moduleItemClicked, position: Int) {

            binding.titleForTxt.text = data.title

            binding.moduleTitleLayout.setOnClickListener {
                if (!isModuleOpenFlag) {
                    binding.arrow.setImageResource(R.drawable.ic_keyboard_arrow_up)
                    binding.recycleViewForModule.show()
                } else {
                    binding.arrow.setImageResource(R.drawable.ic_keyboard_arrow_down)
                    binding.recycleViewForModule.hide()
                }
                isModuleOpenFlag = !isModuleOpenFlag
            }
            binding.recycleViewForModule.apply {
                adaptorVideo = VideoAdaptor {
                    itemClicked.invoke(it)
                }
                adapter = adaptorVideo
            }
            adaptorVideo.submitList(data.module)
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<VideoContent>() {
            override fun areItemsTheSame(
                oldItem: VideoContent,
                newItem: VideoContent
            ) = oldItem.title == newItem.title

            override fun areContentsTheSame(
                oldItem: VideoContent,
                newItem: VideoContent
            ) = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleItemListViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        val view: DemoModuleItemLayoutBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.demo_module_item_layout,
            parent,
            false
        )

        return ModuleItemListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModuleItemListViewHolder, position: Int) {
        val currItem = getItem(position)
        currItem?.let {
            holder.makeData(it, itemClicked, position + 1)
        }
    }

}