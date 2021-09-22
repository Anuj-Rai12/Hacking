package com.example.hackerstudent.recycle.module

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import com.example.hackerstudent.R
import com.example.hackerstudent.databinding.ModuleItemLayoutBinding
import com.example.hackerstudent.databinding.ViedoItemLayoutBinding
import com.example.hackerstudent.utils.Module
import com.example.hackerstudent.utils.Video
import com.example.hackerstudent.utils.hide
import com.example.hackerstudent.utils.show

class ModuleViewHolder(private val binding: ModuleItemLayoutBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private var stringFlag: String? = null
    private var videoItemAdaptor: VideoItemAdaptor? = null
    fun bindIt(
        module: Module,
        itemAssignment: (String, String) -> Unit,
        itemVideo: (String, String) -> Unit
    ) {
        binding.apply {
            moduleTitle.text = module.module

            val data = module.video?.values?.toList()

            courseLayoutRecycle.apply {
                setHasFixedSize(true)
                videoItemAdaptor = VideoItemAdaptor({ title, link -> //Assimgnet
                    itemAssignment(title, link)
                }, { title, link ->
                    //Video
                    itemVideo(title, link)
                })
                adapter = videoItemAdaptor
            }
            videoItemAdaptor?.submitList(data)

            seeBtn.setOnClickListener {
                if (stringFlag == null) {
                    stringFlag = "show"
                    seeBtn.setImageResource(R.drawable.ic_round_add_24)
                    courseLayoutRecycle.hide()
                } else {
                    stringFlag = null
                    seeBtn.setImageResource(R.drawable.ic_round_remove_24)
                    courseLayoutRecycle.show()
                }
            }
        }
    }
}

class PaidViedViewHolder(private val binding: ViedoItemLayoutBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private var stringFlag: String? = null
    @SuppressLint("SetTextI18n")
    fun bindIt(
        video: Video,
        itemAssignment: (String, String) -> Unit,
        itemVideo: (String, String) -> Unit
    ) {
        binding.apply {
            assLayout.hide()
            video.assignment?.let {
                assLayout.show()
                assigmentTxt.text = it.title
            }
            videTitleTxt.text = "${video.title}\n${video.duration}"
            viewAssBtn.setOnClickListener {
                itemAssignment(video.assignment?.title ?: "No", video.assignment?.uri ?: "No")
            }
            videTitleTxt.setOnClickListener {
                itemVideo(video.title ?: "No", video.uri ?: "no")
            }
            assigmentBtn.setOnClickListener {
                if (stringFlag == null) {
                    stringFlag = "Hide"
                    assigmentBtn.setImageResource(R.drawable.ic_round_add_24)
                    viewAssBtn.hide()
                } else {
                    assigmentBtn.setImageResource(R.drawable.ic_round_remove_24)
                    stringFlag = null
                    viewAssBtn.show()
                }
            }
        }
    }
}