package com.uptodd.uptoddapp.ui.monthlyDevelopment

import com.uptodd.uptoddapp.support.all.AllTicketsViewModel
import com.uptodd.uptoddapp.support.all.AllTicketsViewPagerAdapter


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialSharedAxis
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FragmentHomeTrackerBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.expertCounselling.HomeExpertCounselling
import com.uptodd.uptoddapp.ui.expertCounselling.UpComingSessionFragment
import com.uptodd.uptoddapp.ui.monthlyDevelopment.childFragments.QuestionsFragment
import com.uptodd.uptoddapp.ui.monthlyDevelopment.childFragments.TipsFragment
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.models.VideosUrlResponse
import com.uptodd.uptoddapp.ui.webinars.podcastwebinar.PodcastWebinarActivity
import com.uptodd.uptoddapp.utilities.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import kotlin.math.abs

class TrackerResponseFragment : Fragment() {

    companion object {
        fun newInstance() = HomeExpertCounselling()
    }

    private var videosRespons: VideosUrlResponse?=null

    private lateinit var viewModel: AllTicketsViewModel
    var binding: FragmentHomeTrackerBinding?=null
    private lateinit var uptoddDialogs: UpToddDialogs
    private var questionsFragment:QuestionsFragment?=null
    private var tipsFragment:TipsFragment?=null

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
            R.layout.fragment_home_tracker,
            container,
            false
        )
        binding?.lifecycleOwner = this




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
            val args=TrackerResponseFragmentArgs.fromBundle(requireArguments())
            tipsFragment = TipsFragment.getInstance(args.tips)
            questionsFragment = QuestionsFragment.getInstance(args.response!!)

            ToolbarUtils.initNCToolbar(
                requireActivity(),"${args.response?.type}", binding?.toolbar!!,
                findNavController()
            )
            setupViewPager(binding)
        }

        return binding?.root
    }

    override fun onResume() {
        super.onResume()
        Log.i("support", "All tickets fragment")
    }

    private fun setupViewPager(binding: FragmentHomeTrackerBinding?) {
        val adapter = AllTicketsViewPagerAdapter(this.requireActivity())
        binding?.homeTrackerViewPager?.adapter = adapter

        adapter.apply {
            tipsFragment?.let { addFragment(it) }
            questionsFragment?.let { addFragment(it) }
        }

        val fragmentTitleList = arrayListOf(
            "Tips",
            "Form Details"
        )

        binding?.homeTrackerViewPager?.let {
            TabLayoutMediator(binding?.tabLayout!!, it) { tab, position ->
                tab.text = fragmentTitleList[position]
            }.attach()
        }

    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AllTicketsViewModel::class.java)
    }


}