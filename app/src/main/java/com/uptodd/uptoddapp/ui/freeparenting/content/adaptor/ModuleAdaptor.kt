package com.uptodd.uptoddapp.ui.freeparenting.content.adaptor

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.DemoModuleItemLayoutBinding
import com.uptodd.uptoddapp.datamodel.videocontent.Content
import com.uptodd.uptoddapp.datamodel.videocontent.Data
import com.uptodd.uptoddapp.utils.hide
import com.uptodd.uptoddapp.utils.show

typealias moduleItemClicked = (data: Content) -> Unit

class ModuleAdaptor(private val itemClicked: moduleItemClicked) :
    ListAdapter<Data, ModuleAdaptor.ModuleItemListViewHolder>(diffUtil) {
    inner class ModuleItemListViewHolder(private val binding: DemoModuleItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private lateinit var adaptorVideo: VideoAdaptor
        private var isModuleOpenFlag = false

        @SuppressLint("SetTextI18n")
        fun makeData(data: Data, itemClicked: moduleItemClicked, position: Int) {

            binding.serialNoTxt.text="$position. "
                binding.titleForTxt.text = data.section

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

            binding.arrow.setOnClickListener {
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
            adaptorVideo.submitList(data.content)
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Data>() {
            override fun areItemsTheSame(oldItem: Data, newItem: Data): Boolean {
                return oldItem.section==newItem.section
            }

            override fun areContentsTheSame(oldItem: Data, newItem: Data): Boolean {
                return oldItem==newItem
            }

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