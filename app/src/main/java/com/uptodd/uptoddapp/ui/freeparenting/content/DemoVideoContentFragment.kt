package com.uptodd.uptoddapp.ui.freeparenting.content

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.tabs.TabLayoutMediator
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.DemoVideoContentLayoutBinding
import com.uptodd.uptoddapp.datamodel.videocontent.ModuleList
import com.uptodd.uptoddapp.ui.freeparenting.content.tabs.FreeDemoVideoModuleFragments
import com.uptodd.uptoddapp.ui.freeparenting.content.viewpager.ViewPagerAdapter
import com.uptodd.uptoddapp.utils.toastMsg

class DemoVideoContentFragment : Fragment(R.layout.demo_video_content_layout) {
    private lateinit var binding: DemoVideoContentLayoutBinding
    private var viewPagerAdaptor: ViewPagerAdapter? = null

    private val tabsArrayList by lazy {
        arrayListOf("Video", "Information")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DemoVideoContentLayoutBinding.bind(view)
        binding.videoTitle.text = "Video content is good testing sample url!!"

        setAdaptor()
        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, pos ->
            tab.text = tabsArrayList[pos]
        }.attach()
        "https://img.youtube.com/vi/uxSh8svEoZQ/mqdefault.jpg".also { url ->
            Glide.with(this)
                .load(Uri.parse(url))
                .transform(CenterCrop(), RoundedCorners(20))
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.default_set_android_thumbnail)
                .into(binding.videoThumbnail)
        }

    }

    private fun setAdaptor() {
        viewPagerAdaptor = ViewPagerAdapter(this)
        binding.viewPager.isUserInputEnabled = false
        setFragment(FreeDemoVideoModuleFragments(FreeDemoVideoModuleFragments.Companion.VideoContentTabsEnm.MODULE.name))
        setFragment(FreeDemoVideoModuleFragments(FreeDemoVideoModuleFragments.Companion.VideoContentTabsEnm.INFO.name))
    }


    fun setNewVideo(moduleList: ModuleList) {
        activity?.toastMsg("$moduleList")
    }

    private fun setFragment(fragment: Fragment): Int? {
        viewPagerAdaptor?.setFragment(fragment)
        binding.viewPager.adapter = viewPagerAdaptor
        return viewPagerAdaptor?.getSize()
    }

}