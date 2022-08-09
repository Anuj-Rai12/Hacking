package com.uptodd.uptoddapp.ui.freeparenting.daily_book.tabs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.DailyContentFragmentBinding
import com.uptodd.uptoddapp.datamodel.videocontent.delete.DailyCheckData
import com.uptodd.uptoddapp.ui.freeparenting.daily_book.adaptor.DailyContentAdaptor
import com.uptodd.uptoddapp.utils.OnBottomClick
import com.uptodd.uptoddapp.utils.showImage
import com.uptodd.uptoddapp.utils.toastMsg


class DailyContentFragment(private val title: String) : Fragment(R.layout.daily_content_fragment),
    OnBottomClick {

    private lateinit var binding: DailyContentFragmentBinding
    private lateinit var dailVideoAdaptor: DailyContentAdaptor

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DailyContentFragmentBinding.bind(view)
        binding.introProgramsTxt.text = title
        binding.videoThumbnail.apply {
            context.showImage("uSTCoECm3TA", this, true)
        }
        setAdaptor()
    }

    private fun setAdaptor() {
        binding.suggestionPlayList.apply {
            isNestedScrollingEnabled=false
            dailVideoAdaptor = DailyContentAdaptor()
            dailVideoAdaptor.itemClickListener = this@DailyContentFragment
            adapter = dailVideoAdaptor
        }
        dailVideoAdaptor.submitList(DailyCheckData.list)
    }

    override fun <T> onClickListener(res: T) {
        activity?.toastMsg("$res")
    }

}