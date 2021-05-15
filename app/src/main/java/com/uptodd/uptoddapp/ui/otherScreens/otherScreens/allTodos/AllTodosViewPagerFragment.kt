package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.allTodos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.adapters.TodoViewPagerAdapter
import com.uptodd.uptoddapp.databinding.FragmentAllTodosViewPagerBinding
import com.uptodd.uptoddapp.utilities.ChangeLanguage


class AllTodosViewPagerFragment : Fragment() {

    private lateinit var binding: FragmentAllTodosViewPagerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        initialiseBindingAndViewModel(inflater, container)
        setupViewPager()

        return binding.root
    }

    private fun initialiseBindingAndViewModel(inflater: LayoutInflater, container: ViewGroup?) {

        binding =
            DataBindingUtil.inflate(inflater,
                R.layout.fragment_all_todos_view_pager,
                container,
                false)

    }

    private fun setupViewPager() {
        val adapter = TodoViewPagerAdapter(this.requireActivity())
        binding.viewPager.adapter = adapter

        adapter.apply {
            addFragment(DailyFragment())
            addFragment(WeeklyFragment())
            addFragment(MonthlyFragment())
            addFragment(EssentialsFragment())
        }

        val fragmentTitleList = arrayListOf(
            getString(R.string.daily),
            getString(R.string.weekly),
            getString(R.string.monthly),
            getString(R.string.essentials)
        )

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = fragmentTitleList[position]
        }.attach()

    }


}