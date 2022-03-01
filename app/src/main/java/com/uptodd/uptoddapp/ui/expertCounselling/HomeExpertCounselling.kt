package com.uptodd.uptoddapp.ui.expertCounselling

import com.uptodd.uptoddapp.support.all.AllTicketsViewModel
import com.uptodd.uptoddapp.support.all.AllTicketsViewPagerAdapter


import android.app.Dialog
import android.content.Context
import android.content.Intent
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
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialSharedAxis
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.AllTicketsFragmentBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.support.all.allsessions.AllSessions
import com.uptodd.uptoddapp.support.all.expert.ExpertTeam
import com.uptodd.uptoddapp.support.all.support.SupportTeam
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.models.VideosUrlResponse
import com.uptodd.uptoddapp.ui.webinars.podcastwebinar.PodcastWebinarActivity
import com.uptodd.uptoddapp.utilities.*
import org.json.JSONObject
import java.text.SimpleDateFormat

class HomeExpertCounselling : Fragment() {

    companion object {
        fun newInstance() = HomeExpertCounselling()
    }

    private var videosRespons: VideosUrlResponse?=null

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

        ToolbarUtils.initToolbar(
            requireActivity(), binding?.collapseToolbar!!,
            findNavController(),getString(R.string.expert_counselling),"Happy Parenting Journey",
            R.drawable.counselling_icon
        )
        viewModel = ViewModelProvider(this).get(AllTicketsViewModel::class.java)
        binding?.allTicketsBinding = viewModel

        fetchTutorials(requireContext())
        binding?.collapseToolbar?.playTutorialIcon?.setOnClickListener {

            fragmentManager?.let { it1 ->
                val intent = Intent(context, PodcastWebinarActivity::class.java)
                intent.putExtra("url", videosRespons?.counselling)
                intent.putExtra("title", "Couselling Support")
                intent.putExtra("kit_content","")
                intent.putExtra("description","")
                startActivity(intent)
            }


        }

        binding?.collapseToolbar?.playTutorialIcon?.visibility=View.VISIBLE


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

    fun fetchTutorials(context: Context) {
        AndroidNetworking.get("https://uptodd.com/api/featureTutorials?userId=${AllUtil.getUserId()}")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    val data = response?.get("data") as JSONObject
                    videosRespons = AllUtil.getVideosUrlResponse(data.toString())
                }

                override fun onError(anError: ANError?) {

                }

            })
    }

}