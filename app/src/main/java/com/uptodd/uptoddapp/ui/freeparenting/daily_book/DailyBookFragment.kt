package com.uptodd.uptoddapp.ui.freeparenting.daily_book

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.tabs.TabLayoutMediator
import com.uptodd.uptoddapp.FreeParentingDemoActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.DailyBookLayoutBinding
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.LoginSingletonResponse
import com.uptodd.uptoddapp.datamodel.videocontent.Content
import com.uptodd.uptoddapp.datamodel.videocontent.VideoContentList
import com.uptodd.uptoddapp.ui.freeparenting.daily_book.tabs.DailyContentFragment
import com.uptodd.uptoddapp.ui.freeparenting.daily_book.viewmodel.DailyBookVideoModel
import com.uptodd.uptoddapp.ui.freeparenting.purchase.viewpager.ViewPagerAdapter
import com.uptodd.uptoddapp.utils.*
import com.uptodd.uptoddapp.utils.dialog.showDialogBox
import com.uptodd.uptoddapp.workManager.FREE_PARENTING_PROGRAM
import com.uptodd.uptoddapp.workManager.FreeParentingWorkManger
import java.util.concurrent.TimeUnit


class DailyBookFragment : Fragment(R.layout.daily_book_layout) {
    private lateinit var binding: DailyBookLayoutBinding
    private lateinit var viewPagerAdaptor: ViewPagerAdapter
    private val viewModel: DailyBookVideoModel by viewModels()
    private val arrayOfVideoContentDb = mutableListOf<Content>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DailyBookLayoutBinding.bind(view)
        //binding.viewPager.isUserInputEnabled = false
        uploadWorkManger()
        setNavDrawer()
        viewModel.event.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { msg ->
                showErrorDialog(msg)
            }
        }
        fetchDataFromDb()
        fetchDataFromApi()
        setVideoTabItem()
        setMargin()
        binding.navDrawer.setNavigationItemSelectedListener { menu ->
            when (menu.itemId) {
                R.id.free_parenting_logout -> {
                    menu.isChecked = true
                    activity?.showDialogBox(
                        "Logout?",
                        "Are you sure want to logout?",
                        "No",
                        R.drawable.ic_baseline_exit_to_app_24,
                        "Yes",
                        cancelListener = {
                            val activity = (activity as FreeParentingDemoActivity?)
                            if (activity != null && activity.logout()) {
                                activity.gotSelectionScreen()
                            } else {
                                binding.root.showSnackbar("Cannot logout!!")
                            }
                        }, listener = {})
                }
                R.id.mnu_free_privacyFragments -> {
                    menu.isChecked = true
                    val action = DailyBookFragmentDirections.actionGlobalFreePrivacyFragments()
                    findNavController().navigate(action)
                }
                R.id.mnu_free_termsAndConditions -> {
                    menu.isChecked = true
                    val action = DailyBookFragmentDirections.actionGlobalFreeTermsAndConditions()
                    findNavController().navigate(action)
                }
                R.id.mnu_free_contact_us -> {
                    menu.isChecked = true
                    val action = DailyBookFragmentDirections.actionGlobalFreeContactFragments()
                    findNavController().navigate(action)
                }
            }
            true
        }
        binding.toolbarNav.topAppBar.setNavigationOnClickListener {
            binding.drawerLayoutFree.open()
        }
    }

    private fun setNavDrawer() {
        val email = LoginSingletonResponse.getInstance().getLoginRequest()?.email
        binding.navDrawer.getHeaderView(0).findViewById<TextView>(R.id.user_txt_nav).text =
            email.toString().first().uppercaseChar().toString()
        binding.navDrawer.getHeaderView(0).findViewById<TextView>(R.id.email_id_nav).text =
            email.toString()
        binding.navDrawer.setupWithNavController(findNavController())
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            (activity as FreeParentingDemoActivity?)?.hideBottomNavBar()
        } else {
            (activity as FreeParentingDemoActivity?)?.showBottomNavBar()
        }
    }

    private fun setVideoTabItem() {
        viewModel.tabListAndContent.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                binding.itemContainer.show()
                setAdaptor()
                it.forEachIndexed { index, res ->
                    setFragment(res.first, res.second, (index % getAdaptorViewHolderBg.size))
                }
                binding.progressForVideoContent.hide()
                binding.progressForTabContent.hide()
                binding.loadingTxt.hide()
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


    private fun uploadWorkManger() {
        val constraints = Constraints.Builder()
            //.setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workManager = WorkManager.getInstance(requireActivity().application)

        val freeParentingWork =
            PeriodicWorkRequestBuilder<FreeParentingWorkManger>(15, TimeUnit.MINUTES)
                .addTag(FREE_PARENTING_PROGRAM)
                .setConstraints(constraints)
                .build()
        //Just for testing purpose
        workManager.cancelAllWorkByTag(FREE_PARENTING_PROGRAM)

        workManager.enqueue(freeParentingWork)
        workManager.getWorkInfoByIdLiveData(freeParentingWork.id).observe(viewLifecycleOwner) {
            setLogCat("WORK_FREE", "${it.state}")
        }
    }


    private fun fetchDataFromApi() {
        viewModel.videoContentResponseFromApi.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResponseWrapper.Error -> {
                    binding.progressForTabContent.hide()
                    binding.progressForVideoContent.hide()
                    binding.itemContainer.show()
                    binding.loadingTxt.hide()
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
                    binding.loadingTxt.text = "${it.data}"
                    binding.loadingTxt.show()
                    binding.itemContainer.hide()

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

    private fun setFragment(title: String, list: List<Content>, index: Int) {
        viewPagerAdaptor.setFragment(
            DailyContentFragment(
                title, list, arrayOfVideoContentDb,
                getAdaptorViewHolderBg[index]
            )
        )
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
        (activity as FreeParentingDemoActivity?)?.showBottomNavBar()
        viewModel.getVideoContentApi()
        viewModel.getVideoContentDb()
    }

}