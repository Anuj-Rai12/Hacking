package com.uptodd.uptoddapp.ui.webinars.webinarslist

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialSharedAxis
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.UptoddViewModelFactory
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.webinars.WebinarCategories
import com.uptodd.uptoddapp.database.webinars.WebinarCategoryDao
import com.uptodd.uptoddapp.databinding.FragmentWebinarsBinding
import com.uptodd.uptoddapp.ui.webinars.webinarcategories.CategoryWebinar
import com.uptodd.uptoddapp.utilities.AppNetworkStatus
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import java.util.*
import kotlin.collections.ArrayList

class WebinarsFragment : Fragment() {

    private lateinit var binding: FragmentWebinarsBinding
    lateinit var viewModel: WebinarsListViewModel

    lateinit var categoriesList: List<WebinarCategories>

    var preferences: SharedPreferences? = null
    private lateinit var webinarPreferences: SharedPreferences

    private lateinit var webinarCategoryDao: WebinarCategoryDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_webinars, container, false)

        binding.lifecycleOwner = this

        val viewModelFactory =
            UptoddViewModelFactory.getInstance(
                requireActivity().application
            )

        viewModel = ViewModelProvider(this, viewModelFactory).get(WebinarsListViewModel::class.java)

        preferences = requireActivity().getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        webinarCategoryDao = UptoddDatabase.getInstance(requireContext()).webinarCategoryDao

        webinarPreferences =
            requireActivity().getSharedPreferences("WEBINAR_CAT", Context.MODE_PRIVATE)

        if (preferences!!.contains("token"))
            viewModel.token = preferences!!.getString("token", "")

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        val lastUpdated = webinarPreferences.getString("last_updated", "")!!

        val today = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (lastUpdated.isBlank()) {
            loadCategories()
            webinarPreferences.edit {
                putString("last_updated", today.timeInMillis.toString())
                apply()
            }
        } else if (lastUpdated.toLong() < today.timeInMillis) {
            loadCategories()
            webinarPreferences.edit {
                putString("last_updated", today.timeInMillis.toString())
                apply()
            }
        } else {
            loadCategoriesFromLocalDb()
        }

        viewModel.categoriesList.observe(viewLifecycleOwner, Observer {
            Log.d("div", "BlogsFragment L44 Observer called")
            categoriesList = viewModel.categoriesList.value!!
            addCategory()
        })

    }

    private fun loadCategoriesFromLocalDb() {
        webinarCategoryDao.getAll().observe(viewLifecycleOwner, Observer {
            categoriesList = it
            addCategory()
        })
    }

    private fun loadCategories() {
        if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
            showLoadingDialog()
            viewModel.getAllCategories()
        } else {
            //showInternetNotConnectedDialog()
            val snackbar = Snackbar.make(
                binding.layout,
                getString(R.string.no_internet_connection),
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(getString(R.string.retry)) {
                    loadCategories()
                }
            snackbar.show()
        }
    }

    private fun addCategory() {
        val viewPagerAdapter = ViewPagerAdapter(this)

        binding.viewPager.adapter = viewPagerAdapter

        var i: Int = 0
        while (i < categoriesList.size) {
            val bundle = Bundle()
            bundle.putLong("categoryVariableId", categoriesList[i].categoryId)
            val fragment = CategoryWebinar()
            fragment.arguments = bundle
            categoriesList[i].categoryName.let { viewPagerAdapter.apply { addFragment(fragment) } }
            i++
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            if (position == 0)
                tab.text = getString(R.string.all)
            else
                tab.text = categoriesList[position].categoryName
        }.attach()
    }


    private fun showInternetNotConnectedDialog() {
        val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showDialog(R.drawable.gif_loading,
            getString(R.string.no_internet_connection),
            getString(R.string.back),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    dialog.dismiss()
                    findNavController().navigateUp()
                }
            })
    }

    private fun showLoadingDialog() {
        val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showDialog(R.drawable.gif_loading,
            getString(R.string.loading_please_wait),
            getString(R.string.back),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    dialog.dismiss()
                    findNavController().navigateUp()
                }
            })
        viewModel.isLoadingDialogVisible.observe(viewLifecycleOwner, Observer {
            if (!it) {
                upToddDialogs.dismissDialog()
            }
        })
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            upToddDialogs.dismissDialog()
        }, R.string.loadingDuarationInMillis.toLong())

    }

    class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        private val fragmentList = ArrayList<Fragment>()
        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]
        }

        fun addFragment(fragment: Fragment) {
            fragmentList.add(fragment)
        }
    }
}