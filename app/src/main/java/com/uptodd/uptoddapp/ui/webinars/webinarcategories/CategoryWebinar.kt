/*package com.uptodd.uptoddapp.ui.blogs.blogcategories

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FragmentCategory2Binding
import com.uptodd.uptoddapp.ui.webinars.fullwebinar.FullWebinarActivity
import com.uptodd.uptoddapp.ui.webinars.webinarslist.WebinarsAdapter
import com.uptodd.uptoddapp.ui.webinars.webinarslist.WebinarsListViewModel


class CategoryWebinar : Fragment(),WebinarsAdapter.OnCompleteClickListener {

    lateinit var binding: FragmentCategory2Binding
    lateinit var viewModel: WebinarsListViewModel

    private lateinit var recyclerAdapter: WebinarsAdapter

    private var categoryVariableId:Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_category2,container,false)
        binding.lifecycleOwner = this

        viewModel= ViewModelProvider(this).get(WebinarsListViewModel::class.java)

        val list:ArrayList<String> = ArrayList<String>()
        list.add("Sort by recent post")
        list.add("Sort by popularity")
        val adapter: ArrayAdapter<String> = ArrayAdapter(binding.root.context, R.layout.support_simple_spinner_dropdown_item, list)
        binding.spinner.adapter=adapter

        categoryVariableId=requireArguments().getLong("categoryVariableId")

        recyclerAdapter = WebinarsAdapter(emptyList(), this)
        binding.recyclerView.adapter = recyclerAdapter

        //get blog list from viewModel according to category id
        //blog list should be a mutable live data and add observer here with below two lines inside it
        recyclerAdapter.allWebinarsList=viewModel.WebinarsList_baby     //replace viewModel.blogList_baby by category wise blog list
        recyclerAdapter.notifyDataSetChanged()

        //add filter
        //add code for show more button

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    Toast.makeText(activity, "Last", Toast.LENGTH_LONG).show()
                }
            }
        })

        return binding.root
    }

    override fun onClickWebinar(position: Int) {
        val webinar=recyclerAdapter.allWebinarsList[position]
        val intent= Intent(context,FullWebinarActivity::class.java)
        intent.putExtra("url",webinar.webinarURL)
        startActivity(intent)
    }

}*/

package com.uptodd.uptoddapp.ui.webinars.webinarcategories

import android.app.Dialog
import android.content.Context
import android.content.Intent
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.UptoddViewModelFactory
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.webinars.DualWebinars
import com.uptodd.uptoddapp.database.webinars.Webinars
import com.uptodd.uptoddapp.database.webinars.WebinarsDatabaseDao
import com.uptodd.uptoddapp.databinding.FragmentCategory2Binding
import com.uptodd.uptoddapp.ui.webinars.fullwebinar.FullWebinarActivity
import com.uptodd.uptoddapp.ui.webinars.webinarslist.WebinarsAdapter
import com.uptodd.uptoddapp.ui.webinars.webinarslist.WebinarsListViewModel
import com.uptodd.uptoddapp.utilities.AppNetworkStatus
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.ScreenDpi
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import java.util.*


class CategoryWebinar : Fragment(), WebinarsAdapter.OnCompleteClickListener {

    lateinit var binding: FragmentCategory2Binding
    lateinit var viewModel: WebinarsListViewModel

    private lateinit var recyclerAdapter: WebinarsAdapter

    private var categoryVariableId: Long = 0

    var preferences: SharedPreferences? = null

    private lateinit var webinarPreferences: SharedPreferences
    private lateinit var webinarsDatabaseDao: WebinarsDatabaseDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_category2, container, false)
        binding.lifecycleOwner = this

        val viewModelFactory =
            UptoddViewModelFactory.getInstance(
                requireActivity().application
            )

        viewModel = ViewModelProvider(this, viewModelFactory).get(WebinarsListViewModel::class.java)
        binding.webinarListViewModel = viewModel

        preferences = activity?.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        webinarPreferences =
            requireActivity().getSharedPreferences("WEBINAR_WEB", Context.MODE_PRIVATE)

        webinarsDatabaseDao = UptoddDatabase.getInstance(requireContext()).webinarsDatabaseDao

        if (preferences!!.contains("token"))
            viewModel.token = preferences!!.getString("token", "")

        viewModel.dpi = ScreenDpi(requireContext()).getScreenDrawableType()

//        val list: ArrayList<String> = ArrayList()
//        list.add("Sort by recent post")
//        list.add("Sort by popularity")
//        val adapter: ArrayAdapter<String> =
//            ArrayAdapter(binding.root.context, R.layout.support_simple_spinner_dropdown_item, list)
//        binding.spinner.adapter = adapter

        categoryVariableId = requireArguments().getLong("categoryVariableId")
        viewModel.categoryId = categoryVariableId

        recyclerAdapter = WebinarsAdapter(listOf(), this)
        binding.recyclerView.adapter = recyclerAdapter

        binding.webinarRefresh.setOnRefreshListener {
            loadWebinarsFromApi()
        }

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
            loadWebinarsFromApi()
            webinarPreferences.edit {
                putString("last_updated", today.timeInMillis.toString())
                apply()
            }
        } else if (lastUpdated.toLong() < today.timeInMillis) {
            loadWebinarsFromApi()
            webinarPreferences.edit {
                putString("last_updated", today.timeInMillis.toString())
                apply()
            }
        } else {
            loadWebinarsFromLocalDb()
        }

        viewModel.webinarsList.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it.isNotEmpty()) {
                    val listWebinars = convertToDualWebinars(it)
                    recyclerAdapter.allWebinarsList = listWebinars
                    recyclerAdapter.notifyDataSetChanged()
                }
            }
        })

    }

    private fun loadWebinarsFromLocalDb() {
        if (categoryVariableId == -1L) {
            webinarsDatabaseDao.getAll().observe(viewLifecycleOwner, Observer {
                it?.let {
                    viewModel.webinarsList.value = it.toMutableList()
                }
            })
        } else {
            webinarsDatabaseDao.getAllByCategory(categoryVariableId).observe(
                viewLifecycleOwner,
                Observer {
                    it?.let {
                        viewModel.webinarsList.value = it.toMutableList()
                    }
                }
            )
        }
    }

    private fun loadWebinarsFromApi() {
        if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
            viewModel.isLoadingDialogVisible.value = true
            showLoadingDialog()
            viewModel.getWebinarListByCategoryId()
        } else {
            //showInternetNotConnectedDialog()
            val snackbar = Snackbar.make(
                binding.layout,
                getString(R.string.no_internet_connection),
                Snackbar.LENGTH_INDEFINITE
            )
            snackbar.show()
        }
    }

    override fun onClickWebinar1(position: Int) {
        if (position < 0) return
        Log.d("div", "CategoryWebinar L150 $position")
        val webinar = recyclerAdapter.allWebinarsList[position]
        val intent = Intent(context, FullWebinarActivity::class.java)
        if (webinar != null) {
            intent.putExtra("url", webinar.webinarURL1)
            intent.putExtra("description", webinar.description1)
            intent.putExtra("title", webinar.title1)
        }
        startActivity(intent)
    }

    override fun onClickWebinar2(position: Int) {
        if (position < 0) return
        Log.d("div", "CategoryWebinar L161 $position")
        val webinar = recyclerAdapter.allWebinarsList[position]
        val intent = Intent(context, FullWebinarActivity::class.java)
        if (webinar != null) {
            intent.putExtra("url", webinar.webinarURL2)
            intent.putExtra("description", webinar.description2)
            intent.putExtra("title", webinar.title2)
        }
        startActivity(intent)
    }

    private fun initScrollListener() {
        Log.d("div", "CategoryWebinar L189")
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Log.d("div", "CategoryWebinar L197")
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                if (!viewModel.isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == viewModel.dualWebinarsList.size - 1) {
                        //bottom of list!
                        Log.d("div", "CategoryWebinar L202")
                        if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
                            viewModel.loadMore(recyclerAdapter, viewModel.dualWebinarsList.size)
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

    private fun convertToDualWebinars(webinarsList: MutableList<Webinars?>): MutableList<DualWebinars?> {
        var i: Int = 0
        while (i < webinarsList.size) {
            var dualWebinars = DualWebinars()
            if (webinarsList[i] != null) {
                dualWebinars.webinarId = webinarsList[i]!!.webinarId
                dualWebinars.webinarURL1 = webinarsList[i]!!.webinarURL
                dualWebinars.imageURL1 = webinarsList[i]!!.imageURL
                dualWebinars.title1 = webinarsList[i]!!.title
                dualWebinars.description1 = webinarsList[i]!!.description
                dualWebinars.date1 = webinarsList[i]!!.date
            }
            i++
            if (i < webinarsList.size) {
                if (webinarsList[i] != null) {
                    dualWebinars.webinarURL2 = webinarsList[i]!!.webinarURL
                    dualWebinars.imageURL2 = webinarsList[i]!!.imageURL
                    dualWebinars.title2 = webinarsList[i]!!.title
                    dualWebinars.description2 = webinarsList[i]!!.description
                    dualWebinars.date2 = webinarsList[i]!!.date
                }
                i++
            }
            viewModel.dualWebinarsList.add(dualWebinars)
        }
        return viewModel.dualWebinarsList
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