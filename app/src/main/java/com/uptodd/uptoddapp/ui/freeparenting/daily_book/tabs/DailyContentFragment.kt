package com.uptodd.uptoddapp.ui.freeparenting.daily_book.tabs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.DailyContentFragmentBinding
import com.uptodd.uptoddapp.datamodel.videocontent.delete.DailyCheckData
import com.uptodd.uptoddapp.utils.OnBottomClick
import com.uptodd.uptoddapp.utils.dialog.bottom_sheet.DemoBottomSheet
import com.uptodd.uptoddapp.utils.setTextViewMovingAnimation
import com.uptodd.uptoddapp.utils.showImage
import com.uptodd.uptoddapp.utils.toastMsg


class DailyContentFragment(private val title: String) : Fragment(R.layout.daily_content_fragment),
    OnBottomClick {

    private lateinit var binding: DailyContentFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DailyContentFragmentBinding.bind(view)
        binding.introProgramsTxt.text = title
        binding.videoThumbnail.apply {
            context.showImage("uSTCoECm3TA", this, true)
        }
        binding.firstVideoSet.videoThumbnail.apply {
            context.showImage("uSTCoECm3TA", this, false)
        }
        binding.firstVideoSet.videoTitle.text = DailyCheckData.list.first().title
        binding.firstVideoSet.videoTitle.setTextViewMovingAnimation()
        binding.viewMoreVideo.setOnClickListener {
            val bottomSheet = DemoBottomSheet(title)
            bottomSheet.onListener = this
            bottomSheet.show(parentFragmentManager, "MY_BOTTOM_SHEET_FOR_VIEW_CONTENT")
        }
    }

    override fun <T> onClickListener(res: T) {
        activity?.toastMsg("$res")
    }

}