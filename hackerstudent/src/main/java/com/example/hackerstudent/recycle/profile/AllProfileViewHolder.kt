package com.example.hackerstudent.recycle.profile

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.hackerstudent.databinding.ProfileItemClickedBinding
import com.example.hackerstudent.databinding.TitleQouteFramgentBinding
import com.example.hackerstudent.databinding.UserProfileSectionBinding
import com.example.hackerstudent.utils.ProfileDataClass
import com.example.hackerstudent.utils.hide
import com.example.hackerstudent.utils.show

sealed class AllProfileViewHolder(viewBinding: ViewBinding) :
    RecyclerView.ViewHolder(viewBinding.root) {
    class ImageHolder(private val binding: UserProfileSectionBinding) :
        AllProfileViewHolder(binding) {
        @SuppressLint("SetTextI18n")
        fun bindIt(image: ProfileDataClass.ImageHeader) {
            binding.apply {
                titleEmail.text = image.email
                titleTxt.text = "${image.firstname} ${image.lastname}"
            }
        }
    }

    class Title(private val binding: TitleQouteFramgentBinding) : AllProfileViewHolder(binding) {
        fun bindIt(title: ProfileDataClass.Title) {
            binding.apply {
                quoteTitle.hide()
                headingTitle.show()
                writerTitle.hide()
                headingTitle.text = title.title
            }
        }
    }

    class OptionItem(
        private val binding: ProfileItemClickedBinding,
        private val itemClicked: (String) -> Unit
    ) :
        AllProfileViewHolder(binding) {
        fun bindIt(optionFooter: ProfileDataClass.OptionFooter) {
            binding.apply {
                optionText.text = optionFooter.data
                root.setOnClickListener {
                    itemClicked(optionFooter.data)
                }
            }
        }
    }
}