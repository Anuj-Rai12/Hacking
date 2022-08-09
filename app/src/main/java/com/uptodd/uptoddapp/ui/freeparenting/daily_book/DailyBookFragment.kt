package com.uptodd.uptoddapp.ui.freeparenting.daily_book

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.DailyBookLayoutBinding
import com.uptodd.uptoddapp.ui.freeparenting.content.viewpager.ViewPagerAdapter
import com.uptodd.uptoddapp.ui.freeparenting.daily_book.tabs.DailyContentFragment


class DailyBookFragment : Fragment(R.layout.daily_book_layout) {
    private lateinit var binding: DailyBookLayoutBinding
    private lateinit var viewPagerAdaptor: ViewPagerAdapter

    private val arrOfTabs by lazy {
        arrayListOf("Day 1", "Day 2", "Day 3", "Day 4- Test", "Day 6 Result", "Day 7")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DailyBookLayoutBinding.bind(view)
        //binding.viewPager.isUserInputEnabled = false
        setAdaptor()
        arrOfTabs.forEach {
            setFragment(it)
        }
        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, pos ->
            tab.text = arrOfTabs[pos]
        }.attach()

        setMargin()

    }

    private fun setMargin() {
        for (i in 0 until binding.tabs.tabCount - 1) {
            val tab = (binding.tabs.getChildAt(0) as ViewGroup).getChildAt(i)
            val p = tab.layoutParams as MarginLayoutParams
            p.setMargins(0, 0, 50, 0)
            tab.requestLayout()
        }
    }

    private fun setFragment(title: String) {
        viewPagerAdaptor.setFragment(DailyContentFragment("Intro to the $title Lecture"))
    }

    private fun setAdaptor() {
        viewPagerAdaptor = ViewPagerAdapter(childFragmentManager, lifecycle)
        binding.viewPager.adapter = viewPagerAdaptor
    }


}