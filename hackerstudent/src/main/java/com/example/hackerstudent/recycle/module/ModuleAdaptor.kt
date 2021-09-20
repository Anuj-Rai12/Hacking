package com.example.hackerstudent.recycle.module

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.hackerstudent.databinding.ModuleItemLayoutBinding
import com.example.hackerstudent.databinding.ViedoItemLayoutBinding
import com.example.hackerstudent.utils.Module
import com.example.hackerstudent.utils.Video

class ModuleAdaptor(
    private val itemAssignment: (String, String) -> Unit,
    private val itemVideo: (String, String) -> Unit
) : PagingDataAdapter<Module, ModuleViewHolder>(diffUtil) {

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Module>() {
            override fun areItemsTheSame(oldItem: Module, newItem: Module): Boolean {
                return oldItem.module == newItem.module
            }

            override fun areContentsTheSame(oldItem: Module, newItem: Module): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val binding =
            ModuleItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ModuleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val cur = getItem(position)
        cur?.let {
            holder.bindIt(it, itemAssignment, itemVideo)
        }
    }
}


class VideoItemAdaptor(
    private val itemAssignment: (String, String) -> Unit,
    private val itemVideo: (String, String) -> Unit
) : ListAdapter<Video, PaidViedViewHolder>(diffUtil) {
    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Video>() {
            override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean {
                return oldItem.title == newItem.title
            }

            override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaidViedViewHolder {
        val binding =
            ViedoItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaidViedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaidViedViewHolder, position: Int) {
        val item = getItem(position)
        item?.let {
            holder.bindIt(it, itemAssignment, itemVideo)
        }
    }
}