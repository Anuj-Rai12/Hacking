package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.editTodoAlarms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.adapters.EditAlarmsViewPagerAdapter
import com.uptodd.uptoddapp.databinding.FragmentEditAlarmsViewPagerBinding
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.ToolbarUtils


class EditAlarmsViewPagerFragment : Fragment() {

    private lateinit var binding: FragmentEditAlarmsViewPagerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        initialiseBindingAndViewModel(inflater, container)
        ToolbarUtils.initToolbar(
            requireActivity(), binding.collapseToolbar,
            findNavController(),"Routine alarm"," Easy & Simple | Just for you",
            R.drawable.routine_alaram_icon
        )
        setupViewPager()
        if(AllUtil.isUserPremium(requireContext()))
        {
            if(!AllUtil.isSubscriptionOverActive(requireContext()))
            {
                binding.upgradeButton.visibility= View.GONE
            }
        }

        binding.upgradeButton.setOnClickListener {

            it.findNavController().navigate(R.id.action_editAlarmsViewPagerFragment_to_upgradeFragment)
        }

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