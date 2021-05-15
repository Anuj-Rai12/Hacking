package com.uptodd.uptoddapp.ui.blogs.blogslist

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.uptodd.uptoddapp.database.blogs.BlogCategories
import com.uptodd.uptoddapp.database.blogs.BlogCategoryDao
import com.uptodd.uptoddapp.databinding.FragmentBlogsBinding
import com.uptodd.uptoddapp.ui.blogs.blogcategories.Category1
import com.uptodd.uptoddapp.utilities.AppNetworkStatus
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import java.util.*
import kotlin.collections.ArrayList


class BlogsFragment : Fragment() {

    private lateinit var binding: FragmentBlogsBinding
    lateinit var viewModel: BlogsListViewModel

    private lateinit var categoriesList: ArrayList<BlogCategories?>

    private lateinit var preferences: SharedPreferences
    private lateinit var blogPreferences: SharedPreferences

    private lateinit var categoryDao: BlogCategoryDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_blogs, container, false)

        binding.lifecycleOwner = this

        categoryDao = UptoddDatabase.getInstance(requireContext()).categoryDao

        val viewModelFactory =
            UptoddViewModelFactory.getInstance(
                requireActivity().application
            )

        viewModel = ViewModelProvider(this, viewModelFactory).get(BlogsListViewModel::class.java)

//        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.blogs)
//        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        preferences = requireActivity().getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        blogPreferences = requireActivity().getSharedPreferences("BLOG_CAT", Context.MODE_PRIVATE)


        if (preferences.contains("token"))
            viewModel.token = preferences.getString("token", "")




        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.categoriesList.observe(viewLifecycleOwner, Observer {
            Log.d(
                "div",
                "BlogsFragment L44 Observer called ${viewModel.categoriesList.value!!.size}"
            )
            categoriesList = viewModel.categoriesList.value!!
            addCategory()
        })


        val lastUpdated = blogPreferences.getString("last_updated", "")!!
        val today = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (lastUpdated.isBlank()) {
            loadCategories()
            blogPreferences.edit {
                putString("last_updated", today.timeInMillis.toString())
                apply()
            }
        } else if (lastUpdated.toLong() < today.timeInMillis) {
            loadCategories()
            blogPreferences.edit {
                putString("last_updated", today.timeInMillis.toString())
                apply()
            }
        } else {
            loadCategoriesFromLocalDb()
        }

    }

    private fun loadCategoriesFromLocalDb() {
        categoryDao.getAll().observe(viewLifecycleOwner, Observer {
            categoriesList = it as ArrayList<BlogCategories?>
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
                requireView(),
                getString(R.string.no_internet_connection),
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(getString(R.string.retry)) {
                    loadCategories()
                }
            snackbar.show()
        }
    }


    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }*/

    private fun addCategory() {
        val viewPagerAdapter = ViewPagerAdapter(this)

        binding.viewPager.adapter = viewPagerAdapter

        var i = 0

        while (i < categoriesList.size) {
            val bundle = Bundle()
            bundle.putLong("categoryVariableId", categoriesList[i]?.categoryId!!)
            val fragment = Category1()
            fragment.arguments = bundle
            categoriesList[i]?.categoryName?.let { viewPagerAdapter.apply { addFragment(fragment) } }
            i++
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            if (position == 0)
                tab.text = getString(R.string.all)
            else
                tab.text = categoriesList[position]?.categoryName
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
        val handler = Handler()
        handler.postDelayed({
            upToddDialogs.dismissDialog()
        }, R.string.loadingDuarationInMillis.toLong())

    }

    /*class ViewPagerAdapter(fm: FragmentManager, behavior: Int) :
        FragmentPagerAdapter(fm, behavior) {

        private val fragments=ArrayList<Fragment>()
        private val fragmentTitles= ArrayList<String>()

        fun addFragment(fragment: Fragment, title:String)
        {
            fragments.add(fragment)
            fragmentTitles.add(title)
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitles[position]
        }
    }*/

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