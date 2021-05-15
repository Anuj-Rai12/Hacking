package com.uptodd.uptoddapp.ui.blogs.blogcategories

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.UptoddViewModelFactory
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.blogs.BlogDao
import com.uptodd.uptoddapp.databinding.FragmentCategory1Binding
import com.uptodd.uptoddapp.ui.blogs.blogslist.BlogsAdapter
import com.uptodd.uptoddapp.ui.blogs.blogslist.BlogsListViewModel
import com.uptodd.uptoddapp.ui.blogs.fullblog.FullBlogActivity
import com.uptodd.uptoddapp.utilities.AppNetworkStatus
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.ScreenDpi
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import java.util.*
import kotlin.collections.ArrayList


class Category1 : Fragment(), BlogsAdapter.OnCompleteClickListener {

    lateinit var binding: FragmentCategory1Binding
    lateinit var viewModel: BlogsListViewModel

    private lateinit var recyclerAdapter: BlogsAdapter

    private var categoryVariableId: Long = 0

    private lateinit var preferences: SharedPreferences

    private lateinit var blogPreferences: SharedPreferences

    private lateinit var blogDao: BlogDao


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_category1, container, false)
        binding.lifecycleOwner = this

        val viewModelFactory =
            UptoddViewModelFactory.getInstance(
                requireActivity().application
            )

        viewModel = ViewModelProvider(this, viewModelFactory).get(BlogsListViewModel::class.java)
        binding.blogsListViewModel = viewModel

        preferences = requireActivity().getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)

        blogDao = UptoddDatabase.getInstance(requireContext()).blogDao

        if (preferences.contains("token"))
            viewModel.token = preferences.getString("token", "")

        viewModel.dpi = ScreenDpi(requireContext()).getScreenDrawableType()

        val list: ArrayList<String> = ArrayList()
        list.add("Sort by recent post")
        list.add("Sort by popularity")
        val adapter: ArrayAdapter<String> =
            ArrayAdapter(binding.root.context, R.layout.support_simple_spinner_dropdown_item, list)
        binding.spinner.adapter = adapter

        categoryVariableId = requireArguments().getLong("categoryVariableId")
        viewModel.categoryId = categoryVariableId

        blogPreferences = requireActivity().getSharedPreferences(
            "Blog$categoryVariableId",
            Context.MODE_PRIVATE
        )




        recyclerAdapter = BlogsAdapter(ArrayList(), this, requireActivity())
        binding.recyclerView.adapter = recyclerAdapter

        //get blog list from viewModel according to category id
        //blog list should be a mutable live data and add observer here with below two lines inside it

        viewModel.blogsList.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it.isNotEmpty()) {
                    recyclerAdapter.allBlogsList = it
                    recyclerAdapter.notifyDataSetChanged()
                }
            }
        })


//        initScrollListener();

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val lastUpdated = blogPreferences.getString("last_updated", "")!!
        val today = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val editor = blogPreferences.edit()

        if (lastUpdated.isBlank()) {
            fetchBlogFromApi()
            editor.putString("last_updated", today.timeInMillis.toString())
            editor.apply()
        } else if (lastUpdated.toLong() < today.timeInMillis) {
            fetchBlogFromApi()
            editor.putString("last_updated", today.timeInMillis.toString())
            editor.apply()
        } else {
            fetchBlogFromLocalDb()
        }


        binding.blogRefresh.setOnRefreshListener {
            fetchBlogFromApi()
        }

    }

    private fun fetchBlogFromLocalDb() {
        if (categoryVariableId == -1L) {
            blogDao.getAll().observe(viewLifecycleOwner, Observer {
                it?.let {
                    viewModel.blogsList.value = it.toMutableList()
                }
            })
        } else {
            blogDao.getAllById(categoryVariableId).observe(viewLifecycleOwner, Observer {
                it?.let {
                    viewModel.blogsList.value = it.toMutableList()
                }
            })
        }

    }

    private fun fetchBlogFromApi() {
        if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
            viewModel.isLoadingDialogVisible.value = true
            showLoadingDialog()
            viewModel.getBlogListByCategoryId()
        } else {
            //showInternetNotConnectedDialog()
            val snackbar = Snackbar.make(
                requireView(),
                getString(R.string.no_internet_connection),
                Snackbar.LENGTH_INDEFINITE
            )
            snackbar.show()
        }
        binding.blogRefresh.isRefreshing = false
    }

    override fun onClickBlog(position: Int) {
        val blog = recyclerAdapter.allBlogsList[position]
        val intent = Intent(context, FullBlogActivity::class.java)
        if (blog != null) {
            intent.putExtra("url", blog.blogURL)
        }
        startActivity(intent)
    }

    private fun initScrollListener() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                if (!viewModel.isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == viewModel.blogsList.value?.size?.minus(
                            1
                        ) ?: 0
                    ) {
                        //bottom of list!
                        if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
                            viewModel.loadMore(recyclerAdapter, viewModel.blogsList.value!!.size)
                            viewModel.isLoading = true
                        } else {
                            //showInternetNotConnectedDialog()
                            val snackbar = Snackbar.make(
                                binding.layout,
                                getString(R.string.no_internet_connection),
                                Snackbar.LENGTH_LONG
                            )
                            snackbar.show()
                        }

                    }
                }
            }
        })
    }

    private fun showInternetNotConnectedDialog() {
        val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showDialog(
            R.drawable.gif_loading,
            getString(R.string.no_internet_connection),
            getString(R.string.close),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    dialog.dismiss()
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
}