package com.uptodd.uptoddapp.ui.freeparenting.content

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.DemoVideoContentLayoutBinding
import com.uptodd.uptoddapp.datamodel.videocontent.ModuleList
import com.uptodd.uptoddapp.ui.freeparenting.content.tabs.FreeDemoVideoModuleFragments
import com.uptodd.uptoddapp.ui.freeparenting.content.viewmodel.VideoContentViewModel
import com.uptodd.uptoddapp.ui.freeparenting.content.viewpager.ViewPagerAdapter
import com.uptodd.uptoddapp.utils.toastMsg

class DemoVideoContentFragment : Fragment(R.layout.demo_video_content_layout) {
    private lateinit var binding: DemoVideoContentLayoutBinding
    private var viewPagerAdaptor: ViewPagerAdapter? = null

    private val viewModel: VideoContentViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DemoVideoContentLayoutBinding.bind(view)
        "Video content is good testing sample url!!".also { binding.videoTitle.text = it }
        setAdaptor()
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
        setFragment(FreeDemoVideoModuleFragments())
    }


    private fun changeLayoutOnLandScape() {

        val param = binding.videoViewLayout.layoutParams
        param.height = ViewGroup.LayoutParams.MATCH_PARENT
        binding.mainImageLayout.layoutParams.apply {
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        binding.mainConstraintHolder.layoutParams.apply {
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        binding.videoThumbnail.layoutParams.apply {
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        binding.musicFragmentLayout.layoutParams.apply {
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        binding.mainMusicLayout.layoutParams.apply {
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }

    }


    private fun changeLayoutOnPortrait() {

        val param = binding.videoViewLayout.layoutParams
        param.height = ViewGroup.LayoutParams.WRAP_CONTENT

        binding.mainImageLayout.layoutParams.apply {
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }

        binding.mainConstraintHolder.layoutParams.apply {
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        binding.videoThumbnail.layoutParams.apply {
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }

        binding.musicFragmentLayout.layoutParams.apply {
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        binding.mainMusicLayout.layoutParams.apply {
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            changeLayoutOnLandScape()
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            changeLayoutOnPortrait()
        }
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