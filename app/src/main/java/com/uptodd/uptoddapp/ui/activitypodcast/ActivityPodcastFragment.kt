package com.uptodd.uptoddapp.ui.activitypodcast

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.api.getMonth
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.activitypodcast.ActivityPodcast
import com.uptodd.uptoddapp.databinding.FragmentActivityPodcastBinding
import com.uptodd.uptoddapp.ui.webinars.fullwebinar.FullWebinarActivity
import com.uptodd.uptoddapp.ui.webinars.podcastwebinar.PodcastWebinarActivity
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.workManager.updateApiWorkmanager.CheckPodcastWorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

private const val TAG = "ActivityPodcastFragment"


class ActivityPodcastFragment:Fragment() , ActivityPodcastInterface {


    private lateinit var binding: FragmentActivityPodcastBinding

    private val sharedPreferences: SharedPreferences by lazy {
        requireActivity().getSharedPreferences("last_updated", Context.MODE_PRIVATE)
    }

    private val sharedPrefWorkManager: SharedPreferences by lazy {
        requireActivity().getSharedPreferences("podcast_work_manager", Context.MODE_PRIVATE)
    }

    private val uptoddDatabase: UptoddDatabase by lazy {
        UptoddDatabase.getInstance(requireContext())
    }

    private var activityPodcastList = mutableListOf<ActivityPodcast>()

    private val ioScope = CoroutineScope(Dispatchers.Main)

    private val adapter = ActivityPodcastAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentActivityPodcastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestWorkManager()
        val lastFetched = sharedPreferences.getLong("ACTIVITY_PODCAST", -1)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (lastFetched == -1L || lastFetched < calendar.timeInMillis) {
            binding.activityPodcastRefresh.isRefreshing = true
            fetchDataFromApi()
            sharedPreferences.edit().putLong("ACTIVITY_PODCAST", calendar.timeInMillis).apply()
        } else {
            binding.activityPodcastRefresh.isRefreshing = true
            fetchDataFromLocalDb()
        }

        binding.activityPodcastRefresh.setOnRefreshListener {
            hideNodata()
            fetchDataFromApi()
        }
    }



    private fun fetchDataFromLocalDb() {
        uptoddDatabase.activityPodcastDao.getAll()
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                binding.activityPodcastRefresh.isRefreshing = false
                if (it.isNullOrEmpty()) {
                    showNoData()
                    hideRecyclerView()
                } else {
                    hideNodata()
                    activityPodcastList = it.toMutableList()
                    setupRecyclerView()
                }

            })
    }

    private fun fetchDataFromApi() {
        val uid = AllUtil.getUserId()
        val months = getMonth(requireContext())
        val lang = AllUtil.getLanguage()

        Log.d("months", months.toString())

        AndroidNetworking.get("https://uptodd.com/api/activitypodcast?userId={userId}&months={months}&lang={lang}")
            .addPathParameter("userId", uid.toString())
            .addPathParameter("months", months.toString())
            .addPathParameter("lang", lang)
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    if (response == null) return

                    /* the get method doesn't support returning
                       nullable types so in order to handle nullable
                       objects try block is used to detect nullable
                       object.
                    */

                    try {
                        val data = response.get("data") as JSONArray
                        if (data.length() <= 0) {
                            showNoData()
                            hideRecyclerView()
                        } else {
                            parseData(response.get("data") as JSONArray)
                            hideNodata()
                        }
                    } catch (e: Exception) {
                        Log.i(TAG, "${e.message}")
                        showNoData()
                        hideRecyclerView()
                        return
                    } finally {
                        binding.activityPodcastRefresh.isRefreshing = false
                    }
                }
                override fun onError(anError: ANError?) {
                    binding.activityPodcastRefresh.isRefreshing = false
                }

            })
    }

    private fun parseData(data: JSONArray) {

        for (i in 0 until data.length()) {
            val obj = data.get(i) as JSONObject
            val sample = ActivityPodcast(
                id = obj.getInt("id"),
                title = obj.getString("title"),
                description = obj.getString("description"),
                kitContent = obj.getString("kit_content"),
                video = obj.getString("video")
            )
            activityPodcastList.add(sample)
        }

        ioScope.launch {
            uptoddDatabase.activityPodcastDao.insertAll(activityPodcastList)
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter.list = activityPodcastList
        binding.activityPodcastRecycler.adapter = adapter
        showRecyclerView()
    }


    private fun hideNodata() {
        binding.noDataContainer.isVisible = false
    }

    private fun showNoData() {
        binding.noDataContainer.isVisible = true
    }

    private fun showRecyclerView() {
        binding.activityPodcastRecycler.isVisible = true
    }

    private fun hideRecyclerView() {
        binding.activityPodcastRecycler.isVisible = false
    }

    override fun onClick(act_podacast: ActivityPodcast) {
        val intent = Intent(context, PodcastWebinarActivity::class.java)
        intent.putExtra("url", act_podacast.video)
        intent.putExtra("title", act_podacast.title)
        intent.putExtra("kit_content",act_podacast.kitContent)
        intent.putExtra("description",act_podacast.description)
        startActivity(intent)
    }




    private fun requestWorkManager()
    {

        val check=sharedPrefWorkManager.getInt("ALREADY_REQUESTED",0)

        if(check==0) {
            var workRequest = OneTimeWorkRequest.Builder(
                CheckPodcastWorkManager::class.java
            ).build()

            context?.let { WorkManager.getInstance(it).enqueue(workRequest) }

            sharedPrefWorkManager.edit().putInt("ALREADY_REQUESTED", 1).apply()
        }
    }
}