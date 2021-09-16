package com.example.hackerstudent.recycle.preview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hackerstudent.databinding.ProfileItemClickedBinding
import com.example.hackerstudent.utils.RequirementData
import com.example.hackerstudent.utils.hide
import com.example.hackerstudent.utils.show

class RequirementAdaptor :
    ListAdapter<RequirementData, RequirementAdaptor.RequirementViewHolder>(diffUtil) {

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<RequirementData>() {
            override fun areItemsTheSame(
                oldItem: RequirementData,
                newItem: RequirementData
            ): Boolean {
                return oldItem.list == newItem.list
            }

            override fun areContentsTheSame(
                oldItem: RequirementData,
                newItem: RequirementData
            ): Boolean {
                return oldItem==newItem
            }
        }
    }

    inner class RequirementViewHolder(private val binding: ProfileItemClickedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindIt(requirementData: RequirementData) {
            binding.apply {
                optionText.hide()
                courseReqTxt.show()
                courseReqTxt.text = requirementData.list
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequirementViewHolder {
        val binding=ProfileItemClickedBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return RequirementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequirementViewHolder, position: Int) {
        val current=getItem(position)
        current?.let {
            holder.bindIt(it)
        }
    }
}