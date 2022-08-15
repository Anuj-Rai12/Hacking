package com.uptodd.uptoddapp.ui.freeparenting.daily_book

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.DailyBookLayoutBinding
import com.uptodd.uptoddapp.datamodel.videocontent.Content
import com.uptodd.uptoddapp.datamodel.videocontent.VideoContentList
import com.uptodd.uptoddapp.ui.freeparenting.content.viewpager.ViewPagerAdapter
import com.uptodd.uptoddapp.ui.freeparenting.daily_book.tabs.DailyContentFragment
import com.uptodd.uptoddapp.ui.freeparenting.daily_book.viewmodel.DailyBookVideoModel
import com.uptodd.uptoddapp.utils.ApiResponseWrapper
import com.uptodd.uptoddapp.utils.dialog.showDialogBox
import com.uptodd.uptoddapp.utils.hide
import com.uptodd.uptoddapp.utils.setLogCat
import com.uptodd.uptoddapp.utils.show


class DailyBookFragment : Fragment(R.layout.daily_book_layout) {
    private lateinit var binding: DailyBookLayoutBinding
    private lateinit var viewPagerAdaptor: ViewPagerAdapter
    private val viewModel: DailyBookVideoModel by viewModels()
    private val arrayOfVideoContentDb = mutableListOf<Content>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DailyBookLayoutBinding.bind(view)
        //binding.viewPager.isUserInputEnabled = false
        viewModel.event.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { msg ->
                showErrorDialog(msg)
            }
        }
        fetchDataFromDb()
        fetchDataFromApi()
        setVideoTabItem()
        setMargin()

    }

    private fun setVideoTabItem() {
        viewModel.tabListAndContent.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                setAdaptor()
                it.forEach { res ->
                    setFragment(res.first, res.second)
                }
                binding.progressForVideoContent.hide()
                binding.progressForTabContent.hide()
                TabLayoutMediator(binding.tabs, binding.viewPager) { tab, pos ->
                    tab.text = it[pos].first
                }.attach()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun fetchDataFromDb() {
        viewModel.getVideoContentFromDb.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResponseWrapper.Error ->
                    setLogCat("FetchDataDb", "${it.exception?.localizedMessage}")
                is ApiResponseWrapper.Loading -> {
                    setLogCat("FetchDataDb", "${it.data}")
                }
                is ApiResponseWrapper.Success -> {
                    arrayOfVideoContentDb.addAll(it.data as List<Content>)
                }
            }
        }
    }

    private fun fetchDataFromApi() {
        viewModel.videoContentResponseFromApi.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResponseWrapper.Error -> {
                    binding.progressForTabContent.hide()
                    binding.progressForVideoContent.hide()
                    if (it.data == null) {
                        it.exception?.localizedMessage?.let { msg ->
                            showErrorDialog(msg)
                        }
                    } else {
                        showErrorDialog("${it.data}")
                    }
                }
                is ApiResponseWrapper.Loading -> {
                    binding.progressForTabContent.show()
                    binding.progressForVideoContent.show()
                }
                is ApiResponseWrapper.Success -> {
                    //binding.progressForTabContent.hide()
                    //binding.progressForVideoContent.hide()
                    val videoContentList = it.data as VideoContentList?
                    if (videoContentList == null) {
                        showErrorDialog("Cannot load Data")
                    } else {
                        viewModel.displayData(videoContentList)
                    }
                }
            }
        }
    }

    private fun setMargin() {
        for (i in 0 until binding.tabs.tabCount - 1) {
            val tab = (binding.tabs.getChildAt(0) as ViewGroup).getChildAt(i)
            val p = tab.layoutParams as MarginLayoutParams
            p.setMargins(0, 0, 50, 0)
            tab.requestLayout()
        }
    }

    private fun setFragment(title: String, list: List<Content>) {
        viewPagerAdaptor.setFragment(DailyContentFragment(title, list, arrayOfVideoContentDb))
    }

    private fun setAdaptor() {
        viewPagerAdaptor = ViewPagerAdapter(childFragmentManager, lifecycle)
        binding.viewPager.adapter = viewPagerAdaptor
        binding.viewPager.offscreenPageLimit = 2
    }

    private fun showErrorDialog(msg: String) {
        activity?.showDialogBox("Failed", msg, icon = R.drawable.network_error) {}
    }

    override fun onResume() {
        super.onResume()
        viewModel.getVideoContentApi()
        viewModel.getVideoContentDb()
    }

}