package com.uptodd.uptoddapp.media.resources

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.media.resource.ResourceFiles
import com.uptodd.uptoddapp.databinding.ResourceScreenItemBinding


class ResourceAdapter( val clickListener: ResourceAdapterInterface) :
    RecyclerView.Adapter<ResourceAdapter.ResourceViewHolder>() {
    var resourceList:ArrayList<ResourceFiles>?=null
    init {
        resourceList= ArrayList()
    }

    fun updateList(resourceList:ArrayList<ResourceFiles>)
    {
        this.resourceList=resourceList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ResourceViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ResourceScreenItemBinding.inflate(inflater, parent, false)
        return ResourceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResourceViewHolder, position: Int) {
        resourceList?.get(position)?.let { holder.bind(it) }
    }

    inner class ResourceViewHolder(private val binding: ResourceScreenItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ResourceFiles) {
            binding.resourceItemTitle.text = item.name
            Glide.with(binding.root).load(R.drawable.ic_baseline_library_books_24).into(binding.resourceItemImage)

            binding.root.setOnClickListener {
                clickListener.onClickPoem(item)
            }
        }

    }

    override fun getItemCount(): Int {

        return resourceList?.size!!
    }
}

interface ResourceAdapterInterface {
    fun onClickPoem(resourceFiles: ResourceFiles)
}