package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.editTodoAlarms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.adapters.EditAlarmsViewPagerAdapter
import com.uptodd.uptoddapp.databinding.FragmentEditAlarmsViewPagerBinding
import com.uptodd.uptoddapp.utilities.ChangeLanguage


class EditAlarmsViewPagerFragment : Fragment() {

    private lateinit var binding: FragmentEditAlarmsViewPagerBinding

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
                R.layout.fragment_edit_alarms_view_pager,
                container,
                false)

    }

    private fun setupViewPager() {
        val adapter = EditAlarmsViewPagerAdapter(childFragmentManager, this.lifecycle)
        binding.viewPager.adapter = adapter

        adapter.apply {
            addFragment(EditDailyAlarmsFragment())
            addFragment(EditWeeklyAlarmsFragment())
            addFragment(EditMonthlyAlarmsFragment())
            addFragment(EditEssentialsAlarmsFragment())
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