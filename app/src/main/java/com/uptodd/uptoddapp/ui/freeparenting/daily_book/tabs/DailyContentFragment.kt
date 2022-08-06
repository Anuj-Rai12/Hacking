package com.uptodd.uptoddapp.ui.freeparenting.daily_book.tabs

import android.os.Bundle
import android.view.View
import android.view.animation.TranslateAnimation
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.DailyContentFragmentBinding
import com.uptodd.uptoddapp.datamodel.videocontent.delete.DailyCheckData
import com.uptodd.uptoddapp.ui.freeparenting.daily_book.adaptor.DailyContentAdaptor
import com.uptodd.uptoddapp.utils.OnBottomClick
import com.uptodd.uptoddapp.utils.showImage
import com.uptodd.uptoddapp.utils.toastMsg


class DailyContentFragment(private val title:String) : Fragment(R.layout.daily_content_fragment), OnBottomClick {

    private lateinit var binding: DailyContentFragmentBinding
    private lateinit var videoContent: DailyContentAdaptor

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DailyContentFragmentBinding.bind(view)
        binding.introProgramsTxt.text=title
        binding.videoThumbnail.apply {
            context.showImage("uSTCoECm3TA", this, true)
        }
        binding.firstVideoSet.videoThumbnail.apply {
            context.showImage("uSTCoECm3TA", this, false)
        }
        binding.firstVideoSet.videoTitle.text=DailyCheckData.list.first().title
        val animation = TranslateAnimation(
            1500.0f,
            0.0f,
            0.0f,
            0.0f
        )
        animation.duration = 1500 // animation duration
        binding.firstVideoSet.videoTitle.startAnimation(animation) //your_view for mine is imageView

        setAdaptor()
    }

    private fun setAdaptor() {
        videoContent = DailyContentAdaptor()
        videoContent.itemClickListener = this
    /*    binding.suggestContentRecycleView.setHasFixedSize(true)
        binding.suggestContentRecycleView.adapter = videoContent*/
        //videoContent.submitList(DailyCheckData.list)
    }

    override fun <T> onClickListener(res: T) {
        activity?.toastMsg("$res")
    }

}