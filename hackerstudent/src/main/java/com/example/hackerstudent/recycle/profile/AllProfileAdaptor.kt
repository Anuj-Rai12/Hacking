package com.example.hackerstudent.recycle.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.hackerstudent.R
import com.example.hackerstudent.databinding.ProfileItemClickedBinding
import com.example.hackerstudent.databinding.TitleQouteFramgentBinding
import com.example.hackerstudent.databinding.UserProfileSectionBinding
import com.example.hackerstudent.utils.ProfileDataClass
import java.lang.IllegalArgumentException

class AllProfileAdaptor(private val item: (String) -> Unit) :
    ListAdapter<ProfileDataClass, AllProfileViewHolder>(profileDiff) {
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ProfileDataClass.ImageHeader -> R.layout.user_profile_section
            is ProfileDataClass.OptionFooter -> R.layout.profile_item_clicked
            is ProfileDataClass.Title -> R.layout.title_qoute_framgent
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllProfileViewHolder {
        return when (viewType) {
            R.layout.user_profile_section -> {
                AllProfileViewHolder.ImageHolder(
                    UserProfileSectionBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            R.layout.title_qoute_framgent -> {
                AllProfileViewHolder.Title(
                    TitleQouteFramgentBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            R.layout.profile_item_clicked -> {
                AllProfileViewHolder.OptionItem(
                    ProfileItemClickedBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent, false
                    ),
                    itemClicked=item
                )
            }
            else -> throw  IllegalArgumentException("No Layout Found Anuj ")
        }
    }

    override fun onBindViewHolder(holder: AllProfileViewHolder, position: Int) {
        val item = getItem(position)
        item?.let {
            when (holder) {
                is AllProfileViewHolder.ImageHolder -> holder.bindIt(item as ProfileDataClass.ImageHeader)
                is AllProfileViewHolder.OptionItem -> holder.bindIt(item as ProfileDataClass.OptionFooter)
                is AllProfileViewHolder.Title -> holder.bindIt(item as ProfileDataClass.Title)
            }
        }
    }

    companion object {
        val profileDiff = object : DiffUtil.ItemCallback<ProfileDataClass>() {
            override fun areItemsTheSame(
                oldItem: ProfileDataClass,
                newItem: ProfileDataClass
            ): Boolean {
                return getValue(oldItem) == getValue(newItem)
            }

            private fun getValue(profileDataClass: ProfileDataClass): String {
                return when (profileDataClass) {
                    is ProfileDataClass.ImageHeader -> profileDataClass.email
                    is ProfileDataClass.OptionFooter -> profileDataClass.data
                    is ProfileDataClass.Title -> profileDataClass.title
                }
            }

            override fun areContentsTheSame(
                oldItem: ProfileDataClass,
                newItem: ProfileDataClass
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}