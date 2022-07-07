package com.uptodd.uptoddapp.ui.activitypodcast

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.activitypodcast.ActivityPodcast
import com.uptodd.uptoddapp.databinding.FragmentActivityPodcastBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.models.SuggestedVideosModel
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.models.VideosUrlResponse
import com.uptodd.uptoddapp.ui.webinars.podcastwebinar.PodcastWebinarActivity
import com.uptodd.uptoddapp.utilities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

private const val TAG = "ActivityPodcastFragment"


class ActivityPodcastFragment : Fragment(), ActivityPodcastInterface {


    private lateinit var binding: FragmentActivityPodcastBinding
    private var videosRespons: VideosUrlResponse? = null

    private val sharedPreferences: SharedPreferences by lazy {
        requireActivity().getSharedPreferences("last_updated", Context.MODE_PRIVATE)
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
        if (AllUtil.isUserPremium(requireContext())) {
            if (!AllUtil.isSubscriptionOverActive(requireContext())) {
                binding.upgradeButton.visibility = View.GONE
            }
        }
        binding.upgradeButton.setOnClickListener {

            it.findNavController().navigate(R.id.action_activityPodcastFragment_to_upgradeFragment)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ToolbarUtils.initToolbar(
            requireActivity(), binding.collapseToolbar,
            findNavController(), getString(R.string.activity_podcast), "Curated in UpTodd's Lab",
            R.drawable.activity_podcast_icon
        )

        fetchTutorials(requireContext())

        binding.collapseToolbar.playTutorialIcon.setOnClickListener {

            fragmentManager?.let { it1 ->
                val intent = Intent(context, PodcastWebinarActivity::class.java)
                intent.putExtra("url", videosRespons?.activityPodcast)
                intent.putExtra("title", "Activity Podcast")
                intent.putExtra("kit_content", "")
                intent.putExtra("description", "")
                intent.putExtra("videos", SuggestedVideosModel(mutableListOf()))
                startActivity(intent)
            }


        }

        binding.collapseToolbar.playTutorialIcon.visibility = View.VISIBLE

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


        if (UptoddSharedPreferences.getInstance(requireContext()).shouldShowPodcastTip()) {
            ShowInfoDialog.showInfo(getString(R.string.screen_podcast), requireFragmentManager())
            UptoddSharedPreferences.getInstance(requireContext()).setShownPodcastTip(false)
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


    fun com.androidnetworking.AndroidNetworking.postS(s: String) {

    }

    private fun fetchDataFromApi() {
        val uid = AllUtil.getUserId()
        val months = KidsPeriod(requireContext()).getKidsAge()
        val lang = AllUtil.getLanguage()





        Log.d("months", months.toString())
        val userType = UptoddSharedPreferences.getInstance(requireContext()).getUserType()
        val stage = UptoddSharedPreferences.getInstance(requireContext()).getStage()
        val country = AllUtil.getCountry(requireContext())


        AndroidNetworking.get("https://www.uptodd.com/api/activitypodcast?userId={userId}&months={months}&lang={lang}&userType=$userType&country=$country&motherStage=$stage")
            .addPathParameter("userId", uid.toString())
            .addPathParameter("months", months.toString())
            .addPathParameter("lang", lang)
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {


                    if (response == null) {
                    }

                    /* the get method doesn't support returning
                       nullable types so in order to handle nullable
                       objects try block is used to detect nullable
                       object.
                    */

                    try {
                        val data = response?.get("data") as JSONArray

                        Log.d("activity podcast c", "${data.length()}")

                        if (data.length() <= 0) {
                            showNoData()
                            hideRecyclerView()
                        } else {
                            UptoddSharedPreferences.getInstance(requireContext())
                                .saveCountPodcast(data.length())
                            parseData(response.get("data") as JSONArray)
                            hideNodata()
                            showRecyclerView()
                        }
                    } catch (e: Exception) {
                        Log.i(TAG, "${e.message}")
                        //val stage=UptoddSharedPreferences.getInstance(requireContext()).getStage()

                        if (context != null && !AllUtil.isUserPremium(requireContext())) {
                            val title = activity?.actionBar?.title.toString()

                            val upToddDialogs = context?.let {
                                return@let UpToddDialogs(it)
                            }
                            upToddDialogs?.showInfoDialog("$title is not activated/required for you",
                                "Close",
                                object : UpToddDialogs.UpToddDialogListener {
                                    override fun onDialogButtonClicked(dialog: Dialog) {
                                        try {
                                            findNavController().navigateUp()
                                        } catch (e: Exception) {
                                            activity?.let { act ->
                                                Toast.makeText(
                                                    act,
                                                    "Please Try Again",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                })

                        }

                        showNoData()
                        hideRecyclerView()
                        return
                    } finally {
                        binding.activityPodcastRefresh.isRefreshing = false
                    }
                }

                override fun onError(anError: ANError?) {

                    val stage = UptoddSharedPreferences.getInstance(requireContext()).getStage()
                    var dialogOnce = false
                    if (stage == "prenatal" || stage == "pre birth") {
                        val handler=Handler(Looper.getMainLooper())
                        handler.post {
                            if (!dialogOnce) {
                                dialogOnce = true
                                val upToddDialogs = UpToddDialogs(requireContext())
                                upToddDialogs.showInfoDialog("This section is only for postnatal user",
                                    "Close",
                                    object : UpToddDialogs.UpToddDialogListener {
                                        override fun onDialogButtonClicked(dialog: Dialog) {

                                            findNavController().navigateUp()

                                        }
                                    })
                            }
                        }
                    }

                    binding.activityPodcastRefresh.isRefreshing = false
                }

            })
    }

    private fun parseData(data: JSONArray) {

        activityPodcastList.clear()
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
        val handler=Handler(Looper.getMainLooper())
        var isShowNoDataVisible=false
        handler.post {
            if (!isShowNoDataVisible){
                isShowNoDataVisible=true
                if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
                    val title = (requireActivity() as AppCompatActivity).supportActionBar?.title

                    val upToddDialogs = UpToddDialogs(requireContext())
                    upToddDialogs.showInfoDialog("$title is not activated/required for you",
                        "Close",
                        object : UpToddDialogs.UpToddDialogListener {
                            override fun onDialogButtonClicked(dialog: Dialog) {
                                dialog.dismiss()
                            }

                            override fun onDialogDismiss() {
                                findNavController().navigateUp()
                            }
                        })

                }
                binding.noDataContainer.isVisible = true
            }
        }
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
        intent.putExtra("kit_content", act_podacast.kitContent)
        intent.putExtra("description", act_podacast.description)
        intent.putExtra("videos", SuggestedVideosModel(mutableListOf()))
        startActivity(intent)
    }

    override fun onOptionsItemSelected(@NonNull item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                findNavController().navigateUp()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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
