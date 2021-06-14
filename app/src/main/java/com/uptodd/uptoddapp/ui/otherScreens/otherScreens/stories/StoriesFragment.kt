package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.stories

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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.stories.StoriesDao
import com.uptodd.uptoddapp.databinding.FragmentStoriesBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class StoriesFragment : Fragment(), StoriesRecyclerAdapter.StoriesListener {

    private lateinit var binding: FragmentStoriesBinding

    private var list = mutableListOf<Story>()

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val isLoadingDialogVisible = MutableLiveData<Boolean>()

    private lateinit var preferences: SharedPreferences
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private lateinit var storiesDao: StoriesDao


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fadeThrough = MaterialFadeThrough().apply {
            duration = 1000
        }

        enterTransition = fadeThrough
        reenterTransition = fadeThrough    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        initialiseBindingAndViewModel(inflater, container)

        if(AllUtil.isUserPremium(requireContext()))
        {
            if(!AllUtil.isSubscriptionOverActive(requireContext()))
            {
                binding.upgradeButton.visibility= View.GONE
            }
        }
        binding.upgradeButton.setOnClickListener {

            it.findNavController().navigate(R.id.action_storiesFragment_to_upgradeFragment)
        }
        preferences = requireActivity().getSharedPreferences("last_updated", Context.MODE_PRIVATE)
        storiesDao = UptoddDatabase.getInstance(requireContext()).storiesDao



        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val today = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val lastUpdated = preferences.getString("STORIES", "")!!

        if (lastUpdated.isBlank()) {
            fetchStroiesDataFromApi()
            preferences.edit {
                putString("STORIES", today.timeInMillis.toString())
                apply()
            }
        } else if (lastUpdated.toLong() < today.timeInMillis) {
            fetchStroiesDataFromApi()
            preferences.edit {
                putString("STORIES", today.timeInMillis.toString())
                apply()
            }
        } else {
            fetchStroiesDataFromLocalDb()
        }

        binding.storyRefresh.setOnRefreshListener {
            fetchStroiesDataFromApi()
        }
    }


    private fun fetchStroiesDataFromLocalDb() {
        storiesDao.getAll().observe(viewLifecycleOwner, Observer { stories ->
            if (stories.isEmpty()) {
                fetchStroiesDataFromApi()
            } else {
                list = stories.toMutableList()
                setupRecyclerView()
            }
        })
    }

    private fun fetchStroiesDataFromApi() {
        if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
            isLoadingDialogVisible.value = true
            showLoadingDialog()
            val userType= UptoddSharedPreferences.getInstance(requireContext()).getUserType()
            val country=AllUtil.getCountry(requireContext())
            uiScope.launch {
                AndroidNetworking.get("https://uptodd.com/api/stories?userType=$userType&country=$country")
                    .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            if (response != null) {
                                Log.d("putResposnse", response.get("status").toString())
                                val data = response.get("data") as JSONArray
                                parseData(data)
                            }
                            binding.storyRefresh.isRefreshing = false
                        }

                        override fun onError(anError: ANError?) {
                            binding.storyRefresh.isRefreshing = false
                        }
                    })
            }
        } else {
            Snackbar.make(
                requireView(),
                getString(R.string.no_internet_connection),
                Snackbar.LENGTH_LONG
            )
                .setAction(getString(R.string.retry)) {
                    binding.storyRefresh.isRefreshing = true
                    fetchStroiesDataFromApi()
                }.show()
            binding.storyRefresh.isRefreshing = false
        }
    }

    private fun parseData(data: JSONArray) {
        val dpi = ScreenDpi(requireContext()).getScreenDrawableType()
        val appendable = "https://uptodd.com/images/app/android/thumbnails/stories/$dpi/"
        var i = 0
        list.clear()
        while (i < data.length()) {
            val obj = data.get(i) as JSONObject
            Log.d("div", "StoriesFragment L103 $obj")
            list.add(
                Story(
                    name = obj.getString("name"),
                    url = appendable + obj.getString("image") + ".webp",
                    image = obj.getString("image"),
                    description = obj.getString("description"),
                    language = obj.getString("language")
                )
            )
            i++
        }

        ioScope.launch {
            storiesDao.insertAll(list)
        }

        isLoadingDialogVisible.value = false

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val adapter = StoriesRecyclerAdapter(list, this)
        binding.recyclerView.adapter = adapter
    }


    private fun initialiseBindingAndViewModel(inflater: LayoutInflater, container: ViewGroup?) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_stories, container, false)
        binding.lifecycleOwner = this
    }

    override fun onClickStory(position: Int) {
        val bundle = Bundle()
        bundle.putString("actionBar", getString(R.string.stories))
        bundle.putString("image", list[position].image)
        bundle.putString("title", list[position].name)
        bundle.putString("description", list[position].description)
        bundle.putString("folder", "stories")
        findNavController().navigate(R.id.action_storiesFragment_to_detailsFragment, bundle)
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
        isLoadingDialogVisible.observe(viewLifecycleOwner, Observer {
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