package com.uptodd.uptoddapp.ui.freeparenting.daily_book.tabs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.DailyContentFragmentBinding
import com.uptodd.uptoddapp.datamodel.videocontent.Content
import com.uptodd.uptoddapp.ui.freeparenting.content.repo.VideoContentRepository
import com.uptodd.uptoddapp.ui.freeparenting.daily_book.adaptor.DailyContentAdaptor
import com.uptodd.uptoddapp.utils.OnBottomClick
import com.uptodd.uptoddapp.utils.showImage
import com.uptodd.uptoddapp.utils.toastMsg


class DailyContentFragment(
    private val title: String,
    private val list: List<Content>,
    private val dbContent: MutableList<Content>,
    private val bg: Pair<Int, Int>
) :
    Fragment(R.layout.daily_content_fragment),
    OnBottomClick {

    private lateinit var binding: DailyContentFragmentBinding
    private lateinit var dailVideoAdaptor: DailyContentAdaptor

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DailyContentFragmentBinding.bind(view)
        binding.introProgramsTxt.text = title
        val firstVideo=list.first()
        binding.videoThumbnail.apply {
            if (firstVideo.type == VideoContentRepository.Companion.ItemType.VIDEO.name)
                context.showImage(list.first().url, this, true)
            else{
                setImageResource(R.drawable.music_icon)
            }
            binding.mainContainer.setBackgroundResource(bg.second)
        }
        binding.videoName.text=firstVideo.name
        setAdaptor()
    }

    private fun setAdaptor() {
        binding.suggestionPlayList.apply {
            isNestedScrollingEnabled = false
            dailVideoAdaptor = DailyContentAdaptor(bg.second)
            dailVideoAdaptor.itemClickListener = this@DailyContentFragment
            adapter = dailVideoAdaptor
        }
        dailVideoAdaptor.submitList(list)
    }

    override fun <T> onClickListener(res: T) {
        activity?.toastMsg("$res")
    }

}