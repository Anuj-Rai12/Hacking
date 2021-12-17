package com.uptodd.uptoddapp.ui.expertCounselling

import com.uptodd.uptoddapp.support.all.AllTicketsViewModel
import com.uptodd.uptoddapp.support.all.AllTicketsViewPagerAdapter


import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialSharedAxis
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.AllTicketsFragmentBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.support.all.allsessions.AllSessions
import com.uptodd.uptoddapp.support.all.expert.ExpertTeam
import com.uptodd.uptoddapp.support.all.support.SupportTeam
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.ShowInfoDialog
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import java.text.SimpleDateFormat

class HomeExpertCounselling : Fragment() {

    companion object {
        fun newInstance() = HomeExpertCounselling()
    }

    private lateinit var viewModel: AllTicketsViewModel
    var binding: AllTicketsFragmentBinding?=null
    private lateinit var uptoddDialogs: UpToddDialogs
    private val expertCounselling by lazy {
        ExpertCounsellingFragment()
    }
    private val expertTeam by lazy {
        UpComingSessionFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        uptoddDialogs = UpToddDialogs(requireContext())

        binding= DataBindingUtil.inflate(
            inflater,
            R.layout.all_tickets_fragment,
            container,
            false
        )
        binding?.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(AllTicketsViewModel::class.java)
        binding?.allTicketsBinding = viewModel

        val end=SimpleDateFormat("yyyy-MM-dd").parse(UptoddSharedPreferences.getInstance(requireContext()).getAppExpiryDate())
        if(!AllUtil.isUserPremium(requireContext()))
        {
            val upToddDialogs = UpToddDialogs(requireContext())
            upToddDialogs.showInfoDialog("24*7 Support is only for Premium Subscribers","Close",
                object :UpToddDialogs.UpToddDialogListener
                {
                    override fun onDialogButtonClicked(dialog: Dialog) {
                        dialog.dismiss()
                    }

                    override fun onDialogDismiss() {
                        view?.findNavController()?.navigateUp()
                    }

                }
            )
        }
        else if(AllUtil.isSubscriptionOver(end))
        {
            val upToddDialogs = UpToddDialogs(requireContext())
            upToddDialogs.showInfoDialog("24*7 Support is only for Premium Subscribers","Close",
                object :UpToddDialogs.UpToddDialogListener
                {
                    override fun onDialogButtonClicked(dialog: Dialog) {
                        dialog.dismiss()
                    }

                    override fun onDialogDismiss() {
                        view?.findNavController()?.navigateUp()
                    }

                }
            )
        }
        else{
            setupViewPager(binding)
        }

        return binding?.root
    }

    override fun onResume() {
        super.onResume()
        Log.i("support", "All tickets fragment")
    }

    private fun setupViewPager(binding: AllTicketsFragmentBinding?) {
        val adapter = AllTicketsViewPagerAdapter(this.requireActivity())
        binding?.allTicketsViewPager?.adapter = adapter

        adapter.apply {
            addFragment(expertTeam)
            addFragment(expertCounselling)
        }

        val fragmentTitleList = arrayListOf(
            "Upcoming Sessions",
            "Previous Sessions"
        )

        TabLayoutMediator(binding?.tabLayout!!, binding?.allTicketsViewPager) { tab, position ->
            tab.text = fragmentTitleList[position]
        }.attach()

    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AllTicketsViewModel::class.java)
    }

}