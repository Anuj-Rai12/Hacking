package com.uptodd.uptoddapp.ui.freeparenting.content

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.DemoVideoContentLayoutBinding


class DemoVideoContentFragment : Fragment(R.layout.demo_video_content_layout) {
    private lateinit var binding: DemoVideoContentLayoutBinding

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DemoVideoContentLayoutBinding.bind(view)

    }


}