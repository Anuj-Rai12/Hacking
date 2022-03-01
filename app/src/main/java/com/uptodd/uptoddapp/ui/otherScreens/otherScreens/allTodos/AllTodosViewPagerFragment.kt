package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.allTodos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.tabs.TabLayoutMediator
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.adapters.TodoViewPagerAdapter
import com.uptodd.uptoddapp.databinding.FragmentAllTodosViewPagerBinding
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.models.VideosUrlResponse
import com.uptodd.uptoddapp.ui.webinars.podcastwebinar.PodcastWebinarActivity
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.ToolbarUtils
import org.json.JSONObject


class AllTodosViewPagerFragment : Fragment() {

    private lateinit var binding: FragmentAllTodosViewPagerBinding

    private var videosRespons: VideosUrlResponse?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()


        initialiseBindingAndViewModel(inflater, container)
        setupViewPager()
        ToolbarUtils.initToolbar(
            requireActivity(), binding.collapseToolbar,
            findNavController(),getString(R.string.all_activities)," Easy & Simple | Just for you",
            R.drawable.routine_icon
        )

        if(AllUtil.isUserPremium(requireContext()))
        {
            if(!AllUtil.isSubscriptionOverActive(requireContext()))
            {
                binding.upgradeButton.visibility= View.GONE
            }
        }
        binding.upgradeButton.setOnClickListener {

            it.findNavController().navigate(R.id.action_allTodosViewPagerFragment_to_upgradeFragment)
        }


        fetchTutorials(requireContext())

        binding.collapseToolbar.playTutorialIcon.setOnClickListener {

            fragmentManager?.let { it1 ->
                val intent = Intent(context, PodcastWebinarActivity::class.java)
                intent.putExtra("url", videosRespons?.routines)
                intent.putExtra("title", "All Routines")
                intent.putExtra("kit_content","")
                intent.putExtra("description","")
                startActivity(intent)
            }


        }

        binding.collapseToolbar.playTutorialIcon.visibility=View.VISIBLE

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
        val adapter = TodoViewPagerAdapter(childFragmentManager,lifecycle)
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