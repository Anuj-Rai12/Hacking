package com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.masterFragment

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.adapters.TodoViewPagerAdapter
import com.uptodd.uptoddapp.databinding.FragmentTodosViewPagerBinding
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.*
import com.uptodd.uptoddapp.utilities.AllUtil

// this fragment will hold the view pager which holds daily, weekly, monthly, essentials fragment
// layout text fetches data from data binding

class TodosViewPagerFragment : Fragment() {

    private val viewModel: TodosViewModel by activityViewModels()  // to connect fragment with 1 shared view model
    private lateinit var binding: FragmentTodosViewPagerBinding

    override fun onResume() {
        super.onResume()

        val supportActionBar = (requireActivity() as AppCompatActivity).supportActionBar!!
        supportActionBar.setHomeButtonEnabled(true)
        supportActionBar.setDisplayHomeAsUpEnabled(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        initialiseBindingAndViewModel(inflater, container)
        setupViewPager()
        initialiseScoreDisplay()

        if(AllUtil.isUserPremium(requireContext()))
        {
            if( !AllUtil.isSubscriptionOverActive(requireContext()))
            {
                binding.upgradeButton.visibility= View.GONE
            }
        }
        binding.upgradeButton.setOnClickListener {

            it.findNavController().navigate(R.id.action_activityPodcastFragment_to_upgradeFragment)
        }
//        viewModel.navigateToAppreciationScreenFlag.observe(viewLifecycleOwner, {
//            if (it == true) {
//                viewModel.doneNavigatingToAppreciationScreen()
//                gotoAppreciationFragment()
//            }
//        })


        val tabToDisplayPosition = viewModel.tabPosition.value
        tabToDisplayPosition?.let {
            Log.i("tabPos", tabToDisplayPosition.toString())
            binding.tabLayout.getTabAt(tabToDisplayPosition)?.select()
        }

        return binding.root
    }

    private fun gotoAppreciationFragment() {
       // findNavController().navigate(R.id.appreciationFragment)
    }

    private fun setupViewPager() {
        val adapter = TodoViewPagerAdapter(this.requireActivity())
        binding.viewPager.adapter = adapter

        adapter.apply {
            addFragment(DailyTodosFragment())
            addFragment(WeeklyTodosFragment())
            addFragment(MonthlyTodosFragment())
            addFragment(EssentialTodosFragment())
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


        // uncomment in future to control to-do tabs -->

//        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabSelected(tab: TabLayout.Tab?) {
//                when (tab?.position) {
//
//                }
//            }
//
//            override fun onTabUnselected(tab: TabLayout.Tab?) {
//                // define custom function if needed
//            }
//
//            override fun onTabReselected(tab: TabLayout.Tab?) {
//                // define custom fun if needed
//            }
//        })
    }


    private fun initialiseBindingAndViewModel(inflater: LayoutInflater, container: ViewGroup?) {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_todos_view_pager, container, false)

        binding.todosViewPagerViewModel = viewModel
        binding.lifecycleOwner = this

    }

    private fun initialiseScoreDisplay() {
        viewModel.score.observe(viewLifecycleOwner, {

            // spannable is used to give different text formatting in the same text view
            val spannable = SpannableString(viewModel.score.value)
            spannable.setSpan(
                RelativeSizeSpan(1.2f),
                0, viewModel.score.value!!.substringBefore("/").length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.darkBlue)),
                0, viewModel.score.value!!.substringBefore("/").length + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.scoreView.text = spannable
        })
    }


}