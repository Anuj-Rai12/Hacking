package com.uptodd.uptoddapp.ui.freeparenting.purchase.tabs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.CourseVideoTabsLayoutBinding
import com.uptodd.uptoddapp.ui.freeparenting.daily_book.repo.VideoContentRepository
import com.uptodd.uptoddapp.utils.getAdaptorViewHolderBg
import com.uptodd.uptoddapp.utils.show
import com.uptodd.uptoddapp.utils.showImage

class CourseVideoTabsFragment(private val url: String, private val type: String) :
    Fragment(R.layout.course_video_tabs_layout) {

    private lateinit var binding: CourseVideoTabsLayoutBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CourseVideoTabsLayoutBinding.bind(view)
        when (VideoContentRepository.Companion.ItemType.valueOf(type)) {
            VideoContentRepository.Companion.ItemType.MUSIC -> {
                binding.courseImg.apply {
                    show()
                    context.showImage(url, this ,true)
                }
            }
            VideoContentRepository.Companion.ItemType.VIDEO -> {
                //binding.mainConstraintHolder.setBackgroundResource(getAdaptorViewHolderBg.random())
                binding.mainConstraintHolder.show()
                binding.videoThumbnail.apply {
                    context.showImage(url, this, true)
                }
            }
        }
    }

}